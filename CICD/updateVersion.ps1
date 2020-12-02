# IMPORTANT: Execute inside CICD folder
# Script Updates FE's package.json, BE's build.gralde and creates new DB folder
$oldVersion = Read-Host -Prompt 'Old Version'
$newVersion = Read-Host -Prompt 'New Version'

cd ..
(Get-Content -path "client\package.json" -Raw) -replace $oldVersion, $newVersion | Set-Content -Path "client\package.json"

(Get-Content -path "server\dashboard\build.gradle" -Raw) -replace $oldVersion, $newVersion | Set-Content -Path "server\dashboard\build.gradle"
(Get-Content -path "server\common\build.gradle" -Raw) -replace $oldVersion, $newVersion | Set-Content -Path "server\common\build.gradle"
(Get-Content -path "server\applications\build.gradle" -Raw) -replace $oldVersion, $newVersion | Set-Content -Path "server\applications\build.gradle"
(Get-Content -path "server\cronjobs\build.gradle" -Raw) -replace $oldVersion, $newVersion | Set-Content -Path "server\cronjobs\build.gradle"
(Get-Content -path "server\build.gradle" -Raw) -replace $oldVersion, $newVersion | Set-Content -Path "server\build.gradle"

Copy-Item -path "db\$oldVersion" -destination "db\$newVersion" -Recurse 
(Get-Content -path "db\$newVersion\README.md" -Raw) -replace $oldVersion, $newVersion | Set-Content -Path "db\$newVersion\README.md"