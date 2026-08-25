#Requires -Version 5.0

<#
.SYNOPSIS
    TFS CI Pipeline - Deploy Script
.DESCRIPTION
    Complete deployment pipeline for Config, Shell, and BatchTask files
.PARAMETER ConfigFile
    Configuration file path (default: cicd-config.json)
.EXAMPLE
    .\process_shell_config.ps1
.EXAMPLE
    .\process_shell_config.ps1 -ConfigFile "custom-config.json"
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory=$false)]
    [string]$ConfigFile = "cicd-config.json"
)

# Error handling
$ErrorActionPreference = "Stop"
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
$ConfigHome = Expand-ConfigVariable $config.ConfigHome
$ShellHome = Expand-ConfigVariable $config.ShellHome
$script:ServerList = $config.ServerList_shell_config

# Validate required parameters
if ([string]::IsNullOrWhiteSpace($BuildSourceDir)) {
    Write-Host "✗ 設定檔中缺少 BuildSourceDir 參數" -ForegroundColor Red
    exit 1
}

if ([string]::IsNullOrWhiteSpace($ConfigHome)) {
    Write-Host "✗ 設定檔中缺少 ConfigHome 參數" -ForegroundColor Red
    exit 1
}

if ([string]::IsNullOrWhiteSpace($ShellHome)) {
    Write-Host "✗ 設定檔中缺少 ShellHome 參數" -ForegroundColor Red
    exit 1
}

if ($null -eq $ServerList -or $ServerList.Count -eq 0) {
    Write-Host "✗ 設定檔中缺少 ServerList_shell_config 或列表為空" -ForegroundColor Red
    exit 1
}

# Build full paths from parameters
$script:SOURCE_BASE_DIR = $BuildSourceDir
$script:CONFIG_HOME = Join-Path $SOURCE_BASE_DIR $ConfigHome
$script:SHELL_HOME = Join-Path $SOURCE_BASE_DIR $ShellHome
$script:CICD_HOME = Join-Path $SOURCE_BASE_DIR "fep-cicd"

# Use custom output dir or default to OUTPUT under source base
$script:OUTPUT_DIR = Join-Path $SOURCE_BASE_DIR "OUTPUT"

# Statistics
$script:TotalSuccess = 0
$script:TotalFailed = 0

# Helper Functions
function Write-Banner {
    param([string]$Message)
    Write-Host ""
    Write-Host "=================="
    Write-Host $Message
    Write-Host "=================="
    Write-Host ""
}

function Write-Section {
    param([string]$Message)
    Write-Host ""
    Write-Host "+-------------------"
    Write-Host "| $Message"
    Write-Host "+-------------------"
}

function Write-Success {
    param([string]$Message)
    Write-Host "  [SUCCESS] $Message"
}

function Write-Warning {
    param([string]$Message)
    Write-Host "  [WARNING] $Message"
}

function Write-Failure {
    param([string]$Message)
    Write-Host "  [FAILED] $Message"
}

function Write-Info {
    param([string]$Message)
    Write-Host "  [OK] $Message"
}

function Write-Skip {
    param([string]$Message)
    Write-Host "  [SKIP] $Message"
}

# PART 1: Environment Check and Directory Creation
function Test-Environment {
    Write-Banner "TFS CI Pipeline PART1 - Environment Check & Directory Setup"
    Write-Host "Start Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
    Write-Host ""

    Write-Host "[Step 1/3] Verify Environment Variables" -ForegroundColor Cyan
    Write-Host "------------------------"
    Write-Host "SOURCE_BASE_DIR = $SOURCE_BASE_DIR"
    Write-Host "CONFIG_HOME     = $CONFIG_HOME"
    Write-Host "SHELL_HOME      = $SHELL_HOME"
    Write-Host "CICD_HOME       = $CICD_HOME"
    Write-Host "OUTPUT_DIR      = $OUTPUT_DIR"
    Write-Host ""

    $dirCheckFailed = $false

    # Check if source base directory exists
    if (-not (Test-Path $SOURCE_BASE_DIR)) {
        Write-Warning "Source base directory not found: $SOURCE_BASE_DIR"
        $dirCheckFailed = $true
    } else {
        Write-Info "Source base directory exists"
    }

    if (-not (Test-Path $CONFIG_HOME)) {
        Write-Warning "Config directory not found: $CONFIG_HOME"
        $dirCheckFailed = $true
    } else {
        Write-Info "Config directory exists"
    }

    if (-not (Test-Path $SHELL_HOME)) {
        Write-Warning "Shell directory not found: $SHELL_HOME"
        $dirCheckFailed = $true
    } else {
        Write-Info "Shell directory exists"
    }

    if (-not (Test-Path $CICD_HOME)) {
        Write-Warning "CICD directory not found: $CICD_HOME"
        $dirCheckFailed = $true
    } else {
        Write-Info "CICD directory exists"
    }

    if ($dirCheckFailed) {
        throw "Source directory check failed"
    }

    Write-Host ""
}

