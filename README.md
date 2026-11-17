# EnterSend

An Android accessibility app for GrapheneOS "Messaging" that automatically sends a message when you press Enter. The accessibility service is only active when the GrapheneOS "Messaging" app is open.

## Backstory

I wrote this app because I was using `scrcpy` to use my PC keyboard to type in and send text messages on my GrapheneOS phone. However, the Enter key just inserted carriage returns into the text, rather than sending the text message. EnterSend intercepts the Enter key press and automatically clicks the "Send Message" button, making it seamless to type and send messages using a physical keyboard.

## Features

- **Send on Enter:** Automatically clicks the send button when you press Enter.
- **Targeted:** Only active when GrapheneOS "Messaging" (or AOSP Messaging) is open.
- **Lightweight:** Minimal footprint accessibility service.
- **Compose-aware:** Handles Jetpack Compose UI trees where the send button label sits on a non-clickable leaf node.

## Building

### Prerequisites
- JDK 17
- Android SDK (API 34)
- Gradle 8.2

### Build Instructions
```bash
export JAVA_HOME=/path/to/jdk17
export ANDROID_HOME=/path/to/android-sdk
./gradlew assembleDebug
```

## Installation
1. Build the APK or download it from releases.
2. Install via ADB: `adb install -r app-debug.apk`
3. Open Android Settings → Accessibility → EnterSend and enable the service.

## License
This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.