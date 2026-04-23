#!/usr/bin/env sh
set -euo pipefail

# ── Always run relative to this script's location ──────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

# ── Colours ────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
info()    { echo -e "${CYAN}→ $*${NC}"; }
success() { echo -e "${GREEN}✓ $*${NC}"; }
warn()    { echo -e "${YELLOW}⚠  $*${NC}"; }
error()   { echo -e "${RED}✗  $*${NC}"; exit 1; }

info "Project root: $PROJECT_ROOT"

# ── Locate JAVA_HOME ───────────────────────────────────────
if [ -z "${JAVA_HOME:-}" ]; then
    JAVA_BIN=$(readlink -f "$(which java)" 2>/dev/null) || error "java not found in PATH. Install: sudo pacman -S jdk-openjdk"
    JAVA_HOME=$(dirname "$(dirname "$JAVA_BIN")")
fi

if [ ! -f "$JAVA_HOME/include/jni.h" ]; then
    JNI_FOUND=$(find "$JAVA_HOME" -name "jni.h" 2>/dev/null | head -1)
    [ -z "$JNI_FOUND" ] && error "jni.h not found. Install JDK: sudo pacman -S jdk-openjdk"
    JAVA_HOME=$(dirname "$(dirname "$JNI_FOUND")")
fi

success "JAVA_HOME = $JAVA_HOME"
success "jni.h     = $JAVA_HOME/include/jni.h"

# ── Output dirs (always under project root/native/build) ───
BUILD_LINUX="$PROJECT_ROOT/build/linux"
BUILD_WINDOWS="$PROJECT_ROOT/build/windows"
BUILD_MACOS="$PROJECT_ROOT/build/macos"
mkdir -p "$BUILD_LINUX" "$BUILD_WINDOWS" "$BUILD_MACOS"

# ── Source list ────────────────────────────────────────────
DOOM_SOURCES=$(find "$PROJECT_ROOT/doomgeneric/doomgeneric" -name '*.c' \
    ! -name 'doomgeneric_sdl.c'        \
    ! -name 'doomgeneric_win.c'        \
    ! -name 'doomgeneric_xlib.c'       \
    ! -name 'doomgeneric_allegro.c'    \
    ! -name 'doomgeneric_emscripten.c' \
    ! -name 'doomgeneric_linuxvt.c'    \
    ! -name 'doomgeneric_soso.c'       \
    ! -name 'doomgeneric_sosox.c'      \
    ! -name 'i_sdlsound.c'             \
    ! -name 'i_sdlmusic.c'             \
    ! -name 'i_allegromusic.c'         \
    ! -name 'i_allegrosound.c')
    # dummy.c intentionally included — stubs out net/drone symbols

GLUE="$PROJECT_ROOT/doomgeneric_java.c"

# ── Common flags ───────────────────────────────────────────
INCLUDES="-I$PROJECT_ROOT/doomgeneric/doomgeneric"
COMMON_FLAGS="-O2 -std=c99 -D_POSIX_C_SOURCE=200809L -DNOMIXER -Wno-unused-result -Wno-implicit-function-declaration"

# ── Linux ──────────────────────────────────────────────────
info "Compiling doom.so (Linux)..."
gcc -shared -fPIC $COMMON_FLAGS $INCLUDES \
    -I"$JAVA_HOME/include" \
    -I"$JAVA_HOME/include/linux" \
    $DOOM_SOURCES "$GLUE" \
    -lm \
    -o "$BUILD_LINUX/doom.so"
success "doom.so → $BUILD_LINUX/doom.so"

# ── Windows ────────────────────────────────────────────────
if command -v x86_64-w64-mingw32-gcc &>/dev/null; then
    info "Compiling doom.dll (Windows via mingw-w64)..."
    x86_64-w64-mingw32-gcc -shared $COMMON_FLAGS $INCLUDES \
        -I"$JAVA_HOME/include" \
        -I"$JAVA_HOME/include/linux" \
        -I"$JAVA_HOME/include/win32" \
        $DOOM_SOURCES "$GLUE" \
        -lm \
        -o "$BUILD_WINDOWS/doom.dll"
    success "doom.dll   → $BUILD_WINDOWS/doom.dll"
else
    warn "mingw-w64 not found — skipping Windows build."
    warn "Install: sudo pacman -S mingw-w64-gcc"
fi

# ── macOS ──────────────────────────────────────────────────
# The AUR osxcross-git package has a dead SDK link (404). Build manually:
#
#   git clone https://github.com/tpoechtrager/osxcross ~/osxcross
#   cd ~/osxcross
#   # Get an SDK from https://github.com/alexey-lysiuk/macos-sdk/releases
#   # e.g. MacOSX14.0.sdk.tar.xz
#   cp MacOSX14.0.sdk.tar.xz tarballs/
#   UNATTENDED=1 ./build.sh
#   export PATH="$PATH:$HOME/osxcross/target/bin"  # add to ~/.bashrc too
#
if command -v o64-clang &>/dev/null; then
    info "Compiling doom.dylib (macOS via osxcross)..."
    o64-clang -shared -fPIC $COMMON_FLAGS $INCLUDES \
        -I"$JAVA_HOME/include" \
        -I"$JAVA_HOME/include/linux" \
        -I"$JAVA_HOME/include/darwin" \
        $DOOM_SOURCES "$GLUE" \
        -lm \
        -o "$BUILD_MACOS/doom.dylib"
    success "doom.dylib → $BUILD_MACOS/doom.dylib"
else
    warn "osxcross not found — skipping macOS build."
    warn "See comment above this block for manual install instructions."
fi

# ── Summary ────────────────────────────────────────────────
echo ""
info "Output:"
ls -lh "$BUILD_LINUX/doom.so"    2>/dev/null || true
ls -lh "$BUILD_WINDOWS/doom.dll"    2>/dev/null || true
ls -lh "$BUILD_MACOS/doom.dylib" 2>/dev/null || true