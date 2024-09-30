#!/usr/bin/env bash

# 注：本脚本安全透明, 只联网下载(仅初次使用)不上传, 不会破环环境(会申请安装curl,才会申请一次root权限), 本软件支持一键卸载
# Note: This script is safe and transparent, only downloads (if need) and does not upload, will not break the environment, and supports one-click uninstallation

#
#  CoraxJava - a Java Static Analysis Framework
#  Copyright (C) 2024.  Feysh-Tech Group
#
#  This library is free software; you can redistribute it and/or
#  modify it under the terms of the GNU Lesser General Public
#  License as published by the Free Software Foundation; either
#  version 2.1 of the License, or (at your option) any later version.
#
#  This library is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#  Lesser General Public License for more details.
#
#  You should have received a copy of the GNU Lesser General Public
#  License along with this library; if not, write to the Free Software
#  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
#

################################################################################
#
# Description: coraxjw.sh is a Corax Wrapper program.
# Author: notify-bibi
#
################################################################################

CORAX_VERSION=2.16.2
CORAX_JAVA_ARTIFACT_NAME="corax-java-cli-community-$CORAX_VERSION"
CORAX_JAVA_ARTIFACT_ZIP="$CORAX_JAVA_ARTIFACT_NAME.zip"
CORAX_JAVA_CLI_NAME="corax-cli-community-${CORAX_VERSION}.jar"
JDK_TARGET_DIR="${HOME}/.local/share"
CORAX_TARGET_DIR="${HOME}/.local/corax"
JVM_VERSION=jdk-17.0.3.1

OSS_URL_CORAX_JAVA_CLI_COMMUNITY="https://release.feysh.com/corax/corax-java-cli-community-$CORAX_VERSION.zip?OSSAccessKeyId=LTAI5tKF4FQ2CGnhMA7oU58p&Expires=1758781045&Signature=E7FO2SYWcI5cOMdR%2FBCFSHsTB50%3D"
OSS_URL_JDK_DARWIN_X64="http://release.feysh.com/corax-java-group/jdk-17.0.3.1_macos-x64_bin.tar.gz?OSSAccessKeyId=LTAI5tKF4FQ2CGnhMA7oU58p&Expires=2023865870&Signature=pSaX7XqiEO%2BuZ5NsF0AsnkvKTB0%3D"
OSS_URL_JDK_DARWIN_AARCH64="http://release.feysh.com/corax-java-group/jdk-17.0.3.1_macos-aarch64_bin.tar.gz?OSSAccessKeyId=LTAI5tKF4FQ2CGnhMA7oU58p&Expires=2023865859&Signature=M41W3NYVzNfVR8wLpmOoiGTYxWo%3D"
OSS_URL_JDK_WIN_X64="http://release.feysh.com/corax-java-group/jdk-17.0.3.1_windows-x64_bin.zip?OSSAccessKeyId=LTAI5tKF4FQ2CGnhMA7oU58p&Expires=2023865879&Signature=mnWSUQlW8Mwme%2FCIuvaWD27bNjE%3D"
OSS_URL_JDK_LINUX_X64="http://release.feysh.com/corax-java-group/jdk-17.0.3.1_linux-x64_bin.tar.gz?OSSAccessKeyId=LTAI5tKF4FQ2CGnhMA7oU58p&Expires=2023865849&Signature=VKfQnzV5P7qKygBu9WODOzNPmDw%3D"
OSS_URL_JDK_LINUX_AARCH64="http://release.feysh.com/corax-java-group/jdk-17.0.3.1_linux-aarch64_bin.tar.gz?OSSAccessKeyId=LTAI5tKF4FQ2CGnhMA7oU58p&Expires=2023865822&Signature=E45ZFy%2BYpDRo7dm9%2Ftjg%2Bj1trMU%3D"


uninstall=false
# Attempt to set APP_HOME

# Resolve links: $0 may be a link
app_path=$0

# Need this for daisy-chained symlinks.
while
    APP_HOME=${app_path%"${app_path##*/}"}  # leaves a trailing /; empty if no leading path
    [ -h "$app_path" ]
do
    ls=$( ls -ld "$app_path" )
    link=${ls#*' -> '}
    case $link in             #(
      /*)   app_path=$link ;; #(
      *)    app_path=$APP_HOME$link ;;
    esac
done

APP_HOME=$( cd "${APP_HOME:-./}" && pwd -P ) || exit
APP_NAME="CoraxJava"
APP_BASE_NAME=${0##*/}

DEFAULT_JVM_OPTS=''
BUILD_DIR="/tmp/corax_temp"

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD=maximum

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

