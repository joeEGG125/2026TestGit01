#Requires -Version 5.0

<#
.SYNOPSIS
    MFT File Upload Script for TFS CI Pipeline
.DESCRIPTION
    Uploads fep_app.tar files to MFT server for each configured server
.PARAMETER ConfigFile
    Configuration file path (default: cicd-config.json)
.EXAMPLE
    .\Upload_to_MFT.ps1
.EXAMPLE
    .\Upload_to_MFT.ps1 -ConfigFile "custom-upload-config.json"
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory=$false)]
    [string]$ConfigFile = "cicd-config.json"
)

# Error handling
$ErrorActionPreference = "Continue"
$ProgressPreference = "SilentlyContinue"

# 變數展開函數：支援 TFS 變數 $(VarName) 和環境變數 $env:VarName
function Expand-ConfigVariable {
    param([string]$Value)
    
    if ([string]::IsNullOrWhiteSpace($Value)) {
        return $Value
    }
    
    # 展開 TFS 樣式的變數 $(變數名稱)
    $Value = [System.Text.RegularExpressions.Regex]::Replace(
        $Value, 
        '\$\(([^)]+)\)', 
        {
            param($match)
            $varName = $match.Groups[1].Value
            
            # 先嘗試從環境變數取得（TFS 會將變數設為環境變數）
            $envValue = [System.Environment]::GetEnvironmentVariable($varName)
            if ($envValue) { 
                return $envValue 
            }
            
            # 嘗試將點號轉換為底線（TFS 變數格式：Build.SourcesDirectory -> BUILD_SOURCESDIRECTORY）
            $envVarName = $varName.Replace('.', '_').ToUpper()
            $envValue = [System.Environment]::GetEnvironmentVariable($envVarName)
            if ($envValue) { 
                return $envValue 
            }
            
            # 如果找不到，保持原樣
            return $match.Value
        }
    )
    
    # 展開 PowerShell 環境變數 $env:VarName
    try {
        $Value = $ExecutionContext.InvokeCommand.ExpandString($Value)
    } catch {
        # 如果展開失敗，保持原樣
    }
    
    return $Value
}

# Read configuration file
if (-not (Test-Path -Path $ConfigFile)) {
    Write-Host "✗ 找不到設定檔: $ConfigFile" -ForegroundColor Red
    Write-Host "請建立設定檔後再執行此腳本" -ForegroundColor Yellow
    exit 1
}

try {
    # 讀取設定檔並移除註解（支援 JSONC 格式）
    $rawContent = Get-Content -Path $ConfigFile -Raw
    
    # 移除單行註解 //
    $rawContent = $rawContent -replace "(?m)//.*$", ""
    
    # 移除多行註解 /* */
    $rawContent = $rawContent -replace "(?s)/\*.*?\*/", ""
    
    $config = $rawContent | ConvertFrom-Json
    Write-Host "✓ 成功讀取設定檔: $ConfigFile" -ForegroundColor Green
} catch {
    Write-Host "✗ 讀取設定檔失敗: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Load configuration values and expand variables
$BuildSourceDir = Expand-ConfigVariable $config.BuildSourceDir
$MFTDir = Expand-ConfigVariable $config.MFTDir
$MFTTargetDir = Expand-ConfigVariable $config.MFTTargetDir
$FtpIPAddress = Expand-ConfigVariable $config.FtpIPAddress
$FtpUserName = Expand-ConfigVariable $config.FtpUserName
$FtpPassword = Expand-ConfigVariable $config.FtpPassword
$ServerList = $config.ServerList_upload_to_MFT

# Validate required parameters
if ([string]::IsNullOrWhiteSpace($BuildSourceDir)) {
    Write-Host "✗ 設定檔中缺少 BuildSourceDir 參數" -ForegroundColor Red
    exit 1
}

if ([string]::IsNullOrWhiteSpace($MFTDir)) {
    Write-Host "✗ 設定檔中缺少 MFTDir 參數" -ForegroundColor Red
    exit 1
}

if ([string]::IsNullOrWhiteSpace($MFTTargetDir)) {
    Write-Host "✗ 設定檔中缺少 MFTTargetDir 參數" -ForegroundColor Red
    exit 1
}

if ($null -eq $ServerList -or $ServerList.Count -eq 0) {
    Write-Host "✗ 設定檔中缺少 ServerList_upload_to_MFT 或列表為空" -ForegroundColor Red
    exit 1
}

# Statistics
$script:UploadSuccess = 0
$script:UploadFailed = 0
$script:UploadSkipped = 0

# Helper Functions
function Write-Banner {
    param([string]$Message)
    Write-Host ""
    Write-Host "================================================================================" -ForegroundColor Cyan
    Write-Host $Message -ForegroundColor Cyan
    Write-Host "================================================================================" -ForegroundColor Cyan
}

function Write-SectionHeader {
    param([string]$Message)
    Write-Host ""
    Write-Host "[$Message]" -ForegroundColor Yellow
    Write-Host "------------------------------------------------------------" -ForegroundColor Yellow
}

function Write-Success {
    param([string]$Message)
    Write-Host "[V] $Message" -ForegroundColor Green
}

function Write-Failure {
    param([string]$Message)
    Write-Host "[X] $Message" -ForegroundColor Red
}

function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Cyan
}

