# 🛡️ ScamShield — Android Scam Link Scanner & Cybersecurity Learning Lab

![ScamShield Icon](assets/scamshield-icon.png)

**Scan • Learn • Stay Safe**

ScamShield is an Android cybersecurity application developed using Kotlin and Jetpack Compose. It combines multi-source URL threat checking, scan management, security reports, and beginner-friendly cybersecurity learning modules in one application.

The project demonstrates practical Android development, REST API integration, backend communication, URL analysis, and cybersecurity awareness.

---

## 📸 Application Screenshots

### 1. ScamShield Dashboard
![ScamShield Dashboard](screenshots/Screenshot%201.png)

### 2. Cybersecurity Learning Lab
![Learning Lab Modules](screenshots/Screenshot%202.png)

### 3. Link Scanner Interface
![Link Scanner Interface](screenshots/Screenshot%203.png)

### 4. URL Scanning and Threat Analysis
![URL Scanning and Threat Analysis](screenshots/Screenshot%204.png)

### 5. Scan History
![Scan History](screenshots/Screenshot%205.png)

---

## 🚀 Features

### 1. Link Scanner & Threat Detection
- Scan HTTP and HTTPS URLs.
- Check URLs using URLhaus and Google Safe Browsing.
- Display threat verdicts and source-wise results.
- Show scan reliability information.
- Explain warnings and detection limitations.
- Validate URL input before scanning.
- Identify certain invalid or unreachable domains.

### 2. Scan History Management
- Save previous scan records.
- View scan timestamps and detailed results.
- Search and filter scan history.
- Copy scanned URLs.
- Delete individual records.
- Clear all scan history.
- Preserve scan history after restarting the application.

### 3. Link Sharing & Clipboard Scanning
- Share URLs from other Android applications directly to ScamShield.
- Automatically place shared URLs into the scanner.
- Scan URLs copied to the clipboard.

### 4. Advanced URL Analysis
- Local URL risk analysis using heuristic checks.
- Duplicate URL detection.
- Save frequently used URLs as bookmarks.
- Add personal notes to saved scan records.
- View detailed scan information and source evidence.

### 5. Security Reports & Analytics
- Export scan history as CSV.
- Generate PDF scan reports.
- Share scan reports.
- View scan statistics.
- Explore seven-day scan analytics.

### 6. Cybersecurity Learning Lab
- Networking Basics
- Android Security
- Kali Linux Lab
- Network Tools
- Network Diagnostics
- Lab Activity Log

### 7. Interactive Cybersecurity Tools
- Cybersecurity Knowledge Quiz.
- Searchable Cybersecurity Glossary.
- Privacy, Safety & App Information Centre.

### 8. Server Connectivity
- Connect to the deployed Python backend.
- Check backend availability.
- Display server connection status.
- View HTTP status and response time.
- Perform network diagnostics.

---

## 🧰 Technologies Used

| Technology | Purpose |
|---|---|
| Kotlin | Android application development |
| Android Studio | Development environment |
| Jetpack Compose | User interface |
| Python | Backend development |
| REST APIs | Client-server communication |
| URLhaus | Malicious URL intelligence |
| Google Safe Browsing | Threat detection |
| SharedPreferences | Local data persistence |
| Android PdfDocument | PDF report generation |
| Render | Backend deployment |

---

## 🏗️ System Architecture

**Android Application → Python Backend → Threat Intelligence APIs**

1. The user enters, shares, or selects a URL in ScamShield.
2. The Android application sends the URL to the Python backend.
3. The backend queries supported threat-intelligence services.
4. The backend processes the available responses.
5. The application displays the verdict, source results, and relevant warnings.

### Backend Information

**Deployment Platform:** Render

**Backend URL:**  
https://scamshield-1-d2jy.onrender.com

**API Endpoints:**

- `GET /health` — Checks backend availability.
- `POST /check-url` — Processes URL threat-checking requests.

---

## ⚠️ Security Limitations

- A result of `NO_KNOWN_THREAT_FOUND` does not guarantee that a URL is safe.
- Threat databases may not contain newly created or unreported malicious links.
- Detection depends on the availability and responses of external threat-intelligence services.
- Local URL risk analysis is heuristic-based and is not equivalent to verified threat intelligence.
- ScamShield is an educational project and should not be treated as a replacement for professional security software.
- API credentials are managed on the backend and should never be embedded in the Android application or committed to a public repository.

**Important:** Always exercise caution before opening unfamiliar links, even when a scanner reports no known threat.

---

## 🎯 Project Objective

To develop a practical Android cybersecurity application that demonstrates URL threat-intelligence integration, backend communication, scan management, security reporting, and interactive cybersecurity education.

---

## 🔧 Getting Started

### Prerequisites
- Android Studio
- Android SDK
- Kotlin support
- Internet connection

### Setup

1. Clone this repository:

   `git clone https://github.com/parthpatel030908-pixel/ScamShield.git`

2. Open the project in Android Studio.
3. Allow Gradle synchronization to complete.
4. Build and run the application on an Android emulator or compatible device.

The application uses the deployed backend for URL scanning.

---

## 👨‍💻 Developer

**Parth Bhut**

First-Year Computer Engineering Student  
Government Engineering College, Bhavnagar

Interested in Cybersecurity, Android Development, and Ethical Hacking.

---

## 📌 Project Status

**Version:** 1.0

**Status:** Initial project development completed.

Built as a learning and portfolio project to explore practical Android development and cybersecurity concepts.

---

*Scan • Learn • Stay Safe — ScamShield*
