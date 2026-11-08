
from http.server import BaseHTTPRequestHandler, HTTPServer
import json
import os
import socket
import ipaddress
from urllib.parse import urlparse

import requests


URLHAUS_API = "https://urlhaus-api.abuse.ch/v1/url/"
GOOGLE_API = "https://safebrowsing.googleapis.com/v4/threatMatches:find"

RESERVED_DOMAINS = (
    ".invalid",
    ".test",
    ".localhost",
    ".example",
)


class CyberLabHandler(BaseHTTPRequestHandler):

    def send_json(self, status_code, data):
        body = json.dumps(data).encode("utf-8")
        self.send_response(status_code)
        self.send_header(
            "Content-Type",
            "application/json; charset=utf-8"
        )
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
            length = int(
                self.headers.get("Content-Length", "0")
            )

            if length <= 0 or length > 8192:
                self.send_json(400, {
                    "error": "Invalid request size"
                })
                return

            data = json.loads(self.rfile.read(length))
            url = data.get("url", "")

            if not isinstance(url, str) or len(url) > 2048:
                self.send_json(400, {
                    "verdict": "INVALID_URL",
                    "message": "Invalid URL input."
                })
                return

            url = url.strip()
            parsed = urlparse(url)

            if (
                parsed.scheme.lower() not in ("http", "https")
                or not parsed.hostname
                or any(char.isspace() for char in url)
                or parsed.username is not None
                or parsed.password is not None
            ):
                self.send_json(400, {
                    "verdict": "INVALID_URL",
                    "message": "Enter a complete HTTP or HTTPS URL."
                })
                return

            try:
                port = parsed.port
            except ValueError:
                self.send_json(400, {
                    "verdict": "INVALID_URL",
                    "message": "The URL contains an invalid port."
                })
                return

            hostname = parsed.hostname.rstrip(".").lower()

            if not hostname:
                self.send_json(400, {
                    "verdict": "INVALID_URL",
                    "message": "The URL hostname is missing."
                })
                return

            # Check reserved domain endings
            if hostname == "localhost" or any(
                hostname.endswith(suffix)
                for suffix in RESERVED_DOMAINS
            ):
                self.send_json(200, {
                    "verdict": "DOMAIN_NOT_FOUND",
                    "message": (
                        "This uses a reserved domain ending "
                        "and is not a publicly registered website."
                    ),
                    "domain_status": "reserved",
                    "sources": [],
                    "warnings": [
                        "Threat database checks were not performed."
                    ]
                })
                return

            # Check IP address or DNS resolution
            try:
                ipaddress.ip_address(hostname)
                domain_status = "ip_address"

            except ValueError:
                try:
                    socket.getaddrinfo(
                        hostname,
                        port or (
                            443 if parsed.scheme.lower() == "https"
                            else 80
                        ),
                        type=socket.SOCK_STREAM
                    )
                    domain_status = "resolves"

                except socket.gaierror as dns_error:

                    if dns_error.errno == socket.EAI_NONAME:
                        self.send_json(200, {
                            "verdict": "DOMAIN_NOT_FOUND",
                            "message": (
                                "The domain could not be found "
                                "through DNS. It may not exist "
                                "or may currently be unavailable."
                            ),
                            "domain_status": "not_resolved",
                            "sources": [],
                            "warnings": [
                                "Threat database checks were not performed."
                            ]
                        })
                        return

                    self.send_json(503, {
                        "verdict": "DOMAIN_STATUS_UNCONFIRMED",
                        "message": (
                            "The domain could not be verified "
                            "because DNS lookup was unavailable."
                        ),
                        "domain_status": "unconfirmed",
                        "sources": [],
                        "warnings": [
                            "DNS lookup failed temporarily."
                        ]
                    })
                    return

            results = []
            errors = []

            # URLhaus check
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
                errors.append(
                    "URLhaus key is not configured."
                )

            # Google Safe Browsing check
            google_key = os.environ.get(
                "GOOGLE_SAFE_BROWSING_API_KEY"
            )

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
                                "threatEntries": [
                                    {"url": url}
                                ]
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
                    errors.append(
                        "Google Safe Browsing check failed."
                    )

            else:
                errors.append(
                    "Google Safe Browsing key is not configured."
                )

            # Final verdict
            if any(item["match"] for item in results):
                self.send_json(200, {
                    "verdict": "LIKELY_MALICIOUS",
                    "message": (
                        "At least one source reported a known threat."
                    ),
                    "domain_status": domain_status,
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
                    "domain_status": domain_status,
                    "sources": results,
                    "warnings": []
                })

            else:
                self.send_json(503, {
                    "verdict": "CHECK_UNAVAILABLE",
                    "message": (
                        "Unable to verify the URL using both sources."
                    ),
                    "domain_status": domain_status,
                    "sources": results,
                    "warnings": errors
                })

        except (ValueError, json.JSONDecodeError):
            self.send_json(400, {
                "error": "Invalid JSON"
            })

        except Exception as e:
            print("Unexpected server error:", repr(e))
            self.send_json(500, {
                "error": "Unexpected server error"
            })


if __name__ == "__main__":
    port = int(os.environ.get("PORT", "8000"))

    server = HTTPServer(
        ("0.0.0.0", port),
        CyberLabHandler
    )

    print("ScamShield server running on port", port)

    server.serve_forever()
