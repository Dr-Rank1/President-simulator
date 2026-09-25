with open("app/src/main/java/com/presidentsimulator/game/ui/screens/DiplomacyScreen.kt", "r") as f:
    content = f.read()

import re

if "import androidx.compose.foundation.lazy.LazyColumn" not in content:
    content = content.replace("import androidx.compose.foundation.layout.Column", "import androidx.compose.foundation.layout.Column\nimport androidx.compose.foundation.lazy.LazyColumn\nimport androidx.compose.foundation.lazy.items\nimport androidx.compose.foundation.lazy.itemsIndexed")

old_column = """        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .nssMinistryScrollPadding()
                .padding(Dimens.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {"""

new_column = """        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nssMinistryScrollPadding()
                .padding(Dimens.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {"""

content = content.replace(old_column, new_column)

# Now fix the when block inside
# The old code was:
#            when (selectedTab) {
#                "RELATIONS" -> {
#                    RelationsLegend()
#                    state.diplomacy.rivals.chunked(layout.gridColumns).forEachIndexed { rowIndex, row -> ... }
#
# But now we are inside a LazyColumn. We must use `item { }` and `items()`

old_relations = """                "RELATIONS" -> {
                    RelationsLegend()
                    state.diplomacy.rivals.chunked(layout.gridColumns).forEachIndexed { rowIndex, row ->
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
                        RivalActionPanel(
                            state = state,
                            rival = rival,
                            warActive = activeWar != null,
                            isWarTarget = activeWar?.targetCountryId == rival.id,
                            onProposeTradeDeal = {
                                viewModel.proposeTradeDeal(rival.id, TradeCommodity.GRAIN, 100L, TradeType.EXPORT)
                            },
                            onSendAid = { viewModel.sendForeignAid(rival.id) },
                            onStateVisit = { viewModel.conductStateVisit(rival.id) },
                            onNegotiateTradeTreaty = {
                                viewModel.negotiateTreaty(rival.id, TreatyType.TRADE)
                            },
                            onNegotiateNonAggression = {
                                viewModel.negotiateTreaty(rival.id, TreatyType.NON_AGGRESSION)
                            },
                            onFormAlliance = {
                                viewModel.formAlliance("Pact with ${rival.name}", listOf(rival.id))
                            },
                            onDeclareWar = { goal -> viewModel.declareWar(rival.id, goal) },
                        )
                    }
                }"""

new_relations = """                "RELATIONS" -> {
                    item { RelationsLegend() }
                    
                    selectedRival?.let { rival ->
                        item {
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
                        }
                    }

                    val chunks = state.diplomacy.rivals.chunked(layout.gridColumns)
                    itemsIndexed(chunks, key = { index, _ -> "chunk_$index" }) { rowIndex, row ->
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
                }"""

content = content.replace(old_relations, new_relations)

old_treaties = """                "TREATIES" -> {
                    state.diplomacy.rivals.filter { it.hasTradeTreaty || it.hasNonAggressionPact }.forEach { rival ->
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
                                        onClick = { viewModel.breakTreaty(rival.id, TreatyType.TRADE) },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text("Break Trade")
                                    }
                                }
                                if (rival.hasNonAggressionPact) {
                                    OutlinedButton(
                                        onClick = { viewModel.breakTreaty(rival.id, TreatyType.NON_AGGRESSION) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NssRed),
                                    ) {
                                        Text("Break Non-Aggression")
                                    }
                                }
                            }
                        }
                    }
                    if (state.diplomacy.rivals.none { it.hasTradeTreaty || it.hasNonAggressionPact }) {
                        Text("No active treaties. Negotiate with rivals on the Relations tab.", color = NssMutedForeground)
                    }
                }"""

new_treaties = """                "TREATIES" -> {
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
                                            onClick = { viewModel.breakTreaty(rival.id, TreatyType.TRADE) },
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Text("Break Trade")
                                        }
                                    }
                                    if (rival.hasNonAggressionPact) {
                                        OutlinedButton(
                                            onClick = { viewModel.breakTreaty(rival.id, TreatyType.NON_AGGRESSION) },
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
                }"""

content = content.replace(old_treaties, new_treaties)

old_negotiations = """                "NEGOTIATIONS" -> {
                    state.diplomacy.tradeDeals.forEach { deal ->
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
                                onClick = { viewModel.cancelTradeDeal(deal.id) },
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            ) {
                                Text("Cancel Deal")
                            }
                        }
                    }
                    if (state.diplomacy.tradeDeals.isEmpty()) {
                        Text("No active trade negotiations.", color = NssMutedForeground)
                    }
                }"""

new_negotiations = """                "NEGOTIATIONS" -> {
                    if (state.diplomacy.tradeDeals.isEmpty()) {
                        item { Text("No active trade negotiations.", color = NssMutedForeground) }
                    } else {
                        items(state.diplomacy.tradeDeals, key = { it.id }) { deal ->
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
                                    onClick = { viewModel.cancelTradeDeal(deal.id) },
                                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                ) {
                                    Text("Cancel Deal")
                                }
                            }
                        }
                    }
                }"""

content = content.replace(old_negotiations, new_negotiations)

with open("app/src/main/java/com/presidentsimulator/game/ui/screens/DiplomacyScreen.kt", "w") as f:
    f.write(content)