_msg() {
    local color_on
    local color_off='\033[0m' # Text Reset
    duration=$SECONDS
    h_m_s="$((duration / 3600))h$(((duration / 60) % 60))m$((duration % 60))s"
    time_now="$(date +%Y%m%d-%u-%T.%3N)"

    case "${1:-none}" in
    red | error | erro) color_on='\033[0;31m' ;;       # Red
    green | info) color_on='\033[0;32m' ;;             # Green
    yellow | warning | warn) color_on='\033[0;33m' ;;  # Yellow
    blue) color_on='\033[0;34m' ;;                     # Blue
    purple | question | ques) color_on='\033[0;35m' ;; # Purple
    cyan) color_on='\033[0;36m' ;;                     # Cyan
    orange) color_on='\033[1;33m' ;;                   # Orange
    step)
        ((++STEP))
        color_on="\033[0;36m[${STEP}] $time_now \033[0m"
        color_off=" $h_m_s"
        ;;
    time)
        color_on="[${STEP}] $time_now "
        color_off=" $h_m_s"
        ;;
    log)
        shift
        echo "$time_now $*" >>$me_log
        return
        ;;
    *)
        unset color_on color_off
        ;;
    esac
    [ "$#" -gt 1 ] && shift
    echo -e "${color_on}$*${color_off}"
}

warn () {
    _msg warn "$*"
} >&2

_is_root() {
    if [ "$(id -u)" -eq 0 ]; then
        unset use_sudo
        return 0
    else
        use_sudo=sudo
        return 1
    fi
}

_is_china() {
    # 判断当前机器的 ip 地址是否是国内, 备用选项: cip.cc
    local is_china_ip=$(curl -sSL http://myip.ipip.net | grep '中国' | wc -l | tr -d "[:space:]")
    if [[ $is_china_ip -ge 1 ]]; then
        return 0
    else
        return 1
    fi
}

_detect_os() {
    _is_root || use_sudo=sudo
    if [[ -e /etc/os-release ]]; then
        source /etc/os-release
        os_type="${ID}"
    elif [[ -e /etc/centos-release ]]; then
        os_type=centos
    elif [[ -e /etc/arch-release ]]; then
        os_type=arch
    elif [[ $OSTYPE == darwin* ]]; then
        os_type=macos
    fi
    pkgs=()
    case "$os_type" in
    debian | ubuntu | linuxmint)
        # RUN apt-get update && \
        #        apt-get -y install sudo dialog apt-utils
        # RUN echo 'debconf debconf/frontend select Noninteractive' | debconf-set-selections
        command -v curl >/dev/null || pkgs+=(curl)
        # command -v shc >/dev/null || $use_sudo apt-get install -qq -y shc

        if [[ "${#pkgs[*]}" -ne 0 ]]; then
            $use_sudo apt-get update -qq
            $use_sudo apt-get install -yqq apt-utils >/dev/null
            $use_sudo apt-get install -yqq "${pkgs[@]}" >/dev/null
        fi
        ;;
    centos | amzn | rhel | fedora)
        rpm -q epel-release >/dev/null || {
            if [ "$os_type" = amzn ]; then
                $use_sudo amazon-linux-extras install -y epel >/dev/null
            else
                $use_sudo yum install -y epel-release >/dev/null
                # DNF="dnf --setopt=tsflags=nodocs -y"
                # $DNF install epel-release
            fi
        }
        command -v curl >/dev/null || pkgs+=(curl)
        if [[ "${#pkgs[*]}" -ne 0 ]]; then
            $use_sudo yum install -y "${pkgs[@]}" >/dev/null
        fi
        ;;
    alpine)
        command -v curl >/dev/null || pkgs+=(curl)
        if [[ "${#pkgs[*]}" -ne 0 ]]; then
            $use_sudo apk add --no-cache "${pkgs[@]}" >/dev/null
        fi
        ;;
    macos)
        command -v curl >/dev/null || pkgs+=(curl)
        if (("${#pkgs[*]}")); then
            brew install "${pkgs[@]}"
        fi
        ;;
    arch)
        command -v curl >/dev/null || pkgs+=(curl)
        if (("${#pkgs[*]}")); then
            $use_sudo pacman -S "${pkgs[@]}"
        fi
        ;;
    *)
        echo "Looks like you aren't running this installer on a Debian, Ubuntu, Fedora, CentOS, Amazon Linux 2 or Arch Linux system"
        _msg error "Unsupported. exit."
        exit 1
        ;;
    esac


    # OS specific support (must be 'true' or 'false').
    cygwin=false
    msys=false
    darwin=false
    nonstop=false
    case "$( uname )" in                #(
      CYGWIN* )         cygwin=true  ;; #(
      Darwin* )         darwin=true  ;; #(
      MSYS* | MINGW* )  msys=true    ;; #(
      NONSTOP* )        nonstop=true ;;
    esac
}

