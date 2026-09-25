import re

path_vm = "app/src/main/java/com/presidentsimulator/game/viewmodel/GameViewModel.kt"
with open(path_vm, "r") as f:
    vm = f.read()

# Add a flow for tutorial state
tutorial_flow = """
    private val _showTutorial = MutableStateFlow(false)
    val showTutorial: StateFlow<Boolean> = _showTutorial.asStateFlow()

    fun triggerTutorial() {
        _showTutorial.value = true
    }
    
    fun dismissTutorial() {
        _showTutorial.value = false
    }
"""
if "val showTutorial: StateFlow<Boolean>" not in vm:
    vm = vm.replace("init {", tutorial_flow + "\n    init {")
    # Actually, initially show it if no save
    vm = vm.replace("init {", "init {\n        if (!hasAutomatedSave()) _showTutorial.value = true")

with open(path_vm, "w") as f:
    f.write(vm)

path_nav = "app/src/main/java/com/presidentsimulator/game/ui/navigation/GameNavigation.kt"
with open(path_nav, "r") as f:
    nav = f.read()

nav = nav.replace("var showTutorial by remember { mutableStateOf(!viewModel.hasAutomatedSave()) }", "val showTutorial by viewModel.showTutorial.collectAsState()")
nav = nav.replace("showTutorial = false", "viewModel.dismissTutorial()")

with open(path_nav, "w") as f:
    f.write(nav)

path_settings = "app/src/main/java/com/presidentsimulator/game/ui/screens/SettingsAudioScreen.kt"
with open(path_settings, "r") as f:
    settings = f.read()

btn = """
            NssPanel(modifier = Modifier.fillMaxWidth()) {
                Text("GAME TUTORIAL", fontWeight = FontWeight.Black, fontSize = 10.sp, color = NssForeground)
                Text(
                    text = "RESTART TUTORIAL",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(NssCardShape)
                        .background(NssAccent)
                        .clickable { viewModel.triggerTutorial() }
                        .padding(vertical = 9.dp),
                    color = NssOnPhoto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                )
            }
"""
settings = settings.replace("if (viewModel != null) {", btn + "\n            if (viewModel != null) {")

with open(path_settings, "w") as f:
    f.write(settings)
