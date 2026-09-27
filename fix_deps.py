import os
import re

pom_files = []
for root, dirs, files in os.walk('.'):
    if 'pom.xml' in files:
        pom_files.append(os.path.join(root, 'pom.xml'))

# Fix hibernate-validator
validator_regex = re.compile(r'(<artifactId>hibernate-validator</artifactId>\s*<version>)[^<]+(</version>)')
for file in pom_files:
    with open(file, 'r') as f:
        content = f.read()
    new_content = validator_regex.sub(r'\g<1>6.2.0.Final\g<2>', content)
    if new_content != content:
        with open(file, 'w') as f:
            f.write(new_content)
        print(f"Updated hibernate-validator in {file}")

# Fix commons-lang
for file in pom_files:
    with open(file, 'r') as f:
        content = f.read()
    if 'commons-lang' in content:
        # Replace groupId and artifactId to commons-lang3 and version to 3.14.0
        new_content = content.replace(
            "<groupId>commons-lang</groupId>\n            <artifactId>commons-lang</artifactId>\n            <version>2.6</version>",
            "<groupId>org.apache.commons</groupId>\n            <artifactId>commons-lang3</artifactId>\n            <version>3.14.0</version>"
        )
        if new_content != content:
            with open(file, 'w') as f:
                f.write(new_content)
            print(f"Updated commons-lang in {file}")