function Write-Skip {
    param([string]$Message)
    Write-Host "[SKIP] $Message" -ForegroundColor Gray
}

# Main Functions
function Show-ScriptHeader {
    Write-Banner "MFT File Upload Script - Execution Start"
    Write-Host "Execution Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
    Write-Banner ""
}

function Show-TreeStructure {
    param(
        [string]$Path,
        [string]$Prefix = "",
        [bool]$IsLast = $true,
        [int]$MaxDepth = 5,
        [int]$FileDisplayDepth = 5,
        [int]$CurrentDepth = 0
    )
    
    if ($CurrentDepth -ge $MaxDepth) { return }
    
    try {
        if ($CurrentDepth -ge $FileDisplayDepth) {
            $items = Get-ChildItem -Path $Path -Directory -ErrorAction Stop | Sort-Object Name
        } else {
            $items = Get-ChildItem -Path $Path -ErrorAction Stop | Sort-Object { -not $_.PSIsContainer }, Name
        }
        
        $totalItems = $items.Count
        $counter = 0
        
        foreach ($item in $items) {
            $counter++
            $isLastItem = ($counter -eq $totalItems)
            
            if ($isLastItem) {
                $branch = "└─"
                $extension = "  "
            } else {
                $branch = "├─"
                $extension = "│ "
            }
            
            if ($item.PSIsContainer) {
                Write-Host "$Prefix$branch$($item.Name)" -ForegroundColor Cyan
                Show-TreeStructure -Path $item.FullName -Prefix "$Prefix$extension" -IsLast $isLastItem -MaxDepth $MaxDepth -FileDisplayDepth $FileDisplayDepth -CurrentDepth ($CurrentDepth + 1)
            } else {
                if ($CurrentDepth -lt $FileDisplayDepth) {
                    Write-Host "$Prefix$branch$($item.Name)" -ForegroundColor White
                }
            }
        }
    } catch {
        Write-Warning "Error reading directory: $_"
    }
}

