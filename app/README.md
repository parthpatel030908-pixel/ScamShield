# 🛡️ ScamShield — Android Scam Link Scanner & Cybersecurity Lab

ScamShield is an Android application built with Kotlin and Jetpack Compose. It combines URL threat checking with beginner-friendly cybersecurity learning modules.

The project is designed to demonstrate practical Android development, API integration, network communication, and basic security awareness.

## 🚀 Features

### 1. Link Scanner
- Scan HTTP and HTTPS URLs.
- Check URLs using URLhaus and Google Safe Browsing.
- Display threat verdicts and source results.
- Show scan reliability status.
- Explain warnings and limitations.
- Validate URL input before scanning.

### 2. Scan History
- Save previous scan records.
- View scan timestamps and detailed results.
- Copy scanned URLs.
- Delete individual records.
- Clear all scan history.
- Preserve scan history after restarting the app.

### 3. Link Sharing
- Share a URL from another Android app directly to ScamShield.
- Automatically place the shared URL in the scanner.

### 4. Cybersecurity Learning Lab
- Networking Basics
- Android Security
- Kali Linux Lab
- Network Tools
- Network Diagnostics
- Lab Activity Log

### 5. Server Connectivity
- Connect to a locally hosted Kali Linux backend.
- Check server availability.
- Display HTTP status and response time.
- View diagnostic results.

## 🧰 Technologies Used

- Kotlin
- Android Studio
- Jetpack Compose
- Python
- Kali Linux
- HTTP/JSON APIs
- URLhaus
- Google Safe Browsing
- SharedPreferences

## 🏗️ Architecture

**Android Application → Python Backend → Threat Intelligence APIs**

The Android application sends a URL to the Python backend. The backend checks the URL against supported threat-intelligence sources and returns a structured result to the application.

## ⚠️ Important Security Limitations

- A result of "NO KNOWN THREAT FOUND" does not guarantee that a URL is safe.
- Threat databases may not contain newly created or unreported malicious links.
- Results depend on the availability and responses of external services.
- ScamShield is an educational project and should not be treated as a replacement for professional security software.
- API credentials are kept on the backend and should never be embedded in the Android application.

## 🎯 Project Objective

To develop a practical Android cybersecurity application that demonstrates URL threat intelligence integration, network communication, and cybersecurity education.

## 👨‍💻 Developer

**Parth Bhut**

First-Year Computer Engineering Student  
Interested in Cybersecurity and Ethical Hacking.

---

*Built as a learning and portfolio project.*