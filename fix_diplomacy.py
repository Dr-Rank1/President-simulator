import re

path = "app/src/main/java/com/presidentsimulator/game/ui/screens/DiplomacyScreen.kt"
with open(path, "r") as f:
    content = f.read()

# Remove the item { selectedRival?.let { rival -> ... } } from the LazyColumn
# Because it's hard to regex perfectly, let's just replace the exact block.
# Actually, I can just use python to find the block and move it.
item_block_start = "item { selectedRival?.let { rival ->"
item_block_end = "} }"

if item_block_start in content:
    idx_start = content.find(item_block_start)
    # find the matching closing braces
    brace_count = 0
    idx_end = -1
    for i in range(idx_start + 5, len(content)):
        if content[i] == '{': brace_count += 1
        elif content[i] == '}':
            brace_count -= 1
            if brace_count == 0:
                # Need one more for the outer } }
                if content[i+1:i+3] == " }":
                    idx_end = i + 3
                else:
                    idx_end = i + 1
                break
    
    if idx_end != -1:
        extracted_block = content[idx_start:idx_end]
        # Remove it from the LazyColumn
        content = content[:idx_start] + content[idx_end:]
        
        # Now put the modal overlay at the bottom of the main Column
        modal_ui = """
        if (selectedRival != null) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { selectedRivalId = null }) {
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF131A26)).padding(16.dp)) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedRival.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            androidx.compose.foundation.clickable(onClick = { selectedRivalId = null }) {
                                Text("CLOSE", color = NssAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        RivalActionPanel(
                            state = state,
                            rival = selectedRival,
                            warActive = activeWar != null,
                            isWarTarget = activeWar?.targetCountryId == selectedRival.id,
                            onProposeTradeDeal = { viewModel.proposeTradeDeal(selectedRival.id, TradeCommodity.GRAIN, 100L, TradeType.EXPORT) },
                            onSendAid = { viewModel.sendForeignAid(selectedRival.id) },
                            onStateVisit = { viewModel.conductStateVisit(selectedRival.id) },
                            onNegotiateTradeTreaty = { viewModel.negotiateTreaty(selectedRival.id, TreatyType.TRADE) },
                            onNegotiateNonAggression = { viewModel.negotiateTreaty(selectedRival.id, TreatyType.NON_AGGRESSION) },
                            onFormAlliance = { viewModel.formAlliance("Pact with ${selectedRival.name}", listOf(selectedRival.id)) },
                            onDeclareWar = { goal -> viewModel.declareWar(selectedRival.id, goal); selectedRivalId = null }
                        )
                    }
                }
            }
        }
"""
        # Insert modal_ui right before the last closing brace of DiplomacyScreen
        # We find the last } before private fun
        idx_last_brace = content.rfind("}", 0, content.find("private fun"))
        content = content[:idx_last_brace] + modal_ui + content[idx_last_brace:]
        
with open(path, "w") as f:
    f.write(content)
print("DiplomacyScreen fixed")
