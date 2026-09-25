with open("app/src/main/java/com/presidentsimulator/game/ui/components/NssComponents.kt", "a") as f:
    f.write("""

fun formatMa2Money(amount: Long): String = formatCompactMoney(amount)

fun collectAlertCount(state: com.presidentsimulator.game.data.GameState): Int {
    var count = 0
    if (state.diplomacy.activeWar != null) count++
    if (state.internalSecurity.coupRisk >= 60f) count++
    if (state.internalSecurity.instabilityScore >= 50f) count++
    if (state.production.foodShortage) count++
    if (state.production.energyShortage) count++
    if (state.governance.activeResolution != null) count++
    if (state.gameOver.isGameOver) count++
    return count.coerceAtLeast(if (count == 0) 1 else count)
}
""")

import os
def add_import(filepath, imp):
    with open(filepath, "r") as f:
        content = f.read()
    if imp not in content:
        content = content.replace("import androidx.", f"{imp}\nimport androidx.", 1)
        with open(filepath, "w") as f:
            f.write(content)

add_import("app/src/main/java/com/presidentsimulator/game/ui/navigation/GameNavigation.kt", "import com.presidentsimulator.game.ui.components.collectAlertCount")
add_import("app/src/main/java/com/presidentsimulator/game/ui/screens/EconomyScreen.kt", "import com.presidentsimulator.game.ui.components.formatMa2Money")
add_import("app/src/main/java/com/presidentsimulator/game/ui/screens/MilitaryScreen.kt", "import com.presidentsimulator.game.ui.components.formatMa2Money")
