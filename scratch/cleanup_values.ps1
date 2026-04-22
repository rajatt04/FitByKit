[xml]$lint = Get-Content app\build\reports\lint-results-debug.xml
$unused_strings = @()
$unused_dimens = @()
$unused_styles = @()

foreach ($issue in $lint.issues.issue) {
    if ($issue.id -eq "UnusedResources") {
        if ($issue.message -match "`R\.string\.([^`]+)`") {
            $unused_strings += $matches[1]
        }
        if ($issue.message -match "`R\.dimen\.([^`]+)`") {
            $unused_dimens += $matches[1]
        }
        if ($issue.message -match "`R\.style\.([^`]+)`") {
            $unused_styles += $matches[1]
        }
    }
}

function Clean-File {
    param($FilePath, $UnusedNames, $TagType)
    $lines = Get-Content $FilePath
    $new_lines = @()
    foreach ($line in $lines) {
        $skip = $false
        if ($line -match "<$TagType name=`"([^`"]+)`"") {
            $name = $matches[1]
            if ($UnusedNames -contains $name) {
                $skip = $true
            }
        }
        if (-not $skip) {
            $new_lines += $line
        }
    }
    $new_lines | Set-Content $FilePath -Encoding UTF8
    Write-Host "Cleaned $FilePath"
}

Clean-File -FilePath "app\src\main\res\values\strings.xml" -UnusedNames $unused_strings -TagType "string"
Clean-File -FilePath "app\src\main\res\values\dimens.xml" -UnusedNames $unused_dimens -TagType "dimen"
Clean-File -FilePath "app\src\main\res\values\themes.xml" -UnusedNames $unused_styles -TagType "style"
