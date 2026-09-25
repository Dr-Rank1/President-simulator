with open("app/src/main/java/com/presidentsimulator/game/MainActivity.kt", "r") as f:
    content = f.read()

import re

# We need to add CompositionLocalProvider and LocalDensity
if "import androidx.compose.runtime.CompositionLocalProvider" not in content:
    content = content.replace("import androidx.compose.material3.Surface", "import androidx.compose.material3.Surface\nimport androidx.compose.runtime.CompositionLocalProvider\nimport androidx.compose.ui.platform.LocalDensity\nimport androidx.compose.ui.unit.Density")

new_content = """            PresidentSimulatorTheme {
                val currentDensity = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(currentDensity.density * 0.7f, currentDensity.fontScale * 0.7f)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(NssBackground),
                        color = NssBackground,
                    ) {
                        GameNavigation(viewModel = gameViewModel)
                    }
                }
            }"""

content = re.sub(r"PresidentSimulatorTheme\s*\{[\s\S]*?GameNavigation\(viewModel = gameViewModel\)\n\s*\}\n\s*\}", new_content, content)

with open("app/src/main/java/com/presidentsimulator/game/MainActivity.kt", "w") as f:
    f.write(content)
