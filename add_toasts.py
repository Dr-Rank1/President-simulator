import re

path = "app/src/main/java/com/presidentsimulator/game/viewmodel/GameViewModel.kt"
with open(path, "r") as f:
    content = f.read()

# Replace _state.update { something(it) } 
# with _state.update { old -> val new = something(old); if (new === old) Toast.makeText... else Toast.makeText... }

# Wait, this is tricky. Let's just create a wrapper function in GameViewModel:
# private fun performAction(actionName: String, block: (GameState) -> GameState) {
#     val oldState = _state.value
#     val newState = block(oldState)
#     if (newState !== oldState) {
#         _state.value = newState
#         Toast.makeText(getApplication(), "$actionName Executed", Toast.LENGTH_SHORT).show()
#     } else {
#         Toast.makeText(getApplication(), "$actionName Failed (Insufficient resources/conditions)", Toast.LENGTH_SHORT).show()
#     }
# }
# But GameViewModel is probably using MutableStateFlow update {}.
