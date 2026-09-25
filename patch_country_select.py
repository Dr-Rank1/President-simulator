import re

path = "app/src/main/java/com/presidentsimulator/game/ui/screens/CountrySelectScreen.kt"
with open(path, "r") as f:
    content = f.read()

# Replace header size to be smaller
content = content.replace('fontSize = 16.sp', 'fontSize = 13.sp')
content = content.replace('size(44.dp)', 'size(32.dp)')
content = content.replace('padding(12.dp)', 'padding(8.dp)')
content = content.replace('size(20.dp)', 'size(16.dp)')

# Make texts smaller
content = content.replace('fontSize = 42.sp', 'fontSize = 28.sp') # flag
content = content.replace('fontSize = 24.sp', 'fontSize = 18.sp') # country name
content = content.replace('fontSize = 12.sp', 'fontSize = 10.sp') # official name & stat labels
content = content.replace('fontSize = 20.sp', 'fontSize = 14.sp') # stat value
content = content.replace('fontSize = 16.sp, fontWeight = FontWeight.Black', 'fontSize = 12.sp, fontWeight = FontWeight.Black') # Start game button

# Replace paddings and heights to be smaller
content = content.replace('padding(20.dp)', 'padding(16.dp)')
content = content.replace('Spacer(Modifier.height(24.dp))', 'Spacer(Modifier.height(16.dp))')
content = content.replace('height(56.dp)', 'height(44.dp)')

# Add Government Type (Type of ruling) before START GAME button
insert_target = 'Spacer(Modifier.weight(1f))'

gov_type_ui = """
                        Spacer(Modifier.height(16.dp))
                        Text("IDEOLOGY & GOVERNMENT", color = NssAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(Color(0xFF131A26)).padding(10.dp)) {
                                Column {
                                    Text("IDEOLOGY", color = NssMutedForeground, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Text(nation.ideology.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(Color(0xFF131A26)).padding(10.dp)) {
                                Column {
                                    Text("RULING SYSTEM", color = NssMutedForeground, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Text(nation.governmentSystem.name.replace("_", " "), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        
                        Spacer(Modifier.weight(1f))
"""

if "IDEOLOGY & GOVERNMENT" not in content:
    content = content.replace(insert_target, gov_type_ui)

with open(path, "w") as f:
    f.write(content)

print("Patched CountrySelectScreen")
