with open("app/src/main/java/com/presidentsimulator/game/ui/components/GameTutorialDialog.kt", "r") as f:
    content = f.read()

import re

# Add imports for dialog properties
if "import androidx.compose.ui.Gravity" not in content:
    content = content.replace("import androidx.compose.ui.window.DialogProperties", "import androidx.compose.ui.window.DialogProperties\nimport androidx.compose.ui.window.DialogWindowProvider\nimport android.view.Gravity")

new_pages = """private val tutorialPages = listOf(
    TutorialPage(
        "Welcome, President",
        "Your command center brings together the country’s finances, approval, stability, current risks, and campaign priorities.",
        "Check this dashboard daily to know exactly what needs your immediate attention.",
        Icons.Default.AccountBalance,
    ),
    TutorialPage(
        "Economic Mastery",
        "The Economy tab allows you to adjust tax rates across classes, fund infrastructure, and balance the budget.",
        "High taxes increase revenue but hurt approval and economic growth. Balance is key to survival.",
        Icons.Default.AccountBalance,
    ),
    TutorialPage(
        "Military & Defense",
        "Maintain your army, navy, and airforce. A strong military deters invaders and keeps order during instability.",
        "Funding the military is expensive. Only raise spending if you anticipate conflict or need to suppress a rebellion.",
        Icons.Default.Groups,
    ),
    TutorialPage(
        "Global Diplomacy",
        "You are not alone in the world. The UN and neighboring countries will react to your aggressive or peaceful actions.",
        "Forming alliances provides trade benefits and mutual defense, but can drag you into foreign wars.",
        Icons.Default.Groups,
    ),
    TutorialPage(
        "Passing Laws",
        "Use the Parliament/Congress to pass sweeping reforms. Laws define your government's stance on rights and security.",
        "If you lack support, you may have to bribe officials or use executive orders, which risk public backlash.",
        Icons.Default.AccountBalance,
    ),
    TutorialPage(
        "Advance Time Carefully",
        "The controls at the top pause, resume, or speed up the simulation. Monthly turns update all statistics.",
        "Always resolve urgent red alerts before advancing time, or you may face a crisis.",
        Icons.Default.PlayArrow,
    )
)"""

content = re.sub(r"private val tutorialPages = listOf\([\s\S]*?\n\)", new_pages, content)

# Modify the dialog to be at the bottom by wrapping the content in a Box and adjusting DialogProperties
dialog_start = content.find("Dialog(")
dialog_end = content.find("    ) {", dialog_start) + 7

new_dialog = """Dialog(
        onDismissRequest = onSkip,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        val window = (androidx.compose.ui.platform.LocalView.current.parent as? DialogWindowProvider)?.window
        window?.let {
            it.setGravity(Gravity.BOTTOM)
            it.setDimAmount(0.1f) // very light dim so we don't overshadow the UI
        }
        
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomCenter) {"""

content = content[:dialog_start] + new_dialog + content[dialog_end:]
content = content.replace("            Row(verticalAlignment = Alignment.CenterVertically) {", "        Column(\n            modifier = Modifier.fillMaxWidth().clip(NssCardShape)\n                .background(Color(0xE6050A0F)).border(1.dp, NssBorder, NssCardShape).padding(22.dp),\n        ) {\n            Row(verticalAlignment = Alignment.CenterVertically) {")

# we added a Box, so we need to add another closing brace at the end of GameTutorialDialog
content = content + "\n    }\n"
# and we need to remove the old Column modifier since we added a new one.
# Wait, let's just replace the exact Column block
old_column = """        Column(
            modifier = Modifier.fillMaxWidth(0.9f).clip(NssCardShape)
                .background(NssGameCard).padding(22.dp),
        ) {"""
content = content.replace(old_column, "")

if "import androidx.compose.ui.graphics.Color" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport androidx.compose.ui.graphics.Color")
if "import androidx.compose.foundation.layout.Box" not in content:
    content = content.replace("import androidx.compose.foundation.layout.Column", "import androidx.compose.foundation.layout.Column\nimport androidx.compose.foundation.layout.Box\nimport androidx.compose.foundation.layout.fillMaxSize")

with open("app/src/main/java/com/presidentsimulator/game/ui/components/GameTutorialDialog.kt", "w") as f:
    f.write(content)
