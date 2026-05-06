# Vibe - Ndejje University Event Management Platform

Vibe is a modern Android application designed for the Ndejje University community to discover, manage, and book events. This project follows the MVVM architecture and is built entirely using Jetpack Compose and Kotlin.

## 👥 Development Team & Roles

As per the **NDEJJE UNIVERSITY CAPSTONE PROJECT BRIEF**, the following team members are responsible for the project:

| Name | Role | Registration Number | Primary Responsibilities |
| :--- | :--- | :--- | :--- |
| **LUBEGA BECKHAM JUSPER** | Lead Developer | 24/2/306/D/184 | Core architecture, Jetpack Compose structure, and technical decision-making. |
| **NYOMBI ABUBAKER** | UI/UX Specialist | 24/2/314/D/002 | Material 3 styling, component design, and user flow prototyping. |
| **WASSWA CALVIN** | Software Developer | 24/2/306/W/180 | Database logic, business logic implementation, and core feature development. |
| **TAYEBWA RONALD** | Testing and QA Engineer | 25/2/314/D/3263 | Unit/Integration testing, bug documentation, and functional verification. |
| **KWAGALA DEBORAH** | Documentation & Research | 24/2/314/D/716 | Community problem analysis, technical README, and project reporting. |

## 🛠 Technical Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material 3
- **Architecture:** MVVM (Model-View-ViewModel)
- **Database:** Room (Local Data Persistence)
- **Navigation:** Jetpack Compose Navigation

## 🧪 Testing Summary (QA Engineer)
The following core functions have been verified through automated unit tests:

1.  **Authentication Logic**: Verified successful user login and signup state transitions in `AuthViewModel`.
2.  **Data Persistence**: Verified CRUD operations for events in `EventRepository` using Room.

## 📜 Compliance Features
- **Rule 1 (camelCase)**: Enforced throughout the project for all variables and functions.
- **Rule 2 (StringResources)**: All UI text extracted to `strings.xml`. No hardcoded strings in Composable files.
- **Rule 3 (dimens.xml)**: Dimensions and spacing extracted to `dimens.xml`.
- **Rule 4 (MainActivity)**: Strictly serves as an entry point with logic delegated to `MainViewModel`.

---
© 2026 Ndejje University - Faculty of Science and Computing

https://youtu.be/S57garGLuXs?si=OOe4CMbrb_cOfGQj