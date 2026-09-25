import re

with open("app/src/main/java/com/presidentsimulator/game/ui/components/NssComponents.kt", "r") as f:
    nss = f.read()

nss = nss.replace("fun InteractiveWorldMap(\n    modifier: Modifier = Modifier,\n    modifier: Modifier = Modifier,", "fun InteractiveWorldMap(\n    modifier: Modifier = Modifier,")

with open("app/src/main/java/com/presidentsimulator/game/ui/components/NssComponents.kt", "w") as f:
    f.write(nss)

with open("app/src/main/java/com/presidentsimulator/game/ui/navigation/GameNavigation.kt", "r") as f:
    nav = f.read()

if "import androidx.compose.foundation.layout.Box" not in nav:
    nav = nav.replace("import androidx.compose.foundation.layout.Row\n", "import androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.layout.Box\n")

with open("app/src/main/java/com/presidentsimulator/game/ui/navigation/GameNavigation.kt", "w") as f:
    f.write(nav)

with open("app/src/main/java/com/presidentsimulator/game/ui/screens/MainDashboardScreen.kt", "r") as f:
    main = f.read()

if "import androidx.compose.ui.graphics.Color" not in main:
    main = main.replace("package com.presidentsimulator.game.ui.screens\n", "package com.presidentsimulator.game.ui.screens\n\nimport androidx.compose.ui.graphics.Color\n")

with open("app/src/main/java/com/presidentsimulator/game/ui/screens/MainDashboardScreen.kt", "w") as f:
    f.write(main)

