$projectPath = "C:\Users\212695020\Documents\AngularApps\Spotlight"
$now = Get-Date -UFormat "%s"
$warPath
echo $now
$Server = Read-Host -Prompt 'Input your server  name'

# Download project from GIT
# Drop and set database and seed data

# Change build.gradle and application.properties
(Get-Content -path "$projectPath\server\dashboard\build.gradle" -Raw) -replace '//', '' | Set-Content -Path "$projectPath\server\dashboard\build.gradle"
(Get-Content -path "$projectPath\server\applications\build.gradle" -Raw) -replace '//', '' | Set-Content -Path "$projectPath\server\applications\build.gradle"
(Get-Content -path "$projectPath\server\cronjobs\build.gradle" -Raw) -replace '//', '' | Set-Content -Path "$projectPath\server\cronjobs\build.gradle"
(Get-Content -path "$projectPath\server\submissions\build.gradle" -Raw) -replace '//', '' | Set-Content -Path "$projectPath\server\submissions\build.gradle"
$buildDate = Get-Date
(Get-Content -path "$projectPath\client\src\app\sidebar\sidebar.component.ts" -Raw) -replace "buildDate;", "buildDate = '$buildDate';" | Set-Content -Path "$projectPath\client\src\app\sidebar\sidebar.component.ts"

# Build Angular
cd "$projectPath\client"
if ($Server -eq "dev") {
    (Get-Content -path "$projectPath\server\dashboard\src\main\resources\application.properties" -Raw) -replace 'local', 'dev' | Set-Content -Path "$projectPath\server\dashboard\src\main\resources\application.properties"
    (Get-Content -path "$projectPath\server\applications\src\main\resources\application.properties" -Raw) -replace 'local', 'dev' | Set-Content -Path "$projectPath\server\applications\src\main\resources\application.properties"
    (Get-Content -path "$projectPath\server\cronjobs\src\main\resources\application.properties" -Raw) -replace 'local', 'dev' | Set-Content -Path "$projectPath\server\cronjobs\src\main\resources\application.properties"
    (Get-Content -path "$projectPath\server\submissions\src\main\resources\application.properties" -Raw) -replace 'local', 'dev' | Set-Content -Path "$projectPath\server\submissions\src\main\resources\application.properties"
    $warPath = "C:\Users\212695020\Documents\WARS\dev\$now"
    npm run build-dev    
    npm run war
    cd "$projectPath\spotlight-mobile"
    npm run build-dev    
    npm run war
}

if ($Server -eq "qa") {
    (Get-Content -path "$projectPath\server\dashboard\src\main\resources\application.properties" -Raw) -replace 'local', 'stage' | Set-Content -Path "$projectPath\server\dashboard\src\main\resources\application.properties"
    (Get-Content -path "$projectPath\server\applications\src\main\resources\application.properties" -Raw) -replace 'local', 'stage' | Set-Content -Path "$projectPath\server\applications\src\main\resources\application.properties"
    (Get-Content -path "$projectPath\server\cronjobs\src\main\resources\application.properties" -Raw) -replace 'local', 'stage' | Set-Content -Path "$projectPath\server\cronjobs\src\main\resources\application.properties"
    (Get-Content -path "$projectPath\server\submissions\src\main\resources\application.properties" -Raw) -replace 'local', 'stage' | Set-Content -Path "$projectPath\server\submissions\src\main\resources\application.properties"
    $warPath = "C:\Users\212695020\Documents\WARS\qa\$now"    
    npm run build-stage
    npm run war
    cd "$projectPath\spotlight-mobile"
    npm run build-stage    
    npm run war
}

if ($Server -eq "prod") {
    (Get-Content -path "$projectPath\server\dashboard\src\main\resources\application.properties" -Raw) -replace 'local', 'prod' | Set-Content -Path "$projectPath\server\dashboard\src\main\resources\application.properties"
    (Get-Content -path "$projectPath\server\applications\src\main\resources\application.properties" -Raw) -replace 'local', 'prod' | Set-Content -Path "$projectPath\server\applications\src\main\resources\application.properties"
    (Get-Content -path "$projectPath\server\cronjobs\src\main\resources\application.properties" -Raw) -replace 'local', 'prod' | Set-Content -Path "$projectPath\server\cronjobs\src\main\resources\application.properties"
    (Get-Content -path "$projectPath\server\submissions\src\main\resources\application.properties" -Raw) -replace 'local', 'prod' | Set-Content -Path "$projectPath\server\submissions\src\main\resources\application.properties"
    $warPath = "C:\Users\212695020\Documents\WARS\prod\$now"        
    ng build --prod=true
    npm run war
    cd "$projectPath\spotlight-mobile"
    npm run build-prod     
    npm run war
}

# Build Java Application project
cd "$projectPath\server"
.\gradlew application:bootWar

# Build Java Dashboard project
.\gradlew dashboard:bootWar

# Build Java CronJobs project
.\gradlew cronjobs:bootWar

# Build Java Submissions project
.\gradlew submissions:bootWar

# Move files
mkdir $warPath
MOVE "$projectPath\server\applications\build\libs\*.war" "$warPath"
MOVE "$projectPath\server\dashboard\build\libs\*.war" "$warPath"
MOVE "$projectPath\server\cronjobs\build\libs\*.war" "$warPath"
MOVE "$projectPath\server\submissions\build\libs\*.war" "$warPath"
MOVE "$projectPath\client\war\spotlight.war" "$warPath"
MOVE "$projectPath\spotlight-mobile\war\spotlight-mobile.war" "$warPath"

# Update war.xml
cd "$warPath"
jar xvf spotlight.war WEB-INF\web.xml
REPLACE "$projectPath\client\web.xml" "$warPath\WEB-INF"
jar uvf spotlight.war WEB-INF\web.xml
# spotlight-mobile
cd "$warPath"
jar xvf spotlight-mobile.war WEB-INF\web.xml
REPLACE "$projectPath\spotlight-mobile\web.xml" "$warPath\WEB-INF"
jar uvf spotlight-mobile.war WEB-INF\web.xml

explorer.exe $warPath

cd "$projectPath" 
git reset --hard
