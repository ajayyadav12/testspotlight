# IMPORTANT: Execute inside CICD folder
# Script Updates FE's package.json, BE's build.gralde and creates new DB folder. Creates new branch, commit and push
Write-Output "**IMPORTANT: Execute inside /CICD folder**"
$oldVersion = Read-Host -Prompt 'Old Version'
$newVersion = Read-Host -Prompt 'New Version'

$newBranch = Read-Host -Prompt 'Want to create new branch for you? Y/N'
if ($newBranch -eq "Y") {
    git checkout -b temp-rel-$newVersion
}

cd ..
(Get-Content -path "client\package.json" -Raw) -replace "`"version`": `"$oldVersion`"", "`"version`": `"$newVersion`"" | Set-Content -Path "client\package.json"

(Get-Content -path "server\dashboard\build.gradle" -Raw) -replace "version '$oldVersion'", "version '$newVersion'" | Set-Content -Path "server\dashboard\build.gradle"
(Get-Content -path "server\common\build.gradle" -Raw) -replace "version '$oldVersion'", "version '$newVersion'" | Set-Content -Path "server\common\build.gradle"
(Get-Content -path "server\applications\build.gradle" -Raw) -replace "version '$oldVersion'", "version '$newVersion'" | Set-Content -Path "server\applications\build.gradle"
(Get-Content -path "server\cronjobs\build.gradle" -Raw) -replace "version '$oldVersion'", "version '$newVersion'" | Set-Content -Path "server\cronjobs\build.gradle"
(Get-Content -path "server\submissions\build.gradle" -Raw) -replace "version '$oldVersion'", "version '$newVersion'" | Set-Content -Path "server\submissions\build.gradle"

mkdir db\$newVersion
Write-Output "Run all the .sql queries when moving to $newVersion version" > db\$newVersion\README.md

git add *
$commit = Read-Host -Prompt 'Verify changes. Want to commit? Y/N'
if ($commit -eq "Y") {
    git commit -m "Init $newVersion"
    $push = Read-Host -Prompt 'Want to push to remote? Y/N'
    if ($push -eq "Y") {
        git push --set-upstream origin temp-rel-$newVersion
    }
}

