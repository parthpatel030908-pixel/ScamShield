
from http.server import BaseHTTPRequestHandler, HTTPServer
import json
import os
from urllib.parse import urlparse

import requests


URLHAUS_API = "https://urlhaus-api.abuse.ch/v1/url/"
GOOGLE_API = "https://safebrowsing.googleapis.com/v4/threatMatches:find"


class CyberLabHandler(BaseHTTPRequestHandler):

    def send_json(self, status_code, data):
        body = json.dumps(data).encode("utf-8")
        self.send_response(status_code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        if self.path == "/health":
            self.send_json(200, {
                "project": "ScamShield",
                "status": "online",
                "purpose": "Authorised learning lab"
            })
        else:
            self.send_json(404, {"error": "Not found"})

    def do_POST(self):
        if self.path != "/check-url":
            self.send_json(404, {"error": "Not found"})
            return

        try:
            length = int(self.headers.get("Content-Length", "0"))

            if length <= 0 or length > 8192:
                self.send_json(400, {"error": "Invalid request size"})
                return

            data = json.loads(self.rfile.read(length))
            url = data.get("url", "")

            if not isinstance(url, str) or len(url) > 2048:
                self.send_json(400, {"error": "Invalid URL input"})
                return

            parsed = urlparse(url)

            if (
                parsed.scheme not in ("http", "https")
                or not parsed.hostname
                or any(char.isspace() for char in url)
            ):
                self.send_json(400, {
                    "verdict": "INVALID_URL",
                    "message": "Enter a complete HTTP or HTTPS URL."
                })
                return

            results = []
            errors = []

            # Check URLhaus
            auth_key = os.environ.get("URLHAUS_AUTH_KEY")

            if auth_key:
                try:
                    response = requests.post(
                        URLHAUS_API,
                        data={"url": url},
                        headers={"Auth-Key": auth_key},
                        timeout=12
                    )
                    response.raise_for_status()
                    result = response.json()
                    status = result.get("query_status")

                    if status == "ok":
                        results.append({
                            "source": "URLhaus",
                            "match": True,
                            "details": {
                                "threat": result.get("threat"),
                                "url_status": result.get("url_status")
                            }
                        })
                    elif status == "no_results":
                        results.append({
                            "source": "URLhaus",
                            "match": False
                        })
                    else:
                        errors.append(
                            "URLhaus returned an unexpected result."
                        )

                except (requests.RequestException, ValueError) as e:
                    print("URLhaus error:", repr(e))
                    errors.append("URLhaus check failed.")

            else:
                errors.append("URLhaus key is not configured.")

            # Check Google Safe Browsing using its JSON v4 API
            google_key = os.environ.get("GOOGLE_SAFE_BROWSING_API_KEY")

            if google_key:
                try:
                    response = requests.post(
                        GOOGLE_API,
                        params={"key": google_key},
                        json={
                            "client": {
                                "clientId": "scamshield-learning-project",
                                "clientVersion": "1.0"
                            },
                            "threatInfo": {
                                "threatTypes": [
                                    "MALWARE",
                                    "SOCIAL_ENGINEERING",
                                    "UNWANTED_SOFTWARE",
                                    "POTENTIALLY_HARMFUL_APPLICATION"
                                ],
                                "platformTypes": ["ANY_PLATFORM"],
                                "threatEntryTypes": ["URL"],
                                "threatEntries": [{"url": url}]
                            }
                        },
                        timeout=12
                    )
                    response.raise_for_status()
                    result = response.json()
                    matches = result.get("matches", [])

                    results.append({
                        "source": "Google Safe Browsing",
                        "match": bool(matches),
                        "threat_types": [
                            item.get("threatType")
                            for item in matches
                            if item.get("threatType")
                        ]
                    })

                except (requests.RequestException, ValueError) as e:
                    print("Google Safe Browsing error:", repr(e))
                    errors.append("Google Safe Browsing check failed.")

            else:
                errors.append(
                    "Google Safe Browsing key is not configured."
                )

            # Decide the final verdict
            if any(item["match"] for item in results):
                self.send_json(200, {
                    "verdict": "LIKELY_MALICIOUS",
                    "message": "At least one source reported a known threat.",
                    "sources": results,
                    "warnings": errors
                })

            elif len(results) == 2 and not errors:
                self.send_json(200, {
                    "verdict": "NO_KNOWN_THREAT_FOUND",
                    "message": (
                        "Neither source found a known threat. "
                        "This does not guarantee that the URL is safe."
                    ),
                    "sources": results,
                    "warnings": []
                })

            else:
                self.send_json(503, {
                    "verdict": "CHECK_UNAVAILABLE",
                    "message": (
                        "Unable to verify the URL using both sources."
                    ),
                    "sources": results,
                    "warnings": errors
                })

        except (ValueError, json.JSONDecodeError):
            self.send_json(400, {"error": "Invalid JSON"})

        except Exception as e:
            print("Unexpected server error:", repr(e))
            self.send_json(500, {
                "error": "Unexpected server error"
            })


if __name__ == "__main__":
    port = int(os.environ.get("PORT", "8000"))
    server = HTTPServer(("0.0.0.0", port), CyberLabHandler)
    print(f"ScamShield server listening on port {port}")
    server.serve_forever()