function Test-OutputFolder {
    Write-Banner "OUTPUT Folder Structure Check"
    
    $outputPath = Join-Path $BuildSourceDir "OUTPUT"
    
    if (Test-Path $outputPath) {
        Write-Info "OUTPUT folder path: $outputPath"
        Write-Host ""
        
        $parentPath = Split-Path -Parent $outputPath
        $outputDirName = Split-Path -Leaf $outputPath
        
        Push-Location $parentPath
        try {
            Write-Host "$outputDirName\" -ForegroundColor Cyan
            Pop-Location
            Show-TreeStructure -Path $outputPath -MaxDepth 10 -FileDisplayDepth 4
            Push-Location $parentPath
        } catch {
            Write-Warning "Tree command failed: $_"
            Write-Host "$outputDirName\" -ForegroundColor Cyan
            Pop-Location
            Show-TreeStructure -Path $outputPath -MaxDepth 10 -FileDisplayDepth 4
            Push-Location $parentPath
        } finally {
            Pop-Location
        }
        
        Write-Host ""
        return $true
    } else {
        Write-Failure "OUTPUT folder does not exist: $outputPath"
        return $false
    }
}

function Get-FolderInfo {
    param([string]$Path)
    
    $info = @{
        Exists = $false
        Size = 0
        FileCount = 0
        SizeKB = "0"
    }
    
    if (Test-Path $Path) {
        $info.Exists = $true
        
        try {
            $size = (Get-ChildItem -Path $Path -Recurse -Force -ErrorAction SilentlyContinue | 
                     Measure-Object -Property Length -Sum -ErrorAction SilentlyContinue).Sum
            
            if ($null -ne $size) {
                $info.Size = $size
                $info.SizeKB = [math]::Round($size / 1KB, 2)
            }
            
            $fileCount = (Get-ChildItem -Path $Path -Recurse -File -ErrorAction SilentlyContinue).Count
            if ($null -ne $fileCount) {
                $info.FileCount = $fileCount
            }
        } catch {
            Write-Warning "Error calculating folder info: $_"
        }
    }
    
    return $info
}

function Show-ServerDetails {
    Write-Banner "Server fep-app Folder Details"
    
    foreach ($server in $ServerList) {
        Write-SectionHeader "Server: $server"
        
        $appPath = Join-Path $BuildSourceDir "OUTPUT\$server\fep-app"
        $folderInfo = Get-FolderInfo -Path $appPath
        
        if ($folderInfo.Exists) {
            Write-Success "Folder exists"
            Write-Host "Path: $appPath"
            Write-Host "Folder Size: $($folderInfo.SizeKB) KB"
            Write-Host "File Count: $($folderInfo.FileCount) files"
        } else {
            Write-Failure "Folder does not exist"
            Write-Host "Path: $appPath"
        }
        
        switch ($server) {
            "FEPHISDB" { $fileName = "fep_his_app.tar" }
            Default    { $fileName = "fep_app.tar" }
        }
        $tarPath = Join-Path $BuildSourceDir "OUTPUT\$server\$fileName"
        
        if (Test-Path $tarPath) {
            $tarSize = (Get-Item $tarPath).Length
            Write-Success "TAR file exists"
            Write-Host "TAR Size: $tarSize bytes ($([math]::Round($tarSize / 1MB, 2)) MB)"
        } else {
            Write-Failure "TAR file does not exist"
        }
        
        Write-Host "------------------------------------------------------------"
    }
    
    Write-SectionHeader "WEB Files Check"
    
    $webWarPath = Join-Path $BuildSourceDir "fep-war\fep-web.war"
    Write-Host "FEPWEB WAR: $webWarPath"
    if (Test-Path $webWarPath) {
        $webSize = (Get-Item $webWarPath).Length
        Write-Success "File exists - $webSize bytes ($([math]::Round($webSize / 1MB, 2)) MB)"
    } else {
        Write-Failure "File does not exist"
    }
    
    Write-Host "------------------------------------------------------------"
}

