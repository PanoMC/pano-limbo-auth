#!/usr/bin/env bash
#
# Populates libs/ with the three dependency sets that are normally served only by
# maven.elytrium.net, sourcing them from reachable public locations instead so the
# fork builds even when the Elytrium maven repository is down.
#
#   * LimboAPI          -> GitHub release asset (Elytrium/LimboAPI)
#   * ElytriumCommons   -> built from source (Elytrium/ElytriumJavaCommons, master = 1.2.5)
#   * velocity-proxy    -> the runnable Velocity server jar from PaperMC's download API
#                          (PaperMC does not publish velocity-proxy to any maven repo)
#
# build.gradle picks these up via `flatDir { dirs("libs") }`. maven.elytrium.net is kept
# as a fallback repository, so a normal build still works unchanged when it is reachable.
#
# Pinned versions must match gradle.properties (limboapiVersion / elytriumCommonsVersion)
# and build.gradle (velocity-proxy version).
set -euo pipefail

LIMBOAPI_VERSION="1.1.26"
COMMONS_VERSION="1.2.5-1"          # maven version; master source is 1.2.5
VELOCITY_VERSION="3.4.0-SNAPSHOT"
VELOCITY_BUILD="563"               # PaperMC build id for the pinned snapshot

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LIBS="$ROOT/libs"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
mkdir -p "$LIBS"

echo "==> LimboAPI $LIMBOAPI_VERSION (GitHub release)"
curl -fsSL -o "$LIBS/api-$LIMBOAPI_VERSION.jar" \
  "https://github.com/Elytrium/LimboAPI/releases/download/$LIMBOAPI_VERSION/limboapi-$LIMBOAPI_VERSION.jar"

echo "==> velocity-proxy $VELOCITY_VERSION (PaperMC build $VELOCITY_BUILD)"
VURL="$(curl -fsSL "https://fill.papermc.io/v3/projects/velocity/versions/$VELOCITY_VERSION/builds/$VELOCITY_BUILD" \
  | python3 -c 'import sys,json;print(json.load(sys.stdin)["downloads"]["server:default"]["url"])')"
curl -fsSL -o "$LIBS/velocity-proxy-$VELOCITY_VERSION.jar" "$VURL"

echo "==> ElytriumCommons (built from source)"
git clone --depth 1 https://github.com/Elytrium/ElytriumJavaCommons "$WORK/commons"
# The upstream build only declares mavenCentral + elytrium-repo; add PaperMC so
# velocity-api resolves without maven.elytrium.net.
sed -i 's#mavenCentral()#mavenCentral()\n        maven { setUrl("https://repo.papermc.io/repository/maven-public/") }#' "$WORK/commons/build.gradle"
( cd "$WORK/commons" && ./gradlew --no-daemon -x test -x checkstyleMain -x checkstyleTest \
    -x spotbugsMain -x spotbugsTest -x javadoc build )
for m in config kyori velocity utils; do
  jar="$(find "$WORK/commons/$m/build/libs" -name '*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' | head -1)"
  cp "$jar" "$LIBS/$m-$COMMONS_VERSION.jar"
done

echo "==> libs/ ready:"
ls -1 "$LIBS"
