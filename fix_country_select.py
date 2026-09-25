with open("app/src/main/java/com/presidentsimulator/game/ui/screens/CountrySelectScreen.kt", "r") as f:
    content = f.read()

# We need to change NationList and NationChoiceRow to use LazyVerticalGrid
import re

# Add required imports
if "import androidx.compose.foundation.lazy.grid.LazyVerticalGrid" not in content:
    content = content.replace(
        "import androidx.compose.foundation.lazy.LazyColumn",
        "import androidx.compose.foundation.lazy.LazyColumn\nimport androidx.compose.foundation.lazy.grid.LazyVerticalGrid\nimport androidx.compose.foundation.lazy.grid.GridCells\nimport androidx.compose.foundation.lazy.grid.items\nimport androidx.compose.foundation.layout.aspectRatio"
    )

nation_list_old = """@Composable
private fun NationList(
    nations: List<PlayableNationCatalog.NationDefinition>,
    selectedNationId: String,
    favoriteIds: Set<String>,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(nations, key = { it.id }) { item ->
            NationChoiceRow(item, item.id == selectedNationId, item.id in favoriteIds,
                onSelect = { onSelect(item.id) }, onToggleFavorite = { onToggleFavorite(item.id) })
        }
        if (nations.isEmpty()) item { Text("No countries match that search.", color = NssMutedForeground, fontSize = 12.sp, modifier = Modifier.padding(16.dp)) }
    }
}"""

nation_list_new = """@Composable
private fun NationList(
    nations: List<PlayableNationCatalog.NationDefinition>,
    selectedNationId: String,
    favoriteIds: Set<String>,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(nations, key = { it.id }) { item ->
            NationChoiceCard(item, item.id == selectedNationId, item.id in favoriteIds,
                onSelect = { onSelect(item.id) }, onToggleFavorite = { onToggleFavorite(item.id) })
        }
    }
    if (nations.isEmpty()) {
        Text("No countries match that search.", color = NssMutedForeground, fontSize = 14.sp, modifier = Modifier.padding(16.dp))
    }
}"""

content = content.replace(nation_list_old, nation_list_new)

nation_row_old = """@Composable
private fun NationChoiceRow(
    nation: PlayableNationCatalog.NationDefinition,
    selected: Boolean,
    favorite: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(NssCardShape)
            .background(if (selected) NssPrimary.copy(alpha = 0.35f) else NssGameCard)
            .border(1.dp, if (selected) NssAccent else NssBorder, NssCardShape)
            .clickable(onClick = onSelect).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(nation.flagEmoji, fontSize = 26.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(nation.name, color = NssForeground, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${nation.governmentLabel} · ${nation.region}", color = NssMutedForeground, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(
            if (favorite) "★" else "☆",
            modifier = Modifier.clip(CircleShape).clickable(onClick = onToggleFavorite).padding(6.dp),
            color = NssAccent,
            fontSize = 17.sp,
        )
        if (selected) SelectionCheck()
    }
}"""

nation_row_new = """@Composable
private fun NationChoiceCard(
    nation: PlayableNationCatalog.NationDefinition,
    selected: Boolean,
    favorite: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(NssCardShape)
            .background(if (selected) NssPrimary.copy(alpha = 0.4f) else NssGameCard)
            .border(2.dp, if (selected) NssAccent else NssBorder, NssCardShape)
            .clickable(onClick = onSelect)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                if (favorite) "★" else "☆",
                modifier = Modifier.clip(CircleShape).clickable(onClick = onToggleFavorite).padding(4.dp),
                color = NssAccent,
                fontSize = 20.sp,
            )
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = "Selected", tint = NssEmerald, modifier = Modifier.size(24.dp))
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        Text(nation.flagEmoji, fontSize = 60.sp)
        Spacer(modifier = Modifier.weight(1f))
        
        Text(nation.name, color = NssForeground, fontWeight = FontWeight.Black, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        Text("${nation.governmentLabel}", color = NssAccent, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        Text("${nation.region}", color = NssMutedForeground, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
    }
}"""

content = content.replace(nation_row_old, nation_row_new)

with open("app/src/main/java/com/presidentsimulator/game/ui/screens/CountrySelectScreen.kt", "w") as f:
    f.write(content)
