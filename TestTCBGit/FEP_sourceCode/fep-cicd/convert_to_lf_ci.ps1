# CI/CD Pipeline 版本 - CRLF to LF Converter
# 移除所有互動式輸入，適合自動化執行

param(
    [Parameter(Mandatory=$false)]
    [string]$ConfigFile = "cicd-config.json"
)

# 設定錯誤處理
$ErrorActionPreference = "Continue"

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
    Write-Host ""
} catch {
    Write-Host "✗ 讀取設定檔失敗: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 從設定檔取得參數
$TargetPaths = Expand-ConfigVariable $config.TargetPaths
$FileExtensions = $config.FileExtensions
$MaxRetries = if ($config.MaxRetries) { $config.MaxRetries } else { 5 }
$RetryDelayMs = if ($config.RetryDelayMs) { $config.RetryDelayMs } else { 1000 }

# 驗證必要參數
if ($null -eq $TargetPaths -or $TargetPaths.Count -eq 0) {
    Write-Host "✗ 設定檔中缺少 TargetPaths 或列表為空" -ForegroundColor Red
    exit 1
}

if ($null -eq $FileExtensions -or $FileExtensions.Count -eq 0) {
    Write-Host "✗ 設定檔中缺少 FileExtensions 或列表為空" -ForegroundColor Red
    exit 1
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "CRLF to LF Converter (CI/CD Mode)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Configuration:"
Write-Host "  Target Paths: $($TargetPaths.Count) path(s)"
Write-Host "  File Extensions: $($FileExtensions -join ', ')"
Write-Host "  Max Retries: $MaxRetries"
Write-Host "  Retry Delay: ${RetryDelayMs}ms"
Write-Host ""

# 統計變數
$totalFilesFound = 0
$totalConverted = 0
$totalSkipped = 0
$totalFailed = 0
$allFailedFiles = @()

# 處理每個目標路徑
foreach ($TargetPath in $TargetPaths) {
    Write-Host "----------------------------------------" -ForegroundColor Cyan
    Write-Host "Processing Path: $TargetPath" -ForegroundColor Cyan
    Write-Host "----------------------------------------" -ForegroundColor Cyan
    Write-Host ""

    # 檢查路徑是否存在
    if (-not (Test-Path $TargetPath)) {
        Write-Host "WARNING: Path does not exist - $TargetPath" -ForegroundColor Yellow
        Write-Host "Skipping this path..." -ForegroundColor Yellow
        Write-Host ""
        continue
    }

    Write-Host "Path exists: OK" -ForegroundColor Green
    Write-Host ""

    # 處理每種副檔名
    foreach ($extension in $FileExtensions) {
        Write-Host "Searching for *$extension files (including subdirectories)..." -ForegroundColor Yellow
        $files = Get-ChildItem -Path $TargetPath -Filter "*$extension" -File -Recurse

        if ($files.Count -eq 0) {
            Write-Host "No *$extension files found in this path" -ForegroundColor Gray
            Write-Host ""
            continue
        }

        Write-Host "Found $($files.Count) *$extension file(s)" -ForegroundColor Green
        Write-Host ""

        $totalFilesFound += $files.Count

        # 處理每個檔案
        $successCount = 0
        $failCount = 0
        $skippedCount = 0
        $failedFiles = @()

        foreach ($file in $files) {
            $fileName = $file.Name
            $relativePath = $file.FullName.Replace($TargetPath, "").TrimStart('\')
            
            Write-Host "Processing: $relativePath ..." -NoNewline
            
            $success = $false
            
            for ($retry = 0; $retry -lt $MaxRetries; $retry++) {
                try {
                    # 讀取檔案為 byte array 來保留原始編碼
                    $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
                    
                    # 檢查並轉換 CRLF 到 LF (直接在 byte 層級操作)
                    $hasCRLF = $false
                    $newBytes = New-Object System.Collections.Generic.List[byte]
                    
                    for ($i = 0; $i -lt $bytes.Length; $i++) {
                        if ($i -lt ($bytes.Length - 1) -and $bytes[$i] -eq 0x0D -and $bytes[$i + 1] -eq 0x0A) {
                            # 找到 CRLF (0x0D 0x0A)，只保留 LF (0x0A)
                            $hasCRLF = $true
                            $newBytes.Add(0x0A)
                            $i++  # 跳過下一個 byte (LF)
                        } else {
                            $newBytes.Add($bytes[$i])
                        }
                    }
                    
                    if ($hasCRLF) {
                        # 寫回檔案
                        [System.IO.File]::WriteAllBytes($file.FullName, $newBytes.ToArray())
                        Write-Host " CONVERTED" -ForegroundColor Green
                        $successCount++
                        $totalConverted++
                    } else {
                        Write-Host " SKIPPED (already LF)" -ForegroundColor Yellow
                        $skippedCount++
                        $totalSkipped++
                    }
                    $success = $true
                    break
                } catch {
                    if ($retry -lt ($MaxRetries - 1)) {
                        # 等待後重試
                        Start-Sleep -Milliseconds $RetryDelayMs
                    } else {
                        Write-Host " FAILED (locked)" -ForegroundColor Red
                        $failedFiles += $relativePath
                        $failCount++
                        $totalFailed++
                        $allFailedFiles += "$TargetPath\$relativePath"
                    }
                }
            }
        }

        Write-Host ""
        Write-Host "Extension *$extension Summary:" -ForegroundColor Cyan
        Write-Host "  Converted        : $successCount" -ForegroundColor Green
        Write-Host "  Already LF format: $skippedCount" -ForegroundColor Yellow
        Write-Host "  Failed (locked)  : $failCount" -ForegroundColor $(if($failCount -gt 0){"Red"}else{"Green"})
        Write-Host ""
    }
}

# 最終統計
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Overall Conversion Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Total files found    : $totalFilesFound"
Write-Host "Converted            : $totalConverted" -ForegroundColor Green
Write-Host "Already LF format    : $totalSkipped" -ForegroundColor Yellow
Write-Host "Failed (locked)      : $totalFailed" -ForegroundColor $(if($totalFailed -gt 0){"Red"}else{"Green"})

if ($totalFailed -gt 0) {
    Write-Host ""
    Write-Host "Failed files:" -ForegroundColor Red
    foreach ($f in $allFailedFiles) {
        Write-Host "  - $f" -ForegroundColor Red
    }
    Write-Host ""
    Write-Host "ERROR: Some files could not be converted" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Cyan
    exit 1  # 返回錯誤代碼，讓 Pipeline 知道有問題
}

Write-Host ""
Write-Host "SUCCESS: All files processed successfully" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
exit 0  # 返回成功代碼