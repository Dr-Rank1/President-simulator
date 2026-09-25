import re

with open("app/src/main/java/com/presidentsimulator/game/ui/screens/DiplomacyScreen.kt", "r") as f:
    content = f.read()

# Add context
if "import android.widget.Toast" not in content:
    content = content.replace("import androidx.compose.ui.Alignment", "import androidx.compose.ui.Alignment\nimport android.widget.Toast\nimport androidx.compose.ui.platform.LocalContext")

# in DiplomacyScreen:
content = content.replace("val layout = rememberNssLayoutSpec()", "val layout = rememberNssLayoutSpec()\n    val context = LocalContext.current")

# Now we need to pass context to RivalActionPanel and the TREATIES / NEGOTIATIONS sections.
# But RivalActionPanel has `onProposeTradeDeal: () -> Unit`. I can just wrap the viewmodel calls in DiplomacyScreen.

# In the RELATIONS section:
old_rival_panel = """                        item {
                            RivalActionPanel(
                                state = state,
                                rival = rival,
                                warActive = activeWar != null,
                                isWarTarget = activeWar?.targetCountryId == rival.id,
                                onProposeTradeDeal = { viewModel.proposeTradeDeal(rival.id, TradeCommodity.GRAIN, 100L, TradeType.EXPORT) },
                                onSendAid = { viewModel.sendForeignAid(rival.id) },
                                onStateVisit = { viewModel.conductStateVisit(rival.id) },
                                onNegotiateTradeTreaty = { viewModel.negotiateTreaty(rival.id, TreatyType.TRADE) },
                                onNegotiateNonAggression = { viewModel.negotiateTreaty(rival.id, TreatyType.NON_AGGRESSION) },
                                onFormAlliance = { viewModel.formAlliance("Pact with ${rival.name}", listOf(rival.id)) },
                                onDeclareWar = { goal -> viewModel.declareWar(rival.id, goal) },
                            )
                        }"""

new_rival_panel = """                        item {
                            RivalActionPanel(
                                state = state,
                                rival = rival,
                                warActive = activeWar != null,
                                isWarTarget = activeWar?.targetCountryId == rival.id,
                                onProposeTradeDeal = { 
                                    viewModel.proposeTradeDeal(rival.id, TradeCommodity.GRAIN, 100L, TradeType.EXPORT)
                                    Toast.makeText(context, "Trade deal proposed to ${rival.name}", Toast.LENGTH_SHORT).show()
                                },
                                onSendAid = { 
                                    viewModel.sendForeignAid(rival.id) 
                                    Toast.makeText(context, "Foreign aid sent to ${rival.name}. Relations improved.", Toast.LENGTH_SHORT).show()
                                },
                                onStateVisit = { 
                                    viewModel.conductStateVisit(rival.id) 
                                    Toast.makeText(context, "State visit to ${rival.name} completed.", Toast.LENGTH_SHORT).show()
                                },
                                onNegotiateTradeTreaty = { 
                                    viewModel.negotiateTreaty(rival.id, TreatyType.TRADE)
                                    Toast.makeText(context, "Trade treaty with ${rival.name} signed.", Toast.LENGTH_SHORT).show()
                                },
                                onNegotiateNonAggression = { 
                                    viewModel.negotiateTreaty(rival.id, TreatyType.NON_AGGRESSION) 
                                    Toast.makeText(context, "Non-Aggression pact with ${rival.name} signed.", Toast.LENGTH_SHORT).show()
                                },
                                onFormAlliance = { 
                                    viewModel.formAlliance("Pact with ${rival.name}", listOf(rival.id)) 
                                    Toast.makeText(context, "Alliance formed with ${rival.name}!", Toast.LENGTH_SHORT).show()
                                },
                                onDeclareWar = { goal -> 
                                    viewModel.declareWar(rival.id, goal) 
                                    Toast.makeText(context, "WAR DECLARED ON ${rival.name.uppercase()}!", Toast.LENGTH_LONG).show()
                                },
                            )
                        }"""

content = content.replace(old_rival_panel, new_rival_panel)

# In TREATIES section
old_break_trade = "onClick = { viewModel.breakTreaty(rival.id, TreatyType.TRADE) }"
new_break_trade = "onClick = { viewModel.breakTreaty(rival.id, TreatyType.TRADE); Toast.makeText(context, \"Trade treaty with ${rival.name} broken.\", Toast.LENGTH_SHORT).show() }"
content = content.replace(old_break_trade, new_break_trade)

old_break_nap = "onClick = { viewModel.breakTreaty(rival.id, TreatyType.NON_AGGRESSION) }"
new_break_nap = "onClick = { viewModel.breakTreaty(rival.id, TreatyType.NON_AGGRESSION); Toast.makeText(context, \"Non-Aggression pact with ${rival.name} broken.\", Toast.LENGTH_SHORT).show() }"
content = content.replace(old_break_nap, new_break_nap)

# In NEGOTIATIONS section
old_cancel_deal = "onClick = { viewModel.cancelTradeDeal(deal.id) }"
new_cancel_deal = "onClick = { viewModel.cancelTradeDeal(deal.id); Toast.makeText(context, \"Trade deal cancelled.\", Toast.LENGTH_SHORT).show() }"
content = content.replace(old_cancel_deal, new_cancel_deal)

with open("app/src/main/java/com/presidentsimulator/game/ui/screens/DiplomacyScreen.kt", "w") as f:
    f.write(content)
