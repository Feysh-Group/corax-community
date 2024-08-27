<# PowerShell equivalent of the bash script #>
# Install with cmd.exe
#   @"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -InputFormat None -ExecutionPolicy Bypass -File coraxjw.ps1
# Install with PowerShell.exe
#   Set-ExecutionPolicy Bypass -Scope Process -Force; .\coraxjw.ps1


##  CoraxJava - a Java Static Analysis Framework
##  Copyright (C) 2024.  Feysh-Tech Group
##
##  This library is free software; you can redistribute it and/or
##  modify it under the terms of the GNU Lesser General Public
##  License as published by the Free Software Foundation; either
##  version 2.1 of the License, or (at your option) any later version.
##
##  This library is distributed in the hope that it will be useful,
##  but WITHOUT ANY WARRANTY; without even the implied warranty of
##  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
##  Lesser General Public License for more details.
##
##  You should have received a copy of the GNU Lesser General Public
##  License along with this library; if not, write to the Free Software
##  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
#

$CORAX_VERSION = "2.14"
$CORAX_JAVA_ARTIFACT_NAME = "corax-java-cli-community-$CORAX_VERSION"
$CORAX_JAVA_ARTIFACT_ZIP = "$CORAX_JAVA_ARTIFACT_NAME.zip"
$CORAX_JAVA_CLI_NAME = "corax-cli-community-${CORAX_VERSION}.jar"
$JDK_TARGET_DIR = $env:USERPROFILE + "\AppData\Local\Programs\"
$CORAX_TARGET_DIR = $env:USERPROFILE + "\AppData\Local\Programs\corax-community"
$JVM_VERSION = "jdk-17.0.3.1"

$app_path = $MyInvocation.MyCommand.Path
$APP_NAME = "CoraxJava"
$APP_BASE_NAME = $MyInvocation.MyCommand.Name
$DEFAULT_JVM_OPTS = ""

$OSS_URL_CORAX_JAVA_CLI_COMMUNITY = "https://release.feysh.com/corax/corax-java-cli-community-$CORAX_VERSION.zip?OSSAccessKeyId=LTAI5tKF4FQ2CGnhMA7oU58p&Expires=1756262371&Signature=LgNw3Jl2JATo6UYQfovJ4z8Q94M%3D"
$OSS_URL_JDK_WIN_X64 = "http://release.feysh.com/corax-java-group/jdk-17.0.3.1_windows-x64_bin.zip?OSSAccessKeyId=LTAI5tKF4FQ2CGnhMA7oU58p&Expires=2023865879&Signature=mnWSUQlW8Mwme%2FCIuvaWD27bNjE%3D"

$uninstall=0
$BUILD_DIR = $env:TEMP + "\corax_temp"


function _is_china {
    $ipInfo = (Invoke-WebRequest -Uri "http://myip.ipip.net").Content
    $r=($ipInfo.Contains("`u{4E2D}`u{56FD}"))
    return $r
}


function _download_extract {
    param (
        [Parameter(Mandatory=$true)]
        [string]$name,

        [Parameter(Mandatory=$true)]
        [string]$url,

        [Parameter(Mandatory=$true)]
        [string]$dest,

        [Parameter(Mandatory=$true)]
        [string]$temp_file_name
    )

    $temp_file="$BUILD_DIR\$temp_file_name"
    $download_flag="$BUILD_DIR\$temp_file_name.flag"
    New-Item -ItemType Directory -Path "$BUILD_DIR" -ErrorAction SilentlyContinue

    if ((Test-Path -Path "$download_flag") -and -not (Test-Path -Path "$temp_file")) {
        Remove-Item "$download_flag" -ErrorAction SilentlyContinue
    }
    if (-not ((Test-Path -Path "$download_flag") -and (Test-Path -Path "$dest"))) {
        Remove-Item "$temp_file" -ErrorAction SilentlyContinue
        Write-Host "[Downloading] Downloading $name : $url to $temp_file"

        #            $client = New-Object Net.WebClient
        #            $client.DownloadFile($url, $temp_file)
        # 启用进度条显示
        $ProgressPreference = 'Continue'

        # 使用 Invoke-WebRequest 下载并显示进度
        try {
            # 若要更详细地控制进度报告，可以使用 Start-BitsTransfer（仅适用于Windows系统）
            if (Get-Command Start-BitsTransfer -ErrorAction SilentlyContinue) {
                Start-BitsTransfer -DisplayName $url -Source $url -Destination $temp_file -ErrorAction Stop
            } else {
                Invoke-WebRequest -Uri $url -OutFile $temp_file -Verbose
            }
        } catch
        {
            throw 'Failed to transfer with BITS. Here is the error message: ' + $error[0].exception.message
        }
        Set-Content -Path "$download_flag" -Value $url
    }
    if (-not (Test-Path -Path "$temp_file")) {
        Remove-Item "$download_flag" -ErrorAction SilentlyContinue
        throw "Failed to download. Retry?"
    }
    Write-Host "Extracting $temp_file to $dest"
    Remove-Item "$dest" -Recurse -ErrorAction SilentlyContinue
    New-Item -ItemType Directory -Path "$dest" -ErrorAction SilentlyContinue

    Expand-Archive "$temp_file" -DestinationPath "$dest"
}