function Invoke-MFTUpload {
    Write-Banner "Start Uploading Files to MFT"
    
    if (-not (Test-Path $MFTDir)) {
        Write-Failure "MFT directory does not exist: $MFTDir"
        return $false
    }
    
    $ftmscmdPath = Join-Path $MFTDir "ftmscmd.exe"
    if (-not (Test-Path $ftmscmdPath)) {
        Write-Failure "ftmscmd.exe not found: $ftmscmdPath"
        return $false
    }
    
    Write-Info "Changing to MFT directory: $MFTDir"
    Push-Location $MFTDir
    
    try {
        foreach ($server in $ServerList) {
            Write-SectionHeader "Upload Task: $server"
            switch ($server) {
                "FEPHISDB" { $fileName = "fep_his_app.tar" }
                Default    { $fileName = "fep_app.tar" }
            }
            $tarPath = Join-Path $BuildSourceDir "OUTPUT\$server\$fileName"
            
            if (Test-Path $tarPath) {
                Write-Info "Starting upload..."

                switch ($server) {
                    "FEPHISDB" { $fileName = "fep_his_app.tar" }
                    Default    { $fileName = "fep_app.tar" }
                }
                $remotePath = "$MFTTargetDir/$server/$fileName"
                
                $arguments = @(
                    "/S"
                    "/LU:$FtpIPAddress"
                    "/RI:$FtpUserName"
                    "/RW:$FtpPassword"
                    "`"$tarPath`""
                    "`"$remotePath`""
                )
                
                Write-Host "Command: ftmscmd.exe $($arguments -join ' ')" -ForegroundColor DarkGray
                
                try {
                    $process = Start-Process -FilePath $ftmscmdPath `
                                            -ArgumentList $arguments `
                                            -NoNewWindow `
                                            -Wait `
                                            -PassThru `
                                            -RedirectStandardOutput "$env:TEMP\mft_stdout_$server.txt" `
                                            -RedirectStandardError "$env:TEMP\mft_stderr_$server.txt"
                    
                    if (Test-Path "$env:TEMP\mft_stdout_$server.txt") {
                        $stdout = Get-Content "$env:TEMP\mft_stdout_$server.txt" -Raw
                        if (-not [string]::IsNullOrWhiteSpace($stdout)) {
                            Write-Host $stdout
                        }
                    }
                    
                    if (Test-Path "$env:TEMP\mft_stderr_$server.txt") {
                        $stderr = Get-Content "$env:TEMP\mft_stderr_$server.txt" -Raw
                        if (-not [string]::IsNullOrWhiteSpace($stderr)) {
                            Write-Host $stderr -ForegroundColor Yellow
                        }
                    }
                    
                    if ($process.ExitCode -eq 0) {
                        Write-Success "Upload succeeded"
                        $script:UploadSuccess++
                    } else {
                        Write-Failure "Upload failed (Exit Code: $($process.ExitCode))"
                        $script:UploadFailed++
                    }
                    
                    Remove-Item "$env:TEMP\mft_stdout_$server.txt" -ErrorAction SilentlyContinue
                    Remove-Item "$env:TEMP\mft_stderr_$server.txt" -ErrorAction SilentlyContinue
                } catch {
                    Write-Failure "Upload failed with exception: $_"
                    $script:UploadFailed++
                }
            } else {
                Write-Skip "File does not exist, skipping upload"
                $script:UploadSkipped++
            }
            
            Write-Host "------------------------------------------------------------"
        }
        
        return $true
    } finally {
        Pop-Location
    }
}

