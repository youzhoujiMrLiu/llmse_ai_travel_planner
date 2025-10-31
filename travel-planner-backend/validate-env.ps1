# .env 文件验证脚本 (PowerShell)
# 用于检查 .env 文件配置是否正确

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Travel Planner - .env 配置验证工具" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 检查 .env 文件是否存在
if (-Not (Test-Path ".env")) {
    Write-Host "❌ 错误: 未找到 .env 文件" -ForegroundColor Red
    Write-Host ""
    Write-Host "请执行以下命令创建 .env 文件:"
    Write-Host "  Copy-Item .env.example .env" -ForegroundColor Yellow
    Write-Host ""
    exit 1
}

Write-Host "✅ .env 文件存在" -ForegroundColor Green
Write-Host ""

# 定义必需的环境变量
$RequiredVars = @("SUPABASE_DB_PASSWORD", "SUPABASE_JWT_SECRET", "QWEN_API_KEY", "AMAP_API_KEY")
$OptionalVars = @("XFYUN_APP_ID", "XFYUN_API_KEY", "XFYUN_API_SECRET")

# 读取 .env 文件内容
$envContent = Get-Content ".env" -Encoding UTF8

# 解析 .env 文件为哈希表
$envVars = @{}
foreach ($line in $envContent) {
    # 跳过注释和空行
    if ($line -match '^\s*#' -or $line -match '^\s*$') {
        continue
    }
    
    # 解析 KEY=VALUE
    if ($line -match '^([^=]+)=(.*)$') {
        $key = $matches[1].Trim()
        $value = $matches[2].Trim().Trim('"').Trim("'")
        $envVars[$key] = $value
    }
}

# 检查必需的环境变量
Write-Host "检查必需的环境变量..." -ForegroundColor Cyan
Write-Host "----------------------------"

$MissingCount = 0
$ExampleValueCount = 0

foreach ($var in $RequiredVars) {
    $value = $envVars[$var]
    
    if ([string]::IsNullOrWhiteSpace($value)) {
        Write-Host "❌ ${var}: 未设置" -ForegroundColor Red
        $MissingCount++
    } elseif ($value -match "your_" -or $value -match "_here") {
        Write-Host "⚠️  ${var}: 使用了示例值" -ForegroundColor Yellow
        $ExampleValueCount++
    } else {
        # 隐藏敏感信息,只显示前 8 个字符
        $maskedValue = $value.Substring(0, [Math]::Min(8, $value.Length)) + "..."
        Write-Host "✅ ${var}: ${maskedValue}" -ForegroundColor Green
    }
}

Write-Host ""

# 检查可选的环境变量
Write-Host "检查可选的环境变量..." -ForegroundColor Cyan
Write-Host "----------------------------"

foreach ($var in $OptionalVars) {
    $value = $envVars[$var]
    
    if ([string]::IsNullOrWhiteSpace($value)) {
        Write-Host "⭕ ${var}: 未设置 (可选)" -ForegroundColor Yellow
    } elseif ($value -match "your_" -or $value -match "_here") {
        Write-Host "⭕ ${var}: 使用了示例值 (可选)" -ForegroundColor Yellow
    } else {
        $maskedValue = $value.Substring(0, [Math]::Min(8, $value.Length)) + "..."
        Write-Host "✅ ${var}: ${maskedValue}" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan

# 总结
if ($MissingCount -gt 0 -or $ExampleValueCount -gt 0) {
    Write-Host "❌ 配置不完整!" -ForegroundColor Red
    Write-Host ""
    if ($MissingCount -gt 0) {
        Write-Host "有 $MissingCount 个必需的环境变量未设置。"
    }
    if ($ExampleValueCount -gt 0) {
        Write-Host "有 $ExampleValueCount 个必需的环境变量使用了示例值。"
    }
    Write-Host ""
    Write-Host "请编辑 .env 文件,填入真实的 API Keys:"
    Write-Host "  notepad .env" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "如何获取 API Keys:"
    Write-Host "  参考: ..\ENVIRONMENT_SETUP.md"
    Write-Host ""
    exit 1
} else {
    Write-Host "✅ 所有必需的环境变量都已正确配置!" -ForegroundColor Green
    Write-Host ""
    Write-Host "可以启动应用了:"
    Write-Host "  .\mvnw.cmd spring-boot:run" -ForegroundColor Yellow
    Write-Host ""
}

# 检查文件编码
try {
    $fileBytes = [System.IO.File]::ReadAllBytes(".env")
    
    # 检查 BOM
    if ($fileBytes.Length -ge 3 -and $fileBytes[0] -eq 0xEF -and $fileBytes[1] -eq 0xBB -and $fileBytes[2] -eq 0xBF) {
        Write-Host "✅ 文件编码: UTF-8 with BOM" -ForegroundColor Green
    } elseif ($fileBytes.Length -ge 2 -and $fileBytes[0] -eq 0xFF -and $fileBytes[1] -eq 0xFE) {
        Write-Host "⚠️  警告: 文件编码是 UTF-16 LE, 建议转换为 UTF-8" -ForegroundColor Yellow
        Write-Host "   执行: Get-Content .env | Out-File -Encoding UTF8 .env" -ForegroundColor Yellow
    } else {
        Write-Host "✅ 文件编码: UTF-8 (无 BOM) 或 ASCII" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️  无法检测文件编码" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