function testValidDownUrl
{
    param(
        [Parameter(Mandatory = $true)]
        [string]$url
    )
    try
    {
        $response = Invoke-WebRequest -Uri "$url" -Method Head
        if ($response.StatusCode -eq 200)
        {
            return $true
        }
        else
        {
            Write-Host "Test url: Failed to download $url : response.status: " $response.StatusCode
            return $false
        }
    }
    catch
    {
        Write-Host "Test url: Warning: Failed to download $url : exception: " $error[0].exception.message
        return $fales
    }
}


function _detect_jdk() {
    $JVM_TARGET_DIR="$JDK_TARGET_DIR\jdk-$JVM_VERSION"

    $flag = "jdk.$JVM_VERSION.flag"
    if ($uninstall) {
        Write-Host "[uninstall] remove $JVM_TARGET_DIR/$flag"
        Write-Host "[uninstall] remove $JVM_TARGET_DIR"
        Remove-Item "$JVM_TARGET_DIR/$flag" -ErrorAction SilentlyContinue
        Remove-Item "$JVM_TARGET_DIR" -Recurse -ErrorAction SilentlyContinue
        return
    }
    if ((Test-Path "$JVM_TARGET_DIR\$flag") -and (Get-ChildItem "$JVM_TARGET_DIR")) {
        # Everything is up-to-date in $JVM_TARGET_DIR, do nothing
    }
    else {
        $ARCHIVE_NAME="jdk.zip"
        $JVM_URL = if ((_is_china) -and (testValidDownUrl -url "$OSS_URL_JDK_WIN_X64")) { $OSS_URL_JDK_WIN_X64 }
        else {"https://download.oracle.com/java/17/archive/${JVM_VERSION}_windows-x64_bin.zip" }

        _download_extract "JDK" $JVM_URL $JVM_TARGET_DIR $ARCHIVE_NAME
        Set-Content -Path "$JVM_TARGET_DIR\$flag" -Value $JVM_URL
    }

    try
    {
        $JAVA_HOME = ""
        foreach ($dir in (Get-ChildItem -Path $JVM_TARGET_DIR -Directory)) {
            if (Test-Path -Path "$($dir.FullName)\bin\java.exe") {
                $JAVA_HOME = $dir.FullName
                break
            }
        }

        if (-not (Test-Path -Path "$JAVA_HOME\bin\java.exe")) {
            Write-Host "Unable to find java.exe under $($JVM_TARGET_DIR)"
            throw "Failed to locate java.exe"
        }

        Write-Host "[Version] JDK: $JAVA_HOME (version: $JVM_VERSION)"

        $env:JAVA_HOME = $JAVA_HOME

        if ($JAVA_HOME) {
            if (Test-Path -Path "$JAVA_HOME\jre\sh\java.exe" -PathType Leaf) {
                # IBM's JDK on AIX uses strange locations for the executables
                $JAVACMD = "$JAVA_HOME\jre\sh\java.exe"
            } else {
                $JAVACMD = "$JAVA_HOME\bin\java.exe"
            }

            if (-not (Test-Path -Path "$JAVACMD" -PathType Leaf)) {
                throw "ERROR: JAVA_HOME is set to an invalid directory: $($JAVA_HOME)"
            }
        } else {
            throw "ERROR: JAVA_HOME is not set"
        }
        $global:JAVACMD=$JAVACMD
    }
    catch
    {
        Remove-Item "$JVM_TARGET_DIR/$flag" -ErrorAction SilentlyContinue
        Remove-Item "$JVM_TARGET_DIR" -Recurse -ErrorAction SilentlyContinue
        Write-Host "Retry?"
        throw
    }
}


