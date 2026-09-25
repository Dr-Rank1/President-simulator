with open("app/src/main/java/com/presidentsimulator/game/ui/screens/LawsScreen.kt", "r") as f:
    content = f.read()

# Add context import
if "import android.widget.Toast" not in content:
    content = content.replace("import androidx.compose.ui.Alignment", "import androidx.compose.ui.Alignment\nimport android.widget.Toast\nimport androidx.compose.ui.platform.LocalContext")

# For OppositionTab:
old_opp = """fun OppositionTab(
    state: GameState,
    viewModel: GameViewModel,
) {"""
new_opp = """fun OppositionTab(
    state: GameState,
    viewModel: GameViewModel,
) {
    val context = LocalContext.current"""
content = content.replace(old_opp, new_opp)

# Add toasts to OppositionTab
content = content.replace("onClick = { viewModel.negotiateWithOpposition() }", "onClick = { viewModel.negotiateWithOpposition(); Toast.makeText(context, \"Negotiating...\", Toast.LENGTH_SHORT).show() }")
content = content.replace("onClick = { viewModel.smearOpposition() }", "onClick = { viewModel.smearOpposition(); Toast.makeText(context, \"Smear campaign started.\", Toast.LENGTH_SHORT).show() }")
content = content.replace("onClick = { viewModel.concedeToOpposition() }", "onClick = { viewModel.concedeToOpposition(); Toast.makeText(context, \"Concessions made.\", Toast.LENGTH_SHORT).show() }")

# For IdeologyTab:
old_ideo = """fun IdeologyTab(
    state: GameState,
    viewModel: GameViewModel,
) {"""
new_ideo = """fun IdeologyTab(
    state: GameState,
    viewModel: GameViewModel,
) {
    val context = LocalContext.current"""
content = content.replace(old_ideo, new_ideo)
content = content.replace("onClick = { viewModel.setIdeology(ideology) }", "onClick = { viewModel.setIdeology(ideology); Toast.makeText(context, \"Ideology shifted to ${ideology.displayName}.\", Toast.LENGTH_SHORT).show() }")

# For ReligiousTab:
old_rel = """fun ReligiousTab(
    state: GameState,
    viewModel: GameViewModel,
) {"""
new_rel = """fun ReligiousTab(
    state: GameState,
    viewModel: GameViewModel,
) {
    val context = LocalContext.current"""
content = content.replace(old_rel, new_rel)
content = content.replace("onClick = { viewModel.changeStateReligion(religion) }", "onClick = { viewModel.changeStateReligion(religion); Toast.makeText(context, \"State Religion changed to ${religion.displayName}.\", Toast.LENGTH_SHORT).show() }")

with open("app/src/main/java/com/presidentsimulator/game/ui/screens/LawsScreen.kt", "w") as f:
    f.write(content)
