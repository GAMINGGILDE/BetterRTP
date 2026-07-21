#!/usr/bin/env bash
set -euo pipefail

server_name="${1:?server name is required}"
server_url="${2:?server URL is required}"
server_sha256="${3:?server SHA-256 is required}"
plugin_jar="${4:?plugin JAR is required}"
user_agent="GAMINGGILDE-BetterRTP-CI/4.0 (https://github.com/GAMINGGILDE/BetterRTP)"
server_dir="smoke-test/${server_name}"

mkdir -p "${server_dir}/plugins"
curl --fail --location --silent --show-error \
  --header "User-Agent: ${user_agent}" \
  --output "${server_dir}/server.jar" "${server_url}"
echo "${server_sha256}  ${server_dir}/server.jar" | sha256sum --check --strict
cp "${plugin_jar}" "${server_dir}/plugins/BetterRTP.jar"

printf 'eula=true\n' > "${server_dir}/eula.txt"
cat > "${server_dir}/server.properties" <<'PROPERTIES'
online-mode=false
server-port=0
query.port=0
enable-query=false
enable-rcon=false
max-players=1
view-distance=2
simulation-distance=2
spawn-protection=0
PROPERTIES

mkfifo "${server_dir}/console.pipe"
exec 3<> "${server_dir}/console.pipe"

pushd "${server_dir}" >/dev/null
java -Xms512M -Xmx1536M -jar server.jar --nogui \
  < console.pipe > server.log 2>&1 &
server_pid=$!
popd >/dev/null

cleanup() {
  if kill -0 "${server_pid}" 2>/dev/null; then
    printf 'stop\n' >&3 || true
    sleep 2
    kill "${server_pid}" 2>/dev/null || true
  fi
  exec 3>&- || true
}
trap cleanup EXIT

ready=false
for _ in $(seq 1 240); do
  if grep -q 'Done (' "${server_dir}/server.log"; then
    ready=true
    break
  fi
  if ! kill -0 "${server_pid}" 2>/dev/null; then
    echo "${server_name} terminated before startup completed" >&2
    cat "${server_dir}/server.log" >&2
    exit 1
  fi
  sleep 1
done

if [[ "${ready}" != true ]]; then
  echo "${server_name} did not finish startup within 240 seconds" >&2
  cat "${server_dir}/server.log" >&2
  exit 1
fi

grep -F '[BetterRTP] Enabling BetterRTP' "${server_dir}/server.log"
grep -F '[BetterRTP] Configuration validation completed without errors.' "${server_dir}/server.log"

if grep -Eqi \
  'Error occurred while enabling BetterRTP|Could not load.+BetterRTP|Exception.+BetterRTP|Thread failed' \
  "${server_dir}/server.log"; then
  echo "${server_name} reported a BetterRTP startup error" >&2
  cat "${server_dir}/server.log" >&2
  exit 1
fi

printf 'stop\n' >&3
wait "${server_pid}"
trap - EXIT
exec 3>&-

echo "${server_name} smoke test passed"
