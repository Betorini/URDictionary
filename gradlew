#!/bin/sh
# Gradle wrapper - delegates to system gradle or downloads
set -e

GRADLE_VERSION="8.7"
GRADLE_HOME="$HOME/.gradle/wrapper/dists/gradle-${GRADLE_VERSION}-bin"

if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
fi

echo "Gradle not found. Please install Gradle $GRADLE_VERSION or run via Android Studio."
exit 1
