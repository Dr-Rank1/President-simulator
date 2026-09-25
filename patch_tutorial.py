with open("app/src/main/java/com/presidentsimulator/game/ui/components/GameTutorialDialog.kt", "r") as f:
    content = f.read()

# Remove dimming completely
content = content.replace("it.setDimAmount(0.1f)", "it.setDimAmount(0.0f) // Removed dimming so it doesn't overshadow")
content = content.replace("it.setDimAmount(0.3f)", "it.setDimAmount(0.0f)")

# Make it smaller width
content = content.replace("modifier = Modifier.fillMaxWidth().clip(NssCardShape)", "modifier = Modifier.fillMaxWidth(0.85f).clip(NssCardShape)")

# Make the text more instructional and less generic
new_pages = """private val tutorialPages = listOf(
    TutorialPage(
        "Welcome, President",
        "This is your command dashboard. From here you monitor your nation's vitals: Approval, Budget, and Stability.",
        "Your first goal is to balance the budget. Check the Economy tab next.",
        Icons.Default.AccountBalance,
    ),
    TutorialPage(
        "Managing the Economy",
        "Use the Economy tab to adjust taxes and allocate funds. A high GDP growth means more revenue.",
        "Warning: High taxes will crash your approval rating. Find the sweet spot.",
        Icons.Default.AccountBalance,
    ),
    TutorialPage(
        "National Defense",
        "The Military tab is where you recruit soldiers and invest in defense technology.",
        "A weak military invites foreign invasions. Keep it strong, but don't bankrupt the country.",
        Icons.Default.Groups,
    ),
    TutorialPage(
        "Global Diplomacy",
        "The Foreign Affairs tab lets you form alliances, sign trade deals, or declare wars.",
        "Build relationships before declaring war, or you will face crippling UN sanctions.",
        Icons.Default.Groups,
    ),
    TutorialPage(
        "Passing Laws",
        "Your parliament is waiting. Pass laws to shape your country's ideology and address crises.",
        "Some laws require high political capital. Check your mandate progress to gain capital.",
        Icons.Default.AccountBalance,
    ),
    TutorialPage(
        "Advancing Time",
        "The simulation is turn-based. Tap the Next Month button at the top to advance time.",
        "Watch out for random events and crises that pop up between turns!",
        Icons.Default.PlayArrow,
    )
)"""

import re
content = re.sub(r'private val tutorialPages = listOf\([\s\S]*?\n\)', new_pages, content)

with open("app/src/main/java/com/presidentsimulator/game/ui/components/GameTutorialDialog.kt", "w") as f:
    f.write(content)