function Test-ListFiles {
    Write-Host "[Step 2/3] Check List Files"
    Write-Host "------------------------"

    $configListCount = 0
    $shellListCount = 0
    $batchTaskCount = 0
    $hisBatchTaskCount = 0

    # Check Config Lists
    Write-Host "Config List Check:"
    foreach ($server in $ServerList) {
        $configList = Join-Path $CONFIG_HOME "${server}_config_list.txt"
        if (Test-Path $configList) {
            Write-Info "${server}_config_list.txt"
            $configListCount++
        } else {
            Write-Host "  [X] ${server}_config_list.txt (not found)"
        }
    }

    # Check Shell Lists
    Write-Host ""
    Write-Host "Shell List Check:"
    foreach ($server in $ServerList) {
        $shellList = Join-Path $SHELL_HOME "${server}_shell_list.txt"
        if (Test-Path $shellList) {
            Write-Info "${server}_shell_list.txt"
            $shellListCount++
        } else {
            Write-Host "  [X] ${server}_shell_list.txt (not found)"
        }
    }

    # Check BatchTask Lists
    Write-Host ""
    Write-Host "BatchTask List Check:"
    $batchTaskList = Join-Path $CICD_HOME "FEP_BatchTask_list.txt"
    if (Test-Path $batchTaskList) {
        Write-Info "FEP_BatchTask_list.txt"
        $batchTaskCount++
    } else {
        Write-Host "  [X] FEP_BatchTask_list.txt (not found)"
    }

    $hisBatchTaskList = Join-Path $CICD_HOME "FEPHIS_BatchTask_list.txt"
    if (Test-Path $hisBatchTaskList) {
        Write-Info "FEPHIS_BatchTask_list.txt"
        $hisBatchTaskCount++
    } else {
        Write-Host "  [X] FEPHIS_BatchTask_list.txt (not found)"
    }

    # Warnings
    if ($configListCount -eq 0) {
        Write-Warning "No config list files found, will skip config deployment"
    }
    if ($shellListCount -eq 0) {
        Write-Warning "No shell list files found, will skip shell deployment"
    }
    if ($batchTaskCount -eq 0) {
        Write-Warning "No batchTask list files found, will skip batchTask deployment"
    }
    if ($hisBatchTaskCount -eq 0) {
        Write-Warning "No his_batchTask list files found, will skip His batchTask deployment"
    }

    Write-Success "List file check completed"
    Write-Host ""

    return @{
        ConfigCount = $configListCount
        ShellCount = $shellListCount
        BatchTaskCount = $batchTaskCount
        HisBatchTaskCount = $hisBatchTaskCount
    }
}

function New-OutputDirectories {
    Write-Host "[Step 3/3] Create Output Directory Structure"
    Write-Host "------------------------"

    # Create main output directory
    if (-not (Test-Path $OUTPUT_DIR)) {
        Write-Host "[CREATE] Main output directory: $OUTPUT_DIR"
        New-Item -Path $OUTPUT_DIR -ItemType Directory -Force | Out-Null
    }

    # Create config directories for each server
    foreach ($server in $ServerList) {
        $configDir = Join-Path $OUTPUT_DIR "$server\fep-app\config"
        if (-not (Test-Path $configDir)) {
            Write-Host "[CREATE] $server\fep-app\config directory"
            New-Item -Path $configDir -ItemType Directory -Force | Out-Null
        }
    }

    # Create BatchTask directories
    $batchTaskList = Join-Path $CICD_HOME "FEP_BatchTask_list.txt"
    if (Test-Path $batchTaskList) {
        foreach ($server in $ServerList) {
            $batchTaskDir = Join-Path $OUTPUT_DIR "$server\fep-app\fep-batch-task"
            if (-not (Test-Path $batchTaskDir)) {
                Write-Host "[CREATE] $server\fep-app\fep-batch-task directory"
                New-Item -Path $batchTaskDir -ItemType Directory -Force | Out-Null
            }
        }
    } else {
        Write-Skip "BatchTask list file not found"
    }

    # Create HIS BatchTask directory
    $hisBatchTaskList = Join-Path $CICD_HOME "FEPHIS_BatchTask_list.txt"
    if (Test-Path $hisBatchTaskList) {
    } else {
        Write-Skip "HIS BatchTask list file not found"
    }
    $hisBatchTaskDir = Join-Path $OUTPUT_DIR "FEPHISDB\fep-app\BatchTask"
    if (-not (Test-Path $hisBatchTaskDir)) {
        Write-Host "[CREATE] FEPHISDB\fep-app\BatchTask directory"
        New-Item -Path $hisBatchTaskDir -ItemType Directory -Force | Out-Null
    }

    Write-Host ""
    Write-Success "Output directory structure created"
    Write-Host ""
}

# PART 2: Config and Shell Deployment
function Deploy-ConfigFiles {
    Write-Banner "Deploy Config Files"

    foreach ($server in $ServerList) {
        Write-Section "Server: $server - Config Deployment"

        $configList = Join-Path $CONFIG_HOME "${server}_config_list.txt"
        
        if (-not (Test-Path $configList)) {
            Write-Skip "List file not found"
            Write-Host ""
            continue
        }

        $sourcePath = Join-Path $CONFIG_HOME $server
        $targetPath = Join-Path $OUTPUT_DIR "$server\fep-app\config"

        $serverSuccess = 0
        $serverFailed = 0

        # Read list file
        $files = Get-Content $configList -Encoding UTF8 | Where-Object { 
            $_.Trim() -ne "" -and -not $_.Trim().StartsWith("#") 
        }

        foreach ($file in $files) {
            $sourceFile = Join-Path $sourcePath $file
            $targetFile = Join-Path $targetPath $file

            if (Test-Path $sourceFile) {
                try {
                    Copy-Item -Path $sourceFile -Destination $targetFile -Force -ErrorAction Stop
                    Write-Success $file
                    $serverSuccess++
                    $script:TotalSuccess++
                } catch {
                    Write-Failure "Copy failed - $file"
                    $serverFailed++
                    $script:TotalFailed++
                }
            } else {
                Write-Warning "Source file not found - $file"
                $serverFailed++
                $script:TotalFailed++
            }
        }

        Write-Host "  Statistics: Success $serverSuccess, Failed $serverFailed"
        Write-Host ""
    }
}

function Deploy-ShellFiles {
    Write-Banner "Deploy Shell Files"

    foreach ($server in $ServerList) {
        Write-Section "Server: $server - Shell Deployment"

        $shellList = Join-Path $SHELL_HOME "${server}_shell_list.txt"
        
        if (-not (Test-Path $shellList)) {
            Write-Skip "Server list not found"
            Write-Host ""
            continue
        }

        $sourcePath = Join-Path $SHELL_HOME $server
        $targetPath = Join-Path $OUTPUT_DIR "$server\fep-app"

        $serverSuccess = 0
        $serverFailed = 0

        # Read list file
        $files = Get-Content $shellList -Encoding UTF8 | Where-Object { 
            $_.Trim() -ne "" -and -not $_.Trim().StartsWith("#") 
        }

        foreach ($relPath in $files) {
            $sourceFile = Join-Path $sourcePath $relPath
            $targetFile = Join-Path $targetPath $relPath

            if (Test-Path $sourceFile) {
                # Ensure target directory exists
                $targetDir = Split-Path -Parent $targetFile
                if (-not (Test-Path $targetDir)) {
                    New-Item -Path $targetDir -ItemType Directory -Force | Out-Null
                }

                try {
                    Copy-Item -Path $sourceFile -Destination $targetFile -Force -ErrorAction Stop
                    Write-Success $relPath
                    $serverSuccess++
                    $script:TotalSuccess++
                } catch {
                    Write-Failure "Copy failed - $relPath"
                    $serverFailed++
                    $script:TotalFailed++
                }
            } else {
                Write-Warning "Source file not found - $relPath"
                Write-Host "          Path: $sourceFile"
                $serverFailed++
                $script:TotalFailed++
            }
        }

        Write-Host "  Statistics: Success $serverSuccess, Failed $serverFailed"
        Write-Host ""
    }
}

# PART 3: BatchTask Deployment
function Deploy-BatchTaskFiles {
    Write-Banner "Deploy Batch Task Files"

    # FEP BatchTask Deployment
    foreach ($server in $ServerList) {
        Write-Section "Server: $server - Batch Task Deployment"

        $batchTaskList = Join-Path $CICD_HOME "FEP_BatchTask_list.txt"
        
        if (-not (Test-Path $batchTaskList)) {
            Write-Skip "List file not found"
            Write-Host ""
            continue
        }

        $sourcePath = Join-Path $SOURCE_BASE_DIR "fep-assembly-batch-task"
        $targetPath = Join-Path $OUTPUT_DIR "$server\fep-app\fep-batch-task"

        $serverSuccess = 0
        $serverFailed = 0

        # Read list file
        $files = Get-Content $batchTaskList -Encoding UTF8 | Where-Object { 
            $_.Trim() -ne "" -and -not $_.Trim().StartsWith("#") 
        }

        foreach ($file in $files) {
            $sourceFile = Join-Path $sourcePath $file
            $targetFile = Join-Path $targetPath $file

            if (Test-Path $sourceFile) {
                try {
                    Copy-Item -Path $sourceFile -Destination $targetFile -Force -ErrorAction Stop
                    Write-Success $file
                    $serverSuccess++
                    $script:TotalSuccess++
                } catch {
                    Write-Failure "Copy failed - $file"
                    $serverFailed++
                    $script:TotalFailed++
                }
            } else {
                Write-Warning "Source file not found - $file"
                $serverFailed++
                $script:TotalFailed++
            }
        }

        Write-Host "  Statistics: Success $serverSuccess, Failed $serverFailed"
        Write-Host ""
    }

    # FEPHIS BatchTask Deployment
    Write-Section "Server: FEPHISDB - Batch Task Deployment"

    $hisBatchTaskList = Join-Path $CICD_HOME "FEPHIS_BatchTask_list.txt"
    
    if (-not (Test-Path $hisBatchTaskList)) {
        Write-Skip "List file not found"
        Write-Host ""
        return
    }

    $sourcePath = Join-Path $SOURCE_BASE_DIR "fep-assembly-batch-task"
    $targetPath = Join-Path $OUTPUT_DIR "FEPHISDB\fep-app\BatchTask"

    $serverSuccess = 0
    $serverFailed = 0

    # Read list file
    $files = Get-Content $hisBatchTaskList -Encoding UTF8 | Where-Object { 
        $_.Trim() -ne "" -and -not $_.Trim().StartsWith("#") 
    }

    foreach ($file in $files) {
        $sourceFile = Join-Path $sourcePath $file
        $targetFile = Join-Path $targetPath $file

        if (Test-Path $sourceFile) {
            try {
                Copy-Item -Path $sourceFile -Destination $targetFile -Force -ErrorAction Stop
                Write-Success $file
                $serverSuccess++
                $script:TotalSuccess++
            } catch {
                Write-Failure "Copy failed - $file"
                $serverFailed++
                $script:TotalFailed++
            }
        } else {
            Write-Warning "Source file not found - $file"
            $serverFailed++
            $script:TotalFailed++
        }
    }

    Write-Host "  Statistics: Success $serverSuccess, Failed $serverFailed"
    Write-Host ""
}

# Main Execution
function Main {
    try {
        Write-Host ""
        Write-Host "========================================"
        Write-Host "TFS CI Pipeline - Complete Deployment"
        Write-Host "Start Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
        Write-Host "========================================"
        Write-Host ""

        # Display Configuration
        Write-Host "Configuration File: $ConfigFile"
        Write-Host "Parameters:"
        Write-Host "  BuildSourceDir = $BuildSourceDir"
        Write-Host "  ConfigHome     = $ConfigHome"
        Write-Host "  ShellHome      = $ShellHome"
        Write-Host "  ServerList     = $($ServerList.Count) servers"
        Write-Host ""

        # PART 1: Environment Check and Directory Creation
        Test-Environment
        $listStats = Test-ListFiles
        New-OutputDirectories

        Write-Banner "PART1 Completed"
        Write-Host "Environment Check: Passed"
        Write-Host "List Check: Config $($listStats.ConfigCount)/$($ServerList.Count), Shell $($listStats.ShellCount)/$($ServerList.Count), BatchTask $($listStats.BatchTaskCount)/1, HIS BatchTask $($listStats.HisBatchTaskCount)/1"
        Write-Host "Directory Creation: Completed"
        Write-Host ""

        # PART 2: Config and Shell Deployment
        Deploy-ConfigFiles
        Deploy-ShellFiles

        # PART 3: BatchTask Deployment
        Deploy-BatchTaskFiles

        # Final Statistics
        Write-Banner "Deployment Completed"
        Write-Host "Total Success: $TotalSuccess files"
        Write-Host "Total Failed: $TotalFailed files"
        Write-Host ""

        if ($TotalFailed -gt 0) {
            Write-Warning "Some files failed to deploy, please check error messages above"
            exit 1
        } else {
            Write-Success "All files deployed successfully!"
            exit 0
        }
    } catch {
        Write-Host ""
        Write-Host "========================================"
        Write-Host "Error Occurred During Execution"
        Write-Host "========================================"
        Write-Host "Error Message: $_"
        Write-Host "Error Location: $($_.InvocationInfo.PositionMessage)"
        Write-Host ""
        exit 1
    }
}

# Execute Main
Main