function Invoke-WebFileUpload {
    Write-Banner "Start Uploading WEB Files to MFT"
    
    if (-not (Test-Path $MFTDir)) {
        Write-Failure "MFT directory does not exist: $MFTDir"
        return $false
    }
    
    $ftmscmdPath = Join-Path $MFTDir "ftmscmd.exe"
    if (-not (Test-Path $ftmscmdPath)) {
        Write-Failure "ftmscmd.exe not found: $ftmscmdPath"
        return $false
    }
    
    Write-Info "Changing to MFT directory: $MFTDir"
    Push-Location $MFTDir
    
    try {
        $specialUploads = @(
            @{
                Name = "FEPWEB"
                LocalPath = Join-Path $BuildSourceDir "fep-war\fep-web.war"
                RemotePath = "$MFTTargetDir/FEPWEB/fep-web.war"
            }
        )
        
        foreach ($upload in $specialUploads) {
            Write-SectionHeader "Upload Task: $($upload.Name)"
            
            if (Test-Path $upload.LocalPath) {
                $fileSize = (Get-Item $upload.LocalPath).Length
                Write-Info "File found: $($upload.LocalPath)"
                Write-Info "File size: $fileSize bytes ($([math]::Round($fileSize / 1MB, 2)) MB)"
                Write-Info "Starting upload..."
                
                $arguments = @(
                    "/S"
                    "/LU:$FtpIPAddress"
                    "/RI:$FtpUserName"
                    "/RW:$FtpPassword"
                    "`"$($upload.LocalPath)`""
                    "`"$($upload.RemotePath)`""
                )
                
                Write-Host "Command: ftmscmd.exe $($arguments -join ' ')" -ForegroundColor DarkGray
                
                try {
                    $process = Start-Process -FilePath $ftmscmdPath `
                                            -ArgumentList $arguments `
                                            -NoNewWindow `
                                            -Wait `
                                            -PassThru `
                                            -RedirectStandardOutput "$env:TEMP\mft_stdout_$($upload.Name).txt" `
                                            -RedirectStandardError "$env:TEMP\mft_stderr_$($upload.Name).txt"
                    
                    if (Test-Path "$env:TEMP\mft_stdout_$($upload.Name).txt") {
                        $stdout = Get-Content "$env:TEMP\mft_stdout_$($upload.Name).txt" -Raw
                        if (-not [string]::IsNullOrWhiteSpace($stdout)) {
                            Write-Host $stdout
                        }
                    }
                    
                    if (Test-Path "$env:TEMP\mft_stderr_$($upload.Name).txt") {
                        $stderr = Get-Content "$env:TEMP\mft_stderr_$($upload.Name).txt" -Raw
                        if (-not [string]::IsNullOrWhiteSpace($stderr)) {
                            Write-Host $stderr -ForegroundColor Yellow
                        }
                    }
                    
                    if ($process.ExitCode -eq 0) {
                        Write-Success "Upload succeeded"
                        $script:UploadSuccess++
                    } else {
                        Write-Failure "Upload failed (Exit Code: $($process.ExitCode))"
                        $script:UploadFailed++
                    }
                    
                    Remove-Item "$env:TEMP\mft_stdout_$($upload.Name).txt" -ErrorAction SilentlyContinue
                    Remove-Item "$env:TEMP\mft_stderr_$($upload.Name).txt" -ErrorAction SilentlyContinue
                } catch {
                    Write-Failure "Upload failed with exception: $_"
                    $script:UploadFailed++
                }
            } else {
                Write-Skip "File does not exist, skipping upload: $($upload.LocalPath)"
                $script:UploadSkipped++
            }
            
            Write-Host "------------------------------------------------------------"
        }
        
        return $true
    } finally {
        Pop-Location
    }
}

function Show-Summary {
    Write-Host ""
    Write-Banner "Execution Summary"
    Write-Host "Upload Success: $UploadSuccess" -ForegroundColor Green
    Write-Host "Upload Failed: $UploadFailed" -ForegroundColor $(if ($UploadFailed -gt 0) { "Red" } else { "White" })
    Write-Host "Upload Skipped: $UploadSkipped" -ForegroundColor Gray
    Write-Host "Completion Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
    Write-Banner ""
    Write-Host ""
    
    if ($UploadFailed -gt 0) {
        Write-Host "[CONCLUSION] Script completed, but $UploadFailed file(s) failed to upload" -ForegroundColor Red
        return $false
    } elseif ($UploadSuccess -eq 0 -and $UploadSkipped -gt 0) {
        Write-Host "[CONCLUSION] Script completed, but no files were uploaded (all skipped)" -ForegroundColor Yellow
        return $false
    } elseif (-not ($uploadResult -and $webUploadResult)) {
        Write-Host "[CONCLUSION] Script failed, MFT directory or ftmscmd.exe not found" -ForegroundColor Yellow
        return $false
    } else {
        Write-Host "[CONCLUSION] Script completed successfully" -ForegroundColor Green
        return $true
    }
}

