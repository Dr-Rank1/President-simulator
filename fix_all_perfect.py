import re

# Fix LawsScreen
with open("app/src/main/java/com/presidentsimulator/game/ui/screens/LawsScreen.kt", "r") as f:
    laws = f.read()

# Add imports
if "import android.widget.Toast" not in laws:
    laws = laws.replace("import androidx.compose.ui.Alignment", "import androidx.compose.ui.Alignment\nimport android.widget.Toast\nimport androidx.compose.ui.platform.LocalContext")

# Insert context into the three tabs
laws = re.sub(
    r'(fun OppositionTab\([^)]+\)\s*\{)',
    r'\1\n    val context = LocalContext.current',
    laws
)
laws = re.sub(
    r'(fun IdeologyTab\([^)]+\)\s*\{)',
    r'\1\n    val context = LocalContext.current',
    laws
)
laws = re.sub(
    r'(fun ReligiousTab\([^)]+\)\s*\{)',
    r'\1\n    val context = LocalContext.current',
    laws
)

# Add toasts
laws = laws.replace("onClick = { viewModel.negotiateWithOpposition() }", "onClick = { viewModel.negotiateWithOpposition(); Toast.makeText(context, \"Negotiating...\", Toast.LENGTH_SHORT).show() }")
laws = laws.replace("onClick = { viewModel.smearOpposition() }", "onClick = { viewModel.smearOpposition(); Toast.makeText(context, \"Smear campaign started.\", Toast.LENGTH_SHORT).show() }")
laws = laws.replace("onClick = { viewModel.concedeToOpposition() }", "onClick = { viewModel.concedeToOpposition(); Toast.makeText(context, \"Concessions made.\", Toast.LENGTH_SHORT).show() }")
laws = laws.replace("onClick = { viewModel.setIdeology(ideology) }", "onClick = { viewModel.setIdeology(ideology); Toast.makeText(context, \"Ideology shifted to ${ideology.displayName}.\", Toast.LENGTH_SHORT).show() }")
laws = laws.replace("onClick = { viewModel.changeStateReligion(religion) }", "onClick = { viewModel.changeStateReligion(religion); Toast.makeText(context, \"State Religion changed to ${religion.displayName}.\", Toast.LENGTH_SHORT).show() }")

with open("app/src/main/java/com/presidentsimulator/game/ui/screens/LawsScreen.kt", "w") as f:
    f.write(laws)

# Fix DiplomacyScreen
with open("app/src/main/java/com/presidentsimulator/game/ui/screens/DiplomacyScreen.kt", "r") as f:
    diplo = f.read()

if "import android.widget.Toast" not in diplo:
    diplo = diplo.replace("import androidx.compose.ui.Alignment", "import androidx.compose.ui.Alignment\nimport android.widget.Toast\nimport androidx.compose.ui.platform.LocalContext\nimport androidx.compose.foundation.lazy.LazyColumn\nimport androidx.compose.foundation.lazy.items\nimport androidx.compose.foundation.lazy.itemsIndexed")

diplo = diplo.replace("val layout = rememberNssLayoutSpec()", "val layout = rememberNssLayoutSpec()\n    val context = LocalContext.current")

# Add toasts to RivalActionPanel manually using replace
diplo = diplo.replace("onProposeTradeDeal = { viewModel.proposeTradeDeal(rival.id, TradeCommodity.GRAIN, 100L, TradeType.EXPORT) }", "onProposeTradeDeal = { viewModel.proposeTradeDeal(rival.id, TradeCommodity.GRAIN, 100L, TradeType.EXPORT); Toast.makeText(context, \"Deal proposed.\", Toast.LENGTH_SHORT).show() }")
diplo = diplo.replace("onSendAid = { viewModel.sendForeignAid(rival.id) }", "onSendAid = { viewModel.sendForeignAid(rival.id); Toast.makeText(context, \"Aid sent.\", Toast.LENGTH_SHORT).show() }")
diplo = diplo.replace("onStateVisit = { viewModel.conductStateVisit(rival.id) }", "onStateVisit = { viewModel.conductStateVisit(rival.id); Toast.makeText(context, \"State visit completed.\", Toast.LENGTH_SHORT).show() }")
diplo = diplo.replace("onNegotiateTradeTreaty = { viewModel.negotiateTreaty(rival.id, TreatyType.TRADE) }", "onNegotiateTradeTreaty = { viewModel.negotiateTreaty(rival.id, TreatyType.TRADE); Toast.makeText(context, \"Treaty signed.\", Toast.LENGTH_SHORT).show() }")
diplo = diplo.replace("onNegotiateNonAggression = { viewModel.negotiateTreaty(rival.id, TreatyType.NON_AGGRESSION) }", "onNegotiateNonAggression = { viewModel.negotiateTreaty(rival.id, TreatyType.NON_AGGRESSION); Toast.makeText(context, \"Pact signed.\", Toast.LENGTH_SHORT).show() }")
diplo = diplo.replace("onFormAlliance = { viewModel.formAlliance(\"Pact with ${rival.name}\", listOf(rival.id)) }", "onFormAlliance = { viewModel.formAlliance(\"Pact with ${rival.name}\", listOf(rival.id)); Toast.makeText(context, \"Alliance formed.\", Toast.LENGTH_SHORT).show() }")
diplo = diplo.replace("onDeclareWar = { goal -> viewModel.declareWar(rival.id, goal) }", "onDeclareWar = { goal -> viewModel.declareWar(rival.id, goal); Toast.makeText(context, \"WAR DECLARED!\", Toast.LENGTH_LONG).show() }")

# Replace TREATIES buttons
diplo = diplo.replace("onClick = { viewModel.breakTreaty(rival.id, TreatyType.TRADE) }", "onClick = { viewModel.breakTreaty(rival.id, TreatyType.TRADE); Toast.makeText(context, \"Trade broken.\", Toast.LENGTH_SHORT).show() }")
diplo = diplo.replace("onClick = { viewModel.breakTreaty(rival.id, TreatyType.NON_AGGRESSION) }", "onClick = { viewModel.breakTreaty(rival.id, TreatyType.NON_AGGRESSION); Toast.makeText(context, \"Pact broken.\", Toast.LENGTH_SHORT).show() }")

# Replace NEGOTIATIONS buttons
diplo = diplo.replace("onClick = { viewModel.cancelTradeDeal(deal.id) }", "onClick = { viewModel.cancelTradeDeal(deal.id); Toast.makeText(context, \"Deal cancelled.\", Toast.LENGTH_SHORT).show() }")

# Convert Column to LazyColumn
pattern = r'Column\(\s*modifier = Modifier\s*\.fillMaxSize\(\)\s*\.verticalScroll\(rememberScrollState\(\)\)\s*\.nssMinistryScrollPadding\(\)\s*\.padding\(Dimens\.ContentPadding\),\s*verticalArrangement = Arrangement\.spacedBy\(12\.dp\),\s*\) \{([\s\S]*?)\}\s*\}\s*\}\s*\}'

