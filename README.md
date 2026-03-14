# Touch Lock App

This is a lightweight Android application that locks touch input on the screen while allowing the content behind it to remain visible.

## Features
- Full-screen transparent overlay blocks all touch events.
- Triple-tap anywhere on the screen within 2 seconds to unlock.
- Foreground service ensures it remains active until explicitly stopped.
- Handles `SYSTEM_ALERT_WINDOW` permission seamlessly.

## Build and APK Generation Instructions

### Using Android Studio
1. Open Android Studio.
2. Select **File > Open**, and navigate to this `TouchLockApp` directory.
3. Wait for Android Studio to sync the project with Gradle files (it will download Gradle wrapper automatically).
4. Click the **Run 'app'** button (Play icon / Shift+F10) to build and install the debug APK on a connected device or emulator.

### Generating a Release APK
1. In Android Studio, go to **Build > Generate Signed Bundle / APK...**
2. Choose **APK** and click Next.
3. Create a new Key Store or select an existing one.
4. Select the `release` build variant and click Finish.
5. The generated APK will be found in `app/release/`.
