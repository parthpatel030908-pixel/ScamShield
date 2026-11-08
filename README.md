# 🛡️ ScamShield — Android Scam Link Scanner & Cybersecurity Learning Lab

<p align="center">
  <img src="app/src/main/java/com/example/mobilesecuritylab/assets/scamshieldicon2.png" width="180" alt="ScamShield App Icon">
</p>

<p align="center">
  <b>SCAN • LEARN • STAY SAFE</b>
</p>

ScamShield is an Android application built using **Kotlin and Jetpack Compose**. It combines multi-source URL threat checking with beginner-friendly cybersecurity learning modules.

The project demonstrates practical Android development, API integration, backend communication, local data storage, and cybersecurity awareness.

---

## 📱 Screenshots

<p align="center">
  <img src="./screenshots/Screenshot%201.png" width="250" alt="ScamShield Dashboard">
  <img src="./screenshots/Screenshot%202.png" width="250" alt="ScamShield Learning Lab">
</p>

<p align="center">
  <img src="./screenshots/Screenshot%203.png" width="250" alt="ScamShield Link Scanner">
  <img src="./screenshots/Screenshot%204.png" width="250" alt="ScamShield Scan History">
  <img src="./screenshots/Screenshot%205.png" width="250" alt="ScamShield Advanced Features">
</p>

---

## 🚀 Features

### 1. Link Protection & Scanning
- URL validation and scanning.
- Multi-source threat intelligence using URLhaus and Google Safe Browsing.
- Local URL risk analysis using heuristic checks.
- Detailed scan verdicts and source information.
- Clipboard URL scanning.
- Share-to-scan functionality.
- Duplicate URL detection.

### 2. Scan History & Reports
- Persistent scan history.
- Search and filter scan records.
- Individual scan record deletion.
- Clear all scan history.
- Advanced scan details.
- Export scan history as CSV.
- Generate PDF scan reports.
- Share scan reports.
- Scan statistics and 7-day analytics.

### 3. Saved URLs & Personal Notes
- Save URLs for future reference.
- Add personal notes to scanned URLs.

### 4. Cybersecurity Learning Lab
- Networking Basics.
- Android Security.
- Kali Linux Lab.
- Network Tools.
- Network Diagnostics.
- Lab Activity Log.

### 5. Learning & Awareness
- Interactive cybersecurity knowledge quiz.
- Searchable cybersecurity glossary.
- Privacy, Safety & App Information Centre.

### 6. Backend Connectivity
- Live backend health monitoring.
- REST API communication.
- Multi-source threat intelligence integration.

---

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Kotlin | Android application development |
| Jetpack Compose | Modern UI development |
| Android Studio | Development environment |
| Python | Backend development |
| REST APIs | Client-server communication |
| URLhaus | Malicious URL intelligence |
| Google Safe Browsing | Threat detection |
| SharedPreferences | Local data persistence |
| Android PdfDocument | PDF report generation |
| Render | Backend deployment |

---

## 🏗️ System Architecture

```text
        Android Application
                 |
                 |
          ScamShield UI
                 |
                 |
           Link Scanner
                 |
                 |
          Python Backend
                 |
          -----------------
          |               |
       URLhaus       Google Safe
                       Browsing
          |               |
          -----------------
                 |
                 |
          Scan Result
                 |
                 |
       Android Application
```

---

## 🌐 Backend Information

The ScamShield backend is deployed using Render.

**Backend URL:**

https://scamshield-1-d2jy.onrender.com

**Available endpoints:**

- `/health` — Checks backend connectivity.
- `/check-url` — Checks submitted URLs against supported threat intelligence sources.

---

## ⚠️ Important Security Notes & Limitations

- A `NO_KNOWN_THREAT_FOUND` result does not guarantee that a URL is safe.
- Threat intelligence databases may not contain every malicious website.
- Detection accuracy depends on the availability and coverage of external threat intelligence sources.
- Local URL risk analysis is heuristic-based and does not independently prove that a website is malicious.
- Backend API credentials are intended to remain on the server and must not be exposed in the Android application or public repository.
- ScamShield is an educational cybersecurity project and should not be treated as a complete security solution.

---

## 💻 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/parthpatel030908-pixel/ScamShield.git
```

### 2. Open the project

- Open Android Studio.
- Select **Open**.
- Choose the cloned ScamShield project folder.
- Allow Gradle to sync.

### 3. Run the application

- Connect an Android device or start an emulator.
- Click **Run** in Android Studio.
- Explore the ScamShield dashboard and its features.

---

## 🎯 Project Objectives

- Learn Android application development.
- Understand REST API integration.
- Explore URL threat intelligence services.
- Develop practical cybersecurity awareness.
- Build an integrated Android and Python backend project.

---

## 👨‍💻 Developer

**Parth Bhut**

First-Year Computer Engineering Student  
Government Engineering College, Bhavnagar

Interested in Cybersecurity, Android Development and Technology.

---

## 📌 Project Status

**Version:** 1.0

**Development Status:** Initial development completed.

Built as a first major Android cybersecurity project to explore practical application development and cybersecurity concepts.

---

<p align="center">
  <b>SCAMSHIELD — SCAN • LEARN • STAY SAFE</b>
</p>