new_lazy = """LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nssMinistryScrollPadding()
                .padding(Dimens.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (selectedTab) {
                "RELATIONS" -> {
                    item { RelationsLegend() }
                    itemsIndexed(state.diplomacy.rivals.chunked(layout.gridColumns)) { rowIndex, row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEachIndexed { colIndex, rival ->
                                val cardIndex = rowIndex * layout.gridColumns + colIndex
                                NssNationCard(
                                    nationName = rival.name,
                                    flagEmoji = rivalFlagEmoji(rival),
                                    status = rivalStatus(rival),
                                    threat = rivalThreat(rival),
                                    relations = rival.relationshipScore.coerceIn(0, 100),
                                    tradeLabel = when {
                                        rival.hasEmbargo -> "EMBARGO"
                                        rival.hasTradeTreaty -> "OPEN"
                                        else -> "STANDARD"
                                    },
                                    militaryLabel = if (activeWar?.targetCountryId == rival.id) "ACTIVE CONFLICT" else rival.stance.label.uppercase(),
                                    headerColor = rivalHeaderColor(rival),
                                    imageUrl = NssCardImages.nationCardImage(cardIndex),
                                    headerGradient = NssGradients.Foreign,
                                    isHostile = rival.relationshipScore < 20 || activeWar?.targetCountryId == rival.id,
                                    onAction = { selectedRivalId = if (selectedRivalId == rival.id) null else rival.id },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    selectedRival?.let { rival ->
                        item {
                            RivalActionPanel(
                                state = state,
                                rival = rival,
                                warActive = activeWar != null,
                                isWarTarget = activeWar?.targetCountryId == rival.id,
                                onProposeTradeDeal = { 
                                    viewModel.proposeTradeDeal(rival.id, TradeCommodity.GRAIN, 100L, TradeType.EXPORT)
                                    Toast.makeText(context, "Deal proposed.", Toast.LENGTH_SHORT).show()
                                },
                                onSendAid = { 
                                    viewModel.sendForeignAid(rival.id)
                                    Toast.makeText(context, "Aid sent.", Toast.LENGTH_SHORT).show()
                                },
                                onStateVisit = { 
                                    viewModel.conductStateVisit(rival.id)
                                    Toast.makeText(context, "State visit completed.", Toast.LENGTH_SHORT).show()
                                },
                                onNegotiateTradeTreaty = { 
                                    viewModel.negotiateTreaty(rival.id, TreatyType.TRADE)
                                    Toast.makeText(context, "Treaty signed.", Toast.LENGTH_SHORT).show()
                                },
                                onNegotiateNonAggression = { 
                                    viewModel.negotiateTreaty(rival.id, TreatyType.NON_AGGRESSION)
                                    Toast.makeText(context, "Pact signed.", Toast.LENGTH_SHORT).show()
                                },
                                onFormAlliance = { 
                                    viewModel.formAlliance("Pact with ${rival.name}", listOf(rival.id))
                                    Toast.makeText(context, "Alliance formed.", Toast.LENGTH_SHORT).show()
                                },
                                onDeclareWar = { goal -> 
                                    viewModel.declareWar(rival.id, goal)
                                    Toast.makeText(context, "WAR DECLARED!", Toast.LENGTH_LONG).show()
                                },
                            )
                        }
                    }
                }

                "TREATIES" -> {
                    val treatyRivals = state.diplomacy.rivals.filter { it.hasTradeTreaty || it.hasNonAggressionPact }
                    if (treatyRivals.isEmpty()) {
                        item { Text("No active treaties. Negotiate with rivals on the Relations tab.", color = NssMutedForeground) }
                    } else {
                        items(treatyRivals, key = { it.id }) { rival ->
                            NssStripPhotoCard(
                                imageUrl = NssCardImages.BANNER_FOREIGN,
                                fallbackGradient = NssGradients.Foreign,
                            ) {
                                Text(rival.name, color = NssForeground, fontWeight = FontWeight.Bold)
                                val treatyLabel = when {
                                    rival.hasTradeTreaty && rival.hasNonAggressionPact -> "Trade + Non-Aggression"
                                    rival.hasTradeTreaty -> "Free Trade Agreement"
                                    else -> "Non-Aggression Pact"
                                }
                                Text(
                                    treatyLabel,
                                    color = NssMutedForeground,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (rival.hasTradeTreaty) NssBadge("TRADE")
                                    if (rival.hasNonAggressionPact) NssBadge("NAP")
                                    NssBadge("ACTIVE", large = true)
                                }
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 10.dp),
                                ) {
                                    if (rival.hasTradeTreaty) {
                                        OutlinedButton(
                                            onClick = { 
                                                viewModel.breakTreaty(rival.id, TreatyType.TRADE)
                                                Toast.makeText(context, "Trade broken.", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Text("Break Trade")
                                        }
                                    }
                                    if (rival.hasNonAggressionPact) {
                                        OutlinedButton(
                                            onClick = { 
                                                viewModel.breakTreaty(rival.id, TreatyType.NON_AGGRESSION)
                                                Toast.makeText(context, "Pact broken.", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NssRed),
                                        ) {
                                            Text("Break Non-Aggression")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "NEGOTIATIONS" -> {
                    val deals = state.diplomacy.tradeDeals
                    if (deals.isEmpty()) {
                        item { Text("No active trade negotiations.", color = NssMutedForeground) }
                    } else {
                        items(deals, key = { it.id }) { deal ->
                            NssCard {
                                Text("${deal.type} — ${deal.commodity}", fontWeight = FontWeight.Bold, color = NssForeground)
                                Text("Partner: ${deal.partnerCountryId}", color = NssMutedForeground, fontSize = 12.sp)
                                Text(
                                    "Volume: ${deal.amount}",
                                    color = NssMutedForeground,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                                OutlinedButton(
                                    onClick = { 
                                        viewModel.cancelTradeDeal(deal.id)
                                        Toast.makeText(context, "Deal cancelled.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                ) {
                                    Text("Cancel Deal")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}"""
diplo = re.sub(pattern, new_lazy, diplo)

with open("app/src/main/java/com/presidentsimulator/game/ui/screens/DiplomacyScreen.kt", "w") as f:
    f.write(diplo)

