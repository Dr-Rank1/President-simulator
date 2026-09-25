with open("app/src/main/java/com/presidentsimulator/game/ui/screens/LawsScreen.kt", "r") as f:
    laws = f.read()

# Imports
if "import android.widget.Toast" not in laws:
    laws = laws.replace("import androidx.compose.ui.Alignment", "import androidx.compose.ui.Alignment\nimport android.widget.Toast\nimport androidx.compose.ui.platform.LocalContext")

laws = laws.replace("private fun OppositionChamberPanel(\n    state: GameState,\n    viewModel: GameViewModel,\n) {", "private fun OppositionChamberPanel(\n    state: GameState,\n    viewModel: GameViewModel,\n) {\n    val context = LocalContext.current")
laws = laws.replace("private fun IdeologyPanel(\n    state: GameState,\n    viewModel: GameViewModel,\n) {", "private fun IdeologyPanel(\n    state: GameState,\n    viewModel: GameViewModel,\n) {\n    val context = LocalContext.current")
laws = laws.replace("private fun SocietyMinistriesPanel(\n    state: GameState,\n    viewModel: GameViewModel,\n) {", "private fun SocietyMinistriesPanel(\n    state: GameState,\n    viewModel: GameViewModel,\n) {\n    val context = LocalContext.current")

laws = laws.replace("onClick = { viewModel.negotiateWithOpposition() }", "onClick = { viewModel.negotiateWithOpposition(); Toast.makeText(context, \"Negotiating...\", Toast.LENGTH_SHORT).show() }")
laws = laws.replace("onClick = { viewModel.smearOpposition() }", "onClick = { viewModel.smearOpposition(); Toast.makeText(context, \"Smear campaign started.\", Toast.LENGTH_SHORT).show() }")
laws = laws.replace("onClick = { viewModel.concedeToOpposition() }", "onClick = { viewModel.concedeToOpposition(); Toast.makeText(context, \"Concessions made.\", Toast.LENGTH_SHORT).show() }")
laws = laws.replace("onClick = { viewModel.setIdeology(ideology) }", "onClick = { viewModel.setIdeology(ideology); Toast.makeText(context, \"Ideology shifted to ${ideology.displayName}.\", Toast.LENGTH_SHORT).show() }")
laws = laws.replace("onClick = { viewModel.changeStateReligion(religion) }", "onClick = { viewModel.changeStateReligion(religion); Toast.makeText(context, \"State Religion changed to ${religion.displayName}.\", Toast.LENGTH_SHORT).show() }")

with open("app/src/main/java/com/presidentsimulator/game/ui/screens/LawsScreen.kt", "w") as f:
    f.write(laws)