function _detect_corax() {
    $CJ_TARGET_DIR = "$CORAX_TARGET_DIR\corax-$CORAX_VERSION" # CORAX_JAVA TARGET_DIR

    $flag = "corax.$JVM_VERSION.flag"
    if ($uninstall) {
        Write-Host "[uninstall] remove $CJ_TARGET_DIR/$flag"
        Write-Host "[uninstall] remove $CJ_TARGET_DIR"
        Remove-Item "$CJ_TARGET_DIR/$flag" -ErrorAction SilentlyContinue
        Remove-Item "$CJ_TARGET_DIR" -Recurse -ErrorAction SilentlyContinue
        return
    }
    if ((Test-Path "$CJ_TARGET_DIR\$flag") -and (Get-ChildItem "$CJ_TARGET_DIR")) {
        # Everything is up-to-date in $CJ_TARGET_DIR, do nothing
    }
    else {
        $ARCHIVE_NAME=$CORAX_JAVA_ARTIFACT_ZIP
        $CORAX_JAVA_RELEASE_URL = if ((_is_china) -and (testValidDownUrl -url "$OSS_URL_CORAX_JAVA_CLI_COMMUNITY"))
        {
            $OSS_URL_CORAX_JAVA_CLI_COMMUNITY
        } else
        {
            "https://github.com/Feysh-Group/corax-community/releases/download/v$CORAX_VERSION/$CORAX_JAVA_ARTIFACT_ZIP"
        }

        _download_extract "Corax Java" $CORAX_JAVA_RELEASE_URL $CJ_TARGET_DIR $ARCHIVE_NAME
        Set-Content -Path "$CJ_TARGET_DIR\$flag" -Value $CORAX_JAVA_RELEASE_URL
    }

    try {
        $CORAX_HOME = ""
        foreach ($dir in (Get-ChildItem -Path $CJ_TARGET_DIR -Directory)) {
            if (Test-Path -Path "$($dir.FullName)\$CORAX_JAVA_CLI_NAME") {
                $CORAX_HOME=$dir.FullName
                break
            }
        }
        Write-Host "[Version] Corax Java: $CORAX_HOME (version: $CORAX_VERSION)"

        $env:CORAX_HOME = $CORAX_HOME

        if ($CORAX_HOME) {
            $CORAX_JAR = "$CORAX_HOME\$CORAX_JAVA_CLI_NAME"
            if (-not (Test-Path -PathType Leaf -Path $CORAX_JAR)) {
                throw "ERROR: CORAX_HOME is set to an invalid directory: $($CORAX_HOME)"
            }
        } else {
            throw "ERROR: CORAX_HOME could not be found in $CJ_TARGET_DIR."
        }
        $global:CORAX_JAR=$CORAX_JAR
    } catch
    {
        Remove-Item "$CJ_TARGET_DIR/$flag" -ErrorAction SilentlyContinue
        Remove-Item "$CJ_TARGET_DIR" -Recurse -ErrorAction SilentlyContinue
        Write-Host "Retry?"
        throw
    }

}

function _delegate_corax_run() {
    # 收集java命令的所有参数
    $arguments = @(
        '-jar', "$global:CORAX_JAR",
        '--enable-data-flow', 'true'
    )
    $arguments+=$args
    if (-not ($args -contains '--config'))
    {
        $arguments += @(
          '--config', "default-config.yml@${env:CORAX_HOME}\analysis-config"
        )
    }

    $JAVA_OPTS=$env:JAVA_OPTS
    Write-Host "[cmd] " $global:JAVACMD $DEFAULT_JVM_OPTS $JAVA_OPTS $arguments
    if ([string]::IsNullOrEmpty($DEFAULT_JVM_OPTS)) {
        $_DEFAULT_JVM_OPTS=@()
    } else {
        $_DEFAULT_JVM_OPTS=iex "echo $DEFAULT_JVM_OPTS"
    }

    if ([string]::IsNullOrEmpty($JAVA_OPTS)) {
        $_JAVA_OPTS=@()
    } else {
        $_JAVA_OPTS=iex "echo $JAVA_OPTS"
    }

    # 然后执行 java 命令
    & "$global:JAVACMD" `
	  @_DEFAULT_JVM_OPTS `
	  @_JAVA_OPTS `
	  @arguments `
	  | Out-Default
}

function main() {
    (_detect_jdk)
    (_detect_corax)
    if ($uninstall) {
        Write-Host "[uninstall] remove $BUILD_DIR"
        Remove-Item "$BUILD_DIR" -Recurse -ErrorAction SilentlyContinue
        return
    }
    _delegate_corax_run @args
}

if ($args.Count -eq 1 -and $args[0] -eq "uninstall") {
    $uninstall = $true
}

(main @args)




