import re

with open("app/src/main/java/com/presidentsimulator/game/ui/screens/LawsScreen.kt", "r") as f:
    content = f.read()

if "import android.widget.Toast" not in content:
    content = content.replace("import androidx.compose.ui.Alignment", "import androidx.compose.ui.Alignment\nimport android.widget.Toast\nimport androidx.compose.ui.platform.LocalContext")

content = content.replace("val layout = rememberNssLayoutSpec()", "val layout = rememberNssLayoutSpec()\n    val context = LocalContext.current")

# Law actions
content = content.replace("onClick = { viewModel.negotiateWithOpposition() }", "onClick = { viewModel.negotiateWithOpposition(); Toast.makeText(context, \"Negotiating...\", Toast.LENGTH_SHORT).show() }")
content = content.replace("onClick = { viewModel.smearOpposition() }", "onClick = { viewModel.smearOpposition(); Toast.makeText(context, \"Smear campaign started.\", Toast.LENGTH_SHORT).show() }")
content = content.replace("onClick = { viewModel.concedeToOpposition() }", "onClick = { viewModel.concedeToOpposition(); Toast.makeText(context, \"Concessions made.\", Toast.LENGTH_SHORT).show() }")
content = content.replace("onClick = { viewModel.setIdeology(ideology) }", "onClick = { viewModel.setIdeology(ideology); Toast.makeText(context, \"Ideology shifted to ${ideology.displayName}.\", Toast.LENGTH_SHORT).show() }")
content = content.replace("onClick = { viewModel.changeStateReligion(religion) }", "onClick = { viewModel.changeStateReligion(religion); Toast.makeText(context, \"State Religion changed to ${religion.displayName}.\", Toast.LENGTH_SHORT).show() }")

with open("app/src/main/java/com/presidentsimulator/game/ui/screens/LawsScreen.kt", "w") as f:
    f.write(content)