_download_extract() {
    local name=$1
    local url=$2
    local dest=$3
    local temp_file="$BUILD_DIR/$4"
    local download_flag="$BUILD_DIR/$4.flag"

    set -e
    mkdir -p "$BUILD_DIR"
    if [ -e "$download_flag" ] && [ ! -e "$temp_file" ] ; then
        rm -f "$download_flag"
    fi
    if [ ! -e "$download_flag" ] || [ -z "$(ls "$dest")" ] ; then
        rm -f "$temp_file"
        _msg step "[Downloading] Downloading $name : $url to $temp_file"
        if command -v curl >/dev/null 2>&1; then
            if [ -t 1 ]; then CURL_PROGRESS="--progress-bar"; else CURL_PROGRESS="--show-error"; fi
            # shellcheck disable=SC2086
            curl $CURL_PROGRESS -L --fail --output "${temp_file}" "$url" 2>&1
        elif command -v wget >/dev/null 2>&1; then
            if [ -t 1 ]; then WGET_PROGRESS=""; else WGET_PROGRESS="-nv"; fi
            wget $WGET_PROGRESS -O "${temp_file}" "$url" 2>&1
        else
            die "ERROR: Please install wget or curl"
        fi
        echo "$url" > "$download_flag"
    fi
    if [ ! -e "$temp_file" ] ; then
        rm -f "$download_flag"
        die "Failed to download. Retry?"
    fi

    echo "Extracting $temp_file to $dest"
    rm -rf "$dest"
    mkdir -p "$dest"

    case "$temp_file" in
      *".zip") unzip "$temp_file" -d "$dest" ;;
      *) tar -x -f "$temp_file" -C "$dest" ;;
    esac

}

test_valid_down_url() {
    # 参数检查
    if [ -z "$1" ]; then
        echo "Usage: test_valid_down_url <url>"
        return 1
    fi

    local url="$1"
    local status_code

    # 使用curl命令发送HEAD请求并获取状态码
    status_code=$(curl -s -I -L -w '%{http_code}\n' "$url" -o /dev/null)

    # 检查HTTP状态码是否为200
    if [ "$status_code" -eq 200 ]; then
        return 0
    else
        echo "Test url: Failed to download $url : response.status: $status_code"
        return 1
    fi
}

is_cn=$(_is_china && echo true || echo false)

