import re

with open("app/src/main/java/com/presidentsimulator/game/ui/components/GameTutorialDialog.kt", "r") as f:
    content = f.read()

# Fix the broken GameTutorialDialog signature
# Find the line "@Composable\nfun GameTutorialDialog("
start_idx = content.find("@Composable\nfun GameTutorialDialog(")
end_idx = content.find("val window =", start_idx) - 1

# Replace the broken part with the correct signature and Dialog wrapper
correct_signature = """@Composable
fun GameTutorialDialog(
    page: Int,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit,
) {
    val current = tutorialPages[page.coerceIn(tutorialPages.indices)]

    Dialog(
        onDismissRequest = onSkip,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        """

content = content[:start_idx] + correct_signature + content[end_idx+1:]

with open("app/src/main/java/com/presidentsimulator/game/ui/components/GameTutorialDialog.kt", "w") as f:
    f.write(content)
