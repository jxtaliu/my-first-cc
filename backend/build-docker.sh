#!/bin/bash
# Build script using Docker with JDK 17
# Usage: ./build-docker.sh [mvn args]
# Example: ./build-docker.sh compile
#          ./build-docker.sh test
#          ./build-docker.sh spring-boot:run

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

DOCKER_JAVA17="eclipse-temurin:17-jdk-alpine"

echo "Building with JDK 17 (Docker)..."

# Create .m2 directory if it doesn't exist with proper permissions
M2_DIR="$HOME/.m2"
if [ ! -d "$M2_DIR" ]; then
    mkdir -p "$M2_DIR"
fi

docker run --rm \
    -v "$SCRIPT_DIR:/app" \
    -v "$M2_DIR:/root/.m2" \
    -w /app \
    "$DOCKER_JAVA17" \
    ./mvnw -Duser.home=/root "$@"