_detect_jdk() {
    local JVM_TARGET_DIR="$JDK_TARGET_DIR/jdk-$JVM_VERSION"
    local flag="jdk.$JVM_VERSION.flag"
    if [ "$uninstall" = "true" ]; then
        _msg info "[uninstall] remove $JVM_TARGET_DIR/$flag"
        _msg info "[uninstall] remove $JVM_TARGET_DIR"
        rm -f "$JVM_TARGET_DIR/$flag"
        rm -rf "$JVM_TARGET_DIR"
        return
    fi
    if [ -e "$JVM_TARGET_DIR/$flag" ] && [ -n "$(ls "$JVM_TARGET_DIR")" ] ; then
        # Everything is up-to-date in $JVM_TARGET_DIR, do nothing
        true
    else
      local JVM_ARCH=$(uname -m)
      local ARCHIVE_NAME="jdk.tar.gz"
      if [ "$darwin" = "true" ]; then
          case $JVM_ARCH in
          x86_64)
              if _is_china && test_valid_down_url "$OSS_URL_JDK_DARWIN_X64" == "true"; then
                JVM_URL="$OSS_URL_JDK_DARWIN_X64"
              else
                JVM_URL="https://download.oracle.com/java/17/archive/${JVM_VERSION}_macos-x64_bin.tar.gz"
              fi
              ;;
          arm64)
              if _is_china && test_valid_down_url "$OSS_URL_JDK_DARWIN_AARCH64" == "true"; then
                JVM_URL="$OSS_URL_JDK_DARWIN_AARCH64"
              else
                JVM_URL="https://download.oracle.com/java/17/archive/${JVM_VERSION}_macos-aarch64_bin.tar.gz"
              fi
              ;;
          *)
              die "Unknown architecture $JVM_ARCH"
              ;;
          esac
      elif [ "$cygwin" = "true" ] || [ "$msys" = "true" ]; then
          if _is_china && test_valid_down_url "$OSS_URL_JDK_WIN_X64" == "true"; then
            JVM_URL="$OSS_URL_JDK_WIN_X64"
          else
            JVM_URL="https://download.oracle.com/java/17/archive/${JVM_VERSION}_windows-x64_bin.zip"
          fi
          ARCHIVE_NAME="jdk.zip"
      else
          JVM_ARCH=$(linux$(getconf LONG_BIT) uname -m)
           case $JVM_ARCH in
              x86_64)
                  if _is_china && test_valid_down_url "$OSS_URL_JDK_LINUX_X64" == "true"; then
                    JVM_URL="$OSS_URL_JDK_LINUX_X64"
                  else
                    JVM_URL="https://download.oracle.com/java/17/archive/${JVM_VERSION}_linux-x64_bin.tar.gz"
                  fi
                  ;;
              aarch64)
                  if _is_china && test_valid_down_url "$OSS_URL_JDK_LINUX_AARCH64" == "true"; then
                    JVM_URL=$OSS_URL_JDK_LINUX_AARCH64
                  else
                    JVM_URL="https://download.oracle.com/java/17/archive/${JVM_VERSION}_linux-aarch64_bin.tar.gz"
                  fi
                  ;;
              *)
                  die "Unknown architecture $JVM_ARCH"
                  ;;
              esac
      fi

      _download_extract "JDK" "$JVM_URL" "$JVM_TARGET_DIR" "$ARCHIVE_NAME"
      echo "$JVM_URL" > "$JVM_TARGET_DIR/$flag"
    fi

    JAVA_HOME=
    for d in "$JVM_TARGET_DIR" "$JVM_TARGET_DIR"/* "$JVM_TARGET_DIR"/Contents/Home "$JVM_TARGET_DIR"/*/Contents/Home; do
      if [ -e "$d/bin/java" ]; then
        JAVA_HOME="$d"
      fi
    done

    if [ '!' -e "$JAVA_HOME/bin/java" ]; then
      rm -f "$JVM_TARGET_DIR/$flag"
      rm -rf "$JVM_TARGET_DIR"
      _msg info "Retry?"
      die "Unable to find bin/java under $JVM_TARGET_DIR"
    fi
    _msg info "[Version] JDK: $JAVA_HOME (version: $JVM_VERSION)"

    # Make it available for child processes
    export JAVA_HOME

    set +e

    # Determine the Java command to use to start the JVM.
    if [ -n "$JAVA_HOME" ] ; then
        if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
            # IBM's JDK on AIX uses strange locations for the executables
            JAVACMD=$JAVA_HOME/jre/sh/java
        else
            JAVACMD=$JAVA_HOME/bin/java
        fi
        if [ ! -x "$JAVACMD" ] ; then
            rm -f "$JVM_TARGET_DIR/$flag"
            rm -rf "$JVM_TARGET_DIR"
            _msg info "Retry?"
            die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
        fi
    else
        rm -f "$JVM_TARGET_DIR/$flag"
        rm -rf "$JVM_TARGET_DIR"
        _msg info "Retry?"
        die "ERROR: JAVA_HOME is not set"
    fi

    # Increase the maximum file descriptors if we can.
    if ! "$cygwin" && ! "$darwin" && ! "$nonstop" ; then
        case $MAX_FD in #(
          max*)
            MAX_FD=$( ulimit -H -n ) ||
                warn "Could not query maximum file descriptor limit"
        esac
        case $MAX_FD in  #(
          '' | soft) :;; #(
          *)
            ulimit -n "$MAX_FD" ||
                warn "Could not set maximum file descriptor limit to $MAX_FD"
        esac
    fi

    # For Cygwin or MSYS, switch paths to Windows format before running java
    if "$cygwin" || "$msys" ; then
        APP_HOME=$( cygpath --path --mixed "$APP_HOME" )
        CLASSPATH=$( cygpath --path --mixed "$CLASSPATH" )

        JAVACMD=$( cygpath --unix "$JAVACMD" )

        # Now convert the arguments - kludge to limit ourselves to /bin/sh
        for arg do
            if
                case $arg in                                #(
                  -*)   false ;;                            # don't mess with options #(
                  /?*)  t=${arg#/} t=/${t%%/*}              # looks like a POSIX filepath
                        [ -e "$t" ] ;;                      #(
                  *)    false ;;
                esac
            then
                arg=$( cygpath --path --ignore --mixed "$arg" )
            fi
            # Roll the args list around exactly as many times as the number of
            # args, so each arg winds up back in the position where it started, but
            # possibly modified.
            #
            # NB: a `for` loop captures its iteration list before it begins, so
            # changing the positional parameters here affects neither the number of
            # iterations, nor the values presented in `arg`.
            shift                   # remove old arg
            set -- "$@" "$arg"      # push replacement arg
        done
    fi
}


