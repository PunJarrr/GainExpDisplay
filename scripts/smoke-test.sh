#!/bin/bash
# Boots a test server for each target, checks GainExpDisplay enables and answers /ged reload,
# joins with a test bot that picks up XP and checks the action bar text, stops the server and
# reports problems.
#
#   scripts/smoke-test.sh paper-1.21.11 spigot-1.17.1
#   scripts/smoke-test.sh all
#
# Targets are <platform>-<minecraft version>; `./gradlew -q printTestServers` lists them.
# Running it accepts the Minecraft EULA (https://aka.ms/MinecraftEULA) for the test servers in run/.
# Needs Java (Gradle downloads the rest), Python 3 and Node.js 18+.
set -u
ROOT=$(cd "$(dirname "$0")/.." && pwd)
OUT=$ROOT/build/smoke-test
mkdir -p "$OUT"
cd "$ROOT" || exit 2

TARGETS=("$@")
if [ "${1:-}" = all ]; then
  TARGETS=($(./gradlew -q printTestServers))
fi
[ ${#TARGETS[@]} -eq 0 ] && { echo "usage: $0 <platform-version>... | all"; exit 2; }

# The bot's dependencies (Mineflayer), pinned by tools/smoke-bot/package-lock.json
if [ ! -d tools/smoke-bot/node_modules ]; then
  (cd tools/smoke-bot && npm ci --no-audit --no-fund > "$OUT/npm.log" 2>&1) || { echo "npm ci failed, see $OUT/npm.log"; exit 2; }
fi

# Offline-mode UUID of SmokeBot, so it can be made an operator before it joins
BOT_UUID=$(python3 -I -c 'import hashlib, uuid; print(uuid.UUID(bytes=hashlib.md5(b"OfflinePlayer:SmokeBot").digest(), version=3))')

FAILED=0
for T in "${TARGETS[@]}"; do
  PLATFORM=${T%%-*}; VERSION=${T#*-}
  TASK="run$(echo "${PLATFORM:0:1}" | tr a-z A-Z)${PLATFORM:1}_${VERSION//./_}"
  DIR=$ROOT/run/$T
  LOG=$OUT/$T.log; : > "$LOG"
  if [ "$PLATFORM" = spigot ]; then EXPECT_MESSENGER="BungeeCord chat"; else EXPECT_MESSENGER="built-in Adventure"; fi

  # Fresh world and fresh plugin config each run, so earlier runs can't affect the result
  mkdir -p "$DIR"
  rm -rf "$DIR"/world "$DIR"/world_nether "$DIR"/world_the_end "$DIR/plugins/GainExpDisplay"
  echo "eula=true" > "$DIR/eula.txt"
  printf '%s\n' 'level-type=minecraft\:flat' 'online-mode=false' 'enforce-secure-profile=false' 'spawn-protection=0' \
    'server-port=25565' 'enable-rcon=true' 'rcon.port=25575' 'rcon.password=smoketest' 'max-tick-time=-1' \
    'generate-structures=false' 'spawn-monsters=false' > "$DIR/server.properties"
  printf '[{"uuid":"%s","name":"SmokeBot","level":4,"bypassesPlayerLimit":false}]\n' "$BOT_UUID" > "$DIR/ops.json"

  # Build the plugin (and Spigot via BuildTools, the first time) before starting the clock
  if ! ./gradlew shadowJar $( [ "$PLATFORM" = spigot ] && echo "buildSpigot_${VERSION//./_}" ) < /dev/null >> "$LOG" 2>&1; then
    echo "FAIL $T (build failed, see $LOG)"; FAILED=1; continue
  fi

  ( ./gradlew "$TASK" < /dev/null >> "$LOG" 2>&1; echo "GRADLE_EXIT=$?" >> "$LOG" ) &

  STATUS=timeout
  for _ in $(seq 1 900); do
    if grep -q 'RCON running' "$LOG"; then STATUS=started; break; fi
    if grep -q 'GRADLE_EXIT=' "$LOG"; then STATUS=exited; break; fi
    sleep 1
  done

  PROBLEMS=()
  if [ "$STATUS" = started ]; then
    grep -q 'Enabling GainExpDisplay' "$LOG" || PROBLEMS+=("plugin did not enable")
    grep -q "Sending messages with .*$EXPECT_MESSENGER" "$LOG" || PROBLEMS+=("expected messages via $EXPECT_MESSENGER")

    RELOAD=$(python3 -I scripts/rcon.py 25575 smoketest "ged reload" 2>&1)
    echo "$RELOAD" >> "$OUT/$T.rcon.log"
    echo "$RELOAD" | grep -q 'Configuration reloaded successfully' || PROBLEMS+=("/ged reload gave: $(echo "$RELOAD" | tail -1)")

    BOT=$(node tools/smoke-bot/bot.js 25565 2>&1); BOT_EXIT=$?
    echo "$BOT" > "$OUT/$T.bot.log"
    case $BOT_EXIT in
      0) BOT_RESULT="bot ok" ;;
      3) BOT_RESULT="bot skipped (Mineflayer doesn't support $VERSION)" ;;
      *) BOT_RESULT="bot failed"; PROBLEMS+=("$(echo "$BOT" | grep -E '^(FAIL|SKIP)' | head -1)") ;;
    esac

    python3 -I scripts/rcon.py 25575 smoketest stop > /dev/null 2>&1
  else
    PROBLEMS+=("server $STATUS")
  fi
  for _ in $(seq 1 180); do grep -q 'GRADLE_EXIT=' "$LOG" && break; sleep 1; done
  grep -q 'GRADLE_EXIT=' "$LOG" || { pkill -f "run/$T" 2>/dev/null; PROBLEMS+=("server did not stop"); }

  # Any error or exception in the server log fails the test
  ERRORS=$(sed 's/\x1b\[[0-9;]*m//g' "$LOG" | grep -E '(ERROR\]|/ERROR|SEVERE\]|Exception)' | grep -vE "$(cat scripts/smoke-test-ignore.txt 2>/dev/null | grep -v '^#' | paste -sd'|' - | sed 's/^$/^$/')" | head -5)
  [ -n "$ERRORS" ] && PROBLEMS+=("errors in the server log")

  if [ ${#PROBLEMS[@]} -eq 0 ]; then
    echo "PASS $T ($BOT_RESULT)"
  else
    echo "FAIL $T: $(IFS='; '; echo "${PROBLEMS[*]}") (see $LOG)"
    [ -n "$ERRORS" ] && echo "$ERRORS" | sed 's/^/    /'
    FAILED=1
  fi
done
exit $FAILED
