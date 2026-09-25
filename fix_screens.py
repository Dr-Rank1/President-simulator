import glob
import re

files = glob.glob('app/src/main/java/com/presidentsimulator/game/ui/screens/*.kt')

for file in files:
    with open(file, 'r') as f:
        content = f.read()
    
    # Only modify background(NssBackground)
    if 'background(NssBackground)' in content:
        content = content.replace('background(NssBackground)', 'background(androidx.compose.ui.graphics.Color(0xCC050A0F))')
        
    with open(file, 'w') as f:
        f.write(content)