_detect_corax() {
    local CJ_TARGET_DIR="$CORAX_TARGET_DIR/corax-$CORAX_VERSION" # CORAX_JAVA TARGET_DIR

    local flag="corax.$CORAX_VERSION.flag"

    if [ "$uninstall" = "true" ]; then
        _msg info "[uninstall] remove $CJ_TARGET_DIR/$flag"
        _msg info "[uninstall] remove $CJ_TARGET_DIR"
        rm -f "$CJ_TARGET_DIR/$flag"
        rm -rf "$CJ_TARGET_DIR"
        return
    fi
    if [ -e "$CJ_TARGET_DIR/$flag" ] && [ -n "$(ls "$CJ_TARGET_DIR")" ]; then
        # Everything is up-to-date in $CJ_TARGET_DIR, do nothing
        true
    else
      local ARCHIVE_NAME=$CORAX_JAVA_ARTIFACT_ZIP
      if _is_china && test_valid_down_url "$OSS_URL_CORAX_JAVA_CLI_COMMUNITY" ; then
          CORAX_JAVA_RELEASE_URL="$OSS_URL_CORAX_JAVA_CLI_COMMUNITY"
      else
          CORAX_JAVA_RELEASE_URL="https://github.com/Feysh-Group/corax-community/releases/download/v$CORAX_VERSION/$CORAX_JAVA_ARTIFACT_ZIP"
      fi

      _download_extract "Corax Java" "$CORAX_JAVA_RELEASE_URL" "$CJ_TARGET_DIR" "$ARCHIVE_NAME"
      echo "$CORAX_JAVA_RELEASE_URL" > "$CJ_TARGET_DIR/$flag"
    fi
    CORAX_HOME=
    CORAX_JAR=
    for d in "$CJ_TARGET_DIR" "$CJ_TARGET_DIR"/*; do
      if [ -e "$d/$CORAX_JAVA_CLI_NAME" ]; then
        CORAX_HOME="$d"
      fi
    done

    _msg info "[Version] Corax Java: $CORAX_HOME (version: $CORAX_VERSION)"

    # Make it available for child processes
    export CORAX_HOME

    set +e

    # Determine the Java command to use to start the JVM.
    if [ -n "$CORAX_HOME" ] ; then
        CORAX_JAR="$CORAX_HOME/$CORAX_JAVA_CLI_NAME"
        if [ ! -f "$CORAX_JAR" ] ; then
            rm -f "$CJ_TARGET_DIR/$flag"
            rm -rf "$CJ_TARGET_DIR"
            _msg info "Retry?"
            die "ERROR: CORAX_HOME is set to an invalid directory: $CORAX_HOME"
        fi
    else
        rm -f "$CJ_TARGET_DIR/$flag"
        rm -rf "$CJ_TARGET_DIR"
        _msg info "Retry?"
        die "ERROR: CORAX_HOME could not found in $CJ_TARGET_DIR."
    fi
}

_delegate_corax_run() {
    set -- \
            -jar "$CORAX_JAR" \
            --enable-data-flow true \
            "$@"

    local config_param_found=false
    for arg in "$@"; do
      if [[ $arg == "--config" ]]; then
        config_param_found=true
        break
      fi
    done
    if [[ $config_param_found == "false" ]]; then
        # 如果没有包含，则添加到参数列表的末尾
            set -- \
                "$@" \
                --config default-config.yml@"$CORAX_HOME"/analysis-config
    fi
    # Stop when "xargs" is not available.
    if ! command -v xargs >/dev/null 2>&1
    then
        die "xargs is not available"
    fi

    eval "set -- $(
            printf '%s\n' "$DEFAULT_JVM_OPTS $JAVA_OPTS" |
            xargs -n1 |
            sed ' s~[^-[:alnum:]+,./:=@_]~\\&~g; ' |
            tr '\n' ' '
        )" '"$@"'

    _msg info "[cmd] $JAVACMD $@"
    exec "$JAVACMD" "$@"
}

main() {
    set -e ## 出现错误自动退出
    # set -u ## 变量未定义报错
    SECONDS=0

    ## check OS version/type/install command/install software / 检查系统版本/类型/安装命令/安装软件
    _detect_os
    _detect_jdk
    _detect_corax
    if [ "$uninstall" = "true" ]; then
        _msg info "[uninstall] remove $BUILD_DIR"
        rm -rf "$BUILD_DIR"
        return
    fi
    _delegate_corax_run "$@"
}



[[ $# -eq 1 ]] && [ "$1" == "uninstall" ] && uninstall=true


main "$@"