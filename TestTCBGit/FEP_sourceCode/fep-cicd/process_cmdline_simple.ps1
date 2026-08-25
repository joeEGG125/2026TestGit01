# TFS CI Pipeline Script
# 檢查指定伺服器的 fep-batch-cmdline-simple 資料夾是否存在

[CmdletBinding()]
param(
    [Parameter(Mandatory=$false)]
    [string]$ConfigFile = "cicd-config.json"
)

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

# 讀取設定檔
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

# 從設定檔取得參數
$BuildSourceDir = Expand-ConfigVariable $config.BuildSourceDir
$serverList = $config.ServerList_cmdline

# 驗證必要參數
if ([string]::IsNullOrWhiteSpace($BuildSourceDir)) {
    Write-Host "✗ 設定檔中缺少 BuildSourceDir 參數" -ForegroundColor Red
    exit 1
}

if ($null -eq $serverList -or $serverList.Count -eq 0) {
    Write-Host "✗ 設定檔中缺少 ServerList_cmdline 或列表為空" -ForegroundColor Red
    exit 1
}

# 定義檢查結果
$results = @()
$allExists = $true

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "開始檢查並重新命名資料夾" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "建置來源目錄: $BuildSourceDir" -ForegroundColor Cyan
Write-Host "伺服器數量: $($serverList.Count)" -ForegroundColor Cyan
Write-Host ""

foreach ($server in $serverList) {
    $sourcePath = Join-Path $BuildSourceDir "OUTPUT\$server\fep-app\fep-batch-cmdline-simple"
    $targetPath = Join-Path $BuildSourceDir "OUTPUT\$server\fep-app\fep-batch-cmdline"
    
    Write-Host "檢查伺服器: $server" -ForegroundColor Yellow
    Write-Host "來源路徑: $sourcePath" -ForegroundColor Gray
    
    if (Test-Path -Path $sourcePath) {
        Write-Host "✓ 資料夾存在，準備重新命名..." -ForegroundColor Green
        
        try {
            # 先將 fep-batch-cmdline-simple.jar 改名為 fep-batch-cmdline.jar
            $simpleJar = Join-Path $sourcePath "fep-batch-cmdline-simple.jar"
            $renamedJar = Join-Path $sourcePath "fep-batch-cmdline.jar"
            
            if (Test-Path $simpleJar) {
                Write-Host "  重新命名 JAR 檔案: fep-batch-cmdline-simple.jar -> fep-batch-cmdline.jar" -ForegroundColor Cyan
                Rename-Item -Path $simpleJar -NewName "fep-batch-cmdline.jar" -Force
                Write-Host "  ✓ JAR 檔案重新命名成功" -ForegroundColor Green
            } else {
                Write-Host "  ! 找不到 fep-batch-cmdline-simple.jar，跳過 JAR 重新命名" -ForegroundColor Gray
            }
            
            # 如果目標路徑已存在，將來源資料夾內容移動到目標資料夾
            if (Test-Path -Path $targetPath) {
                Write-Host "  目標路徑已存在，將檔案移動合併..." -ForegroundColor Yellow
                
                # 取得來源資料夾中的所有項目
                $items = Get-ChildItem -Path $sourcePath -Recurse
                
                foreach ($item in $items) {
                    # 計算相對路徑
                    $relativePath = $item.FullName.Substring($sourcePath.Length)
                    $destinationPath = Join-Path -Path $targetPath -ChildPath $relativePath
                    
                    if ($item.PSIsContainer) {
                        # 如果是資料夾，確保目標資料夾存在
                        if (-not (Test-Path -Path $destinationPath)) {
                            New-Item -Path $destinationPath -ItemType Directory -Force | Out-Null
                        }
                    } else {
                        # 如果是檔案，移動到目標位置（覆蓋現有檔案）
                        $destDir = Split-Path -Path $destinationPath -Parent
                        if (-not (Test-Path -Path $destDir)) {
                            New-Item -Path $destDir -ItemType Directory -Force | Out-Null
                        }
                        Copy-Item -Path $item.FullName -Destination $destinationPath -Force
                    }
                }
                
                # 移動完成後刪除來源資料夾
                Remove-Item -Path $sourcePath -Recurse -Force
                Write-Host "  ✓ 成功將所有檔案移動至: fep-batch-cmdline" -ForegroundColor Green
                
                $results += [PSCustomObject]@{
                    Server = $server
                    SourcePath = $sourcePath
                    TargetPath = $targetPath
                    Status = "檔案移動合併成功"
                }
            } else {
                # 目標路徑不存在，直接重新命名
                Rename-Item -Path $sourcePath -NewName "fep-batch-cmdline" -Force
                Write-Host "  ✓ 成功重新命名為: fep-batch-cmdline" -ForegroundColor Green
                
                $results += [PSCustomObject]@{
                    Server = $server
                    SourcePath = $sourcePath
                    TargetPath = $targetPath
                    Status = "重新命名成功"
                }
            }
        } catch {
            Write-Host "  ✗ 重新命名失敗: $($_.Exception.Message)" -ForegroundColor Red
            $results += [PSCustomObject]@{
                Server = $server
                SourcePath = $sourcePath
                TargetPath = $targetPath
                Status = "重新命名失敗: $($_.Exception.Message)"
            }
            $allExists = $false
        }
    } else {
        Write-Host "✗ 資料夾不存在" -ForegroundColor Red
        $results += [PSCustomObject]@{
            Server = $server
            SourcePath = $sourcePath
            TargetPath = $targetPath
            Status = "來源資料夾不存在"
        }
    }
    Write-Host ""
}

# 判斷 Pipeline 執行結果
if ($allExists) {
    Write-Host "✓ 所有伺服器的資料夾都已成功處理" -ForegroundColor Green
    exit 0
} else {
    Write-Host "✗ 部分伺服器處理失敗，請檢查!" -ForegroundColor Red
    exit 1
}