# 🛡️ ScamShield

### Scan. Learn. Stay Safe.

**ScamShield** is an Android-based cybersecurity application designed to help users identify known URL threats, understand online risks, maintain scan records, and learn essential cybersecurity concepts.

It combines multi-source URL threat checking with an interactive cybersecurity learning environment.

---

## 📱 Application Screenshots

## 📸 Screenshots

### 1. ScamShield Dashboard
![ScamShield Dashboard](./screenshots/screenshot%21.png)

### 2. Learning Lab Modules
![Learning Lab Modules](./screenshots/screenshot%22.png)

### 3. Link Scanner Interface
![Link Scanner Interface](./screenshots/screenshot%23.png)

### 4. URL Scanning and Threat Analysis
![URL Scanning and Threat Analysis](./screenshots/screenshot%24.png)

### 5. Scan History
![Scan History](./screenshots/screenshot%25.png)

---

## ✨ Key Features

ScamShield includes 20 features across protection, cybersecurity education, network diagnostics and advanced security tools.

### 🔍 URL Protection
- Multi-source URL threat checking.
- URLhaus integration.
- Google Safe Browsing integration.
- URL validation.
- Threat verdicts with source evidence.
- Share-to-scan functionality.
- Scan history with search and filters.
- Individual and bulk history deletion.

### 📊 Reports and Analytics
- Scan statistics.
- Seven-day scan analytics.
- CSV scan history export.
- PDF scan report generation.
- Shareable scan reports.

### 🧪 Advanced Security Centre
- Advanced scan details.
- Local URL risk analysis using heuristics.
- Duplicate URL detection.
- Saved URLs and bookmarks.
- Personal scan notes.
- Clipboard URL scanner.
- Cybersecurity knowledge quiz.
- Searchable cybersecurity glossary.
- Privacy and app information centre.

### 📚 Cybersecurity Learning Lab
- Networking fundamentals.
- Android security concepts.
- Kali Linux learning lab.
- Network tools.
- Network diagnostics.
- Lab activity log.

---

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Kotlin | Android development |
| Jetpack Compose | Modern Android UI |
| Python | Backend development |
| Render | Backend hosting |
| URLhaus | URL threat intelligence |
| Google Safe Browsing | URL threat checking |
| SharedPreferences | Local data persistence |
| Android PdfDocument | PDF generation |

---

## 🏗️ Application Architecture

ScamShield follows a client-server architecture.

1. The user submits a URL through the Android application.
2. The application validates the URL format.
3. The URL is sent to the Python backend.
4. The backend queries the configured threat-intelligence sources.
5. The backend returns a structured response.
6. The Android application displays the verdict, source evidence and warnings.
7. Scan records and selected user data are stored locally.

---

## 🌐 Backend API

**Backend URL:**

https://scamshield-1-d2jy.onrender.com

| Endpoint | Purpose |
|---|---|
| `/health` | Checks backend availability |
| `/check-url` | Performs URL threat checking |

The backend uses configured threat-intelligence services to evaluate submitted URLs.

---

## 🔐 Security Notes and Limitations

- A `NO_KNOWN_THREAT_FOUND` result does not guarantee that a URL is safe.
- Newly created or unreported malicious URLs may not appear in threat databases.
- Local URL risk analysis is heuristic and should not be treated as definitive detection.
- Third-party service availability can affect scan results.
- API credentials are kept on the backend and should never be committed to the public repository.
- ScamShield is an educational security application, not a replacement for professional security software.

---

## 🚀 Getting Started

### Requirements
- Android Studio
- Android SDK
- Kotlin support
- Internet connection
- Compatible Android device or emulator

### Installation

1. Clone this repository.
2. Open the project in Android Studio.
3. Allow Gradle synchronization to complete.
4. Build and run the application on an Android device or emulator.

Online URL checking requires the backend to be available.

---

## 🎯 Project Objective

The objective of ScamShield is to make basic cybersecurity awareness more accessible by combining URL threat checking, scan management and interactive learning tools in one Android application.

---

## 👨‍💻 Developer

**Parth Bhut**

First-Year Computer Engineering Student  
Government Engineering College, Bhavnagar

**Interests:** Cybersecurity | Android Development | Technology

---

## 📌 Project Status

**Version:** 1.0  
**Status:** Initial project development completed

---

*ScamShield — Secure Today. Safer Tomorrow.*
