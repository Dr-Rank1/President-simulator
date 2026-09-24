# President Simulator

President Simulator is an Android grand strategy game built with Kotlin and Jetpack Compose. Lead a nation through monthly turns by balancing the economy, public support, security, diplomacy, and political stability.

## Features

- **National leadership:** Manage the treasury, taxes, production, infrastructure, research, laws, trade, and military.
- **Political systems:** Appoint a cabinet, respond to scandals, negotiate with opposition parties, manage press coverage, and maintain public support.
- **Diplomacy and security:** Build relations with rival nations, negotiate agreements, respond to threats, conduct intelligence operations, and manage wars.
- **Campaign scenarios:** Choose from scenarios with distinct objectives and starting conditions, then track progress from the dashboard.
- **Challenge rules:** Select optional rules such as Austerity Mandate, Hostile Press, or Snap Election. Challenges modify the starting situation and the campaign score multiplier.
- **Political story arcs:** Make choices across multi-month stories, including cabinet scandals and confidence votes. Decisions affect the press, cabinet cohesion, opposition pressure, and the legacy record.
- **Campaign briefings and reports:** Review a state-aware chief-of-staff outlook, active story progress, legacy pillar scores, and earned honors.
- **Regional relationship map:** See the nation's relationships with neighboring powers from the dashboard.
- **Monthly simulation:** Advance turns manually or use the time controls. Crises can pause the simulation until a decision is made.
- **Save and audio settings:** Save and restore campaign state, with background music and sound effects controls.

## Requirements

- Android Studio with Android SDK 35
- JDK 17
- Android device or emulator running Android 8.0 (API 26) or newer
- A configured Android SDK path (Android Studio can create `local.properties` automatically)

## Build and run

Open the project in Android Studio, allow Gradle sync to finish, and run the `app` configuration on a device or emulator.

The repository includes the Gradle wrapper JAR and Windows batch launcher. On Windows, build the debug APK with:

```powershell
.\gradlew.bat assembleDebug
```

On Linux or macOS, run the wrapper through the checked-in JAR:

```bash
java -classpath gradle/wrapper/gradle-wrapper.jar \
  org.gradle.wrapper.GradleWrapperMain assembleDebug
```

To run the unit tests, replace `assembleDebug` with `testDebugUnitTest`. The debug APK is written to `app/build/outputs/apk/debug/`.

## Project structure

```text
app/src/main/java/com/presidentsimulator/game/
├── data/          # Game state, scenario definitions, and simulation systems
├── viewmodel/     # Campaign state, player actions, and monthly turn pipeline
├── ui/
│   ├── components/ # Shared HUD elements, cards, and dialogs
│   ├── navigation/ # App navigation and campaign flow
│   ├── screens/    # Dashboard and ministry screens
│   └── theme/      # Colors, dimensions, icons, and Compose theme
└── audio/          # Background music and sound effects

app/src/test/       # Unit tests for campaign systems
app-overview/       # Architecture and codebase reference notes
```

## License

Private project. Contact the repository owner for licensing terms and permissions.
