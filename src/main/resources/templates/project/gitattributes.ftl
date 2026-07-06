# Auto detect text files and perform LF normalization
* text=auto

# Java sources
*.java text diff=java

# These files are text and should be normalized (Convert crlf => lf)
*.css text diff=css
*.html text diff=html
*.js text
*.json text
*.properties text
*.xml text diff=html
*.yml text

# These files are binary and should be left untouched
*.jar binary
*.class binary
*.png binary
*.jpg binary
*.gif binary
*.ico binary

# Maven wrapper
mvnw text eol=lf
mvnw.cmd text eol=crlf
