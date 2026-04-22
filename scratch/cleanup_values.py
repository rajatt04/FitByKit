import xml.etree.ElementTree as ET
import re

def clean_file(file_path, unused_names, tag_type):
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        new_lines = []
        for line in lines:
            match = re.search(f'<{tag_type} name="([^"]+)">', line)
            if match:
                name = match.group(1)
                if name in unused_names:
                    continue # Skip this line
            new_lines.append(line)
            
        with open(file_path, 'w', encoding='utf-8') as f:
            f.writelines(new_lines)
        print(f"Cleaned {file_path}")
    except Exception as e:
        print(f"Failed to clean {file_path}: {e}")

try:
    tree = ET.parse('app/build/reports/lint-results-debug.xml')
    root = tree.getroot()
    
    unused_strings = set()
    unused_dimens = set()
    unused_colors = set()
    unused_styles = set()
    
    for issue in root.findall(".//issue[@id='UnusedResources']"):
        msg = issue.get('message')
        # e.g., "The resource `R.string.allow` appears to be unused"
        match = re.search(r'`R\.([^.]+)\.([^`]+)`', msg)
        if match:
            res_type = match.group(1)
            res_name = match.group(2)
            if res_type == 'string':
                unused_strings.add(res_name)
            elif res_type == 'dimen':
                unused_dimens.add(res_name)
            elif res_type == 'color':
                unused_colors.add(res_name)
            elif res_type == 'style':
                unused_styles.add(res_name)

    print(f"Found {len(unused_strings)} strings, {len(unused_dimens)} dimens, {len(unused_colors)} colors, {len(unused_styles)} styles")
    
    clean_file('app/src/main/res/values/strings.xml', unused_strings, 'string')
    clean_file('app/src/main/res/values/dimens.xml', unused_dimens, 'dimen')
    clean_file('app/src/main/res/values/themes.xml', unused_styles, 'style')
    
except Exception as e:
    print(f"Error: {e}")