function Main {
    try {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "TFS CI Pipeline - MFT Upload Process" -ForegroundColor Green
        Write-Host "Start Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""

        Write-Host "Configuration File: $ConfigFile" -ForegroundColor Cyan
        Write-Host "Parameters:" -ForegroundColor Cyan
        Write-Host "  BuildSourceDir = $BuildSourceDir"
        Write-Host "  MFTDir         = $MFTDir"
        Write-Host "  MFTTargetDir   = $MFTTargetDir"
        Write-Host "  FtpIPAddress   = $(if ([string]::IsNullOrEmpty($FtpIPAddress)) { '(Not Set)' } else { $FtpIPAddress })"
        Write-Host "  FtpUserName    = $(if ([string]::IsNullOrEmpty($FtpUserName)) { '(Not Set)' } else { $FtpUserName })"
        Write-Host "  FtpPassword    = $(if ([string]::IsNullOrEmpty($FtpPassword)) { '(Not Set)' } else { '***' })"
        Write-Host "  ServerList     = $($ServerList -join ', ')"
        Write-Host ""
        
        Show-ScriptHeader
        
        $outputExists = Test-OutputFolder
        
        Show-ServerDetails
        
        $tarFilesExist = $false
        foreach ($server in $ServerList) {
            $tarPath = Join-Path $BuildSourceDir "OUTPUT\$server\fep_app.tar"
            if (Test-Path $tarPath) {
                $tarFilesExist = $true
                break
            }
        }
        
        $webWarPath = Join-Path $BuildSourceDir "fep-war\fep-web.war"
        if (Test-Path $webWarPath) {
            $tarFilesExist = $true
        }
        
        if (-not $tarFilesExist) {
            Write-Host ""
            Write-Banner "WARNING: No Files Found for Upload"
            Write-Warning "No TAR or WAR files found for upload"
            Write-Warning "Please verify the build process completed successfully"
            Write-Host ""
            
            $script:UploadSkipped = $ServerList.Count + 2
            
            Show-Summary
            exit 1
        }
        
        $credentialsValid = -not ([string]::IsNullOrEmpty($FtpIPAddress) -or 
                                   [string]::IsNullOrEmpty($FtpUserName) -or 
                                   [string]::IsNullOrEmpty($FtpPassword))
        
        if (-not $credentialsValid) {
            Write-Host ""
            Write-Banner "WARNING: FTP Credentials Not Set"
            Write-Warning "FTP credentials are not set in configuration file"
            Write-Warning "Please add the following to your config file:"
            Write-Warning '  "FtpIPAddress": "your_ip"'
            Write-Warning '  "FtpUserName": "your_username"'
            Write-Warning '  "FtpPassword": "your_password"'
            Write-Host ""
            
            foreach ($server in $ServerList) {
                $tarPath = Join-Path $BuildSourceDir "OUTPUT\$server\fep_app.tar"
                if (Test-Path $tarPath) {
                    $script:UploadSkipped++
                }
            }
            if (Test-Path $webWarPath) { $script:UploadSkipped++ }
            
            Show-Summary
            exit 1
        }
        
        $uploadResult = Invoke-MFTUpload
        
        $webUploadResult = Invoke-WebFileUpload
        
        $success = Show-Summary
        
        if ($success) {
            exit 0
        } else {
            exit 1
        }
    } catch {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Red
        Write-Host "Error Occurred During Execution" -ForegroundColor Red
        Write-Host "========================================" -ForegroundColor Red
        Write-Host "Error Message: $_" -ForegroundColor Red
        Write-Host "Stack Trace:" -ForegroundColor Red
        Write-Host $_.ScriptStackTrace -ForegroundColor Red
        Write-Host ""
        
        Show-Summary
        exit 1
    }
}

Main