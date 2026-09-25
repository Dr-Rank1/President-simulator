import re

with open("app/src/main/java/com/presidentsimulator/game/ui/components/NssComponents.kt", "r") as f:
    nss = f.read()

if "import androidx.compose.foundation.layout.fillMaxSize" not in nss:
    nss = nss.replace("import androidx.compose.foundation.layout.Box\n", "import androidx.compose.foundation.layout.Box\nimport androidx.compose.foundation.layout.fillMaxSize\n")

with open("app/src/main/java/com/presidentsimulator/game/ui/components/NssComponents.kt", "w") as f:
    f.write(nss)

with open("app/src/main/java/com/presidentsimulator/game/ui/navigation/GameNavigation.kt", "r") as f:
    nav = f.read()

nav = nav.replace(
    "            NavHost(\n                navController = navController,\n                startDestination = GameDestination.Dashboard.route,\n                modifier = Modifier.weight(1f).fillMaxHeight(),",
    "            NavHost(\n                navController = navController,\n                startDestination = GameDestination.Dashboard.route,\n                modifier = Modifier.fillMaxSize(),"
)

with open("app/src/main/java/com/presidentsimulator/game/ui/navigation/GameNavigation.kt", "w") as f:
    f.write(nav)

