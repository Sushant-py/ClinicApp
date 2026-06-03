Nasya ClinicApp
📖 About The Project

Nasya ClinicApp is a comprehensive Android-based mobile application designed for the management and monitoring of clinical trials specifically focused on "Nasya" nasal medication dosing. The application streamlines the clinical trial process by prioritizing participant safety, ensuring compliance through smart reminders, and digitizing the onboarding and consent workflows.

This system serves as a bridge between trial administrators and participants, enabling real-time health tracking, adverse event reporting, and secure data logging.

✨ Key Features

Clinical Eligibility Screening: Automated pre-screening checklists to qualify or disqualify participants based on strict study criteria.

eConsent Module: Digital, secure informed consent forms allowing participants to review trial guidelines and agree electronically.

Dose Management & Alarms: Automated scheduling and background alarm receivers to ensure participants take their Nasya medication at the exact prescribed intervals.

Daily Patient Diary: A user-friendly daily logging system for participants to record symptoms, well-being, and medication adherence.

ADR (Adverse Drug Reaction) Reporting: Critical safety module for the rapid logging and submission of any adverse side effects experienced during the trial.

Participant Monitoring Dashboard: An administrative view to track participant progress, dose records, and eligibility statuses.

Patient Education: Integrated video playback for instructional media, ensuring participants understand proper nasal dosage administration.

Multilingual Support: Dynamic language switching to accommodate diverse participant demographics.

🛠 Tech Stack
Platform: Android (Minimum SDK targeting modern devices)

Language: Java

UI/UX: XML Layouts

IDE: Android Studio

Build System: Gradle

📂 Architecture & Core Components

The application follows a standard Android Activity-based architecture, separated into logical modules:

EligibilityActivity / IneligibleActivity: Handles trial qualification logic.

ConsentActivity: Manages the eConsent workflow.

DailyDiaryActivity / AdrActivity: Patient-facing data collection interfaces.

DoseAlarmReceiver: Broadcast receiver for handling local dose notifications.

PatientActivity / MonitorActivity: Dashboards for profile and data review.

🚀 Getting Started
To get a local copy up and running, follow these simple steps.

Prerequisites
Android Studio (Latest stable version recommended)

Java Development Kit (JDK) 11 or higher

An Android Virtual Device (AVD) or a physical Android device for testing.

Installation
Clone the repository:

git clone https://github.com/sushant-py/clinicapp.git

2.  **Open the Project:**
    *   Launch Android Studio.
    *   Select **Open an existing Android Studio project**.
    *   Navigate to the cloned `clinicapp` directory and click **OK**.
3.  **Sync Gradle:**
    *   Allow Android Studio to download necessary dependencies and sync the Gradle files.
4.  **Run the App:**
    *   Select your target device or emulator from the run configurations drop-down.
    *   Click the **Run** or press `Shift + F10`.



Created by Siddhant Singh, Siddharth Nair and Sushant Sinha



