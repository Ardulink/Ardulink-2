#!/usr/bin/env bash
#
# Cut an Ardulink release: bump the version, build + GPG-sign, upload to the
# Maven Central Publisher Portal, then commit, tag and push.
#
# See README.release.md for prerequisites and for the manual equivalent.
#
#   ./release.sh 2.2.1
#   ./release.sh 2.2.1 --no-push     # build + upload only, no git push
#   ./release.sh 2.2.1 -s ~/.m2/central.xml   # Central token in another file
#
# The signing keyring is taken from $GNUPGHOME, else ~/.gnupg.
#
set -euo pipefail

cd "$(dirname "$0")"

RELEASE_PROFILE="release"
GROUP_PATH="org/ardulink"
PROBE_ARTIFACT="ardulink-core-base"

PUSH=true
VERSION=""
SETTINGS=""

die() { printf '\033[31merror\033[0m %s\n' "$*" >&2; exit 1; }
info() { printf '\033[32m==>\033[0m %s\n' "$*"; }
step() { printf '\n\033[1m%s\033[0m\n' "$*"; }
strip_ansi() { sed -E $'s/\x1b\\[[0-9;]*m//g'; }

usage() {
	awk 'NR>2 { if ($0 !~ /^#/) exit; sub(/^# ?/, ""); print }' "$0"
	exit "${1:-1}"
}

while [ $# -gt 0 ]; do
	case "$1" in
		--no-push) PUSH=false ;;
		--settings=*) SETTINGS="${1#*=}" ;;
		--settings | -s)
			shift
			[ $# -gt 0 ] || die "missing file for the settings option"
			SETTINGS="$1"
			;;
		-s?*) SETTINGS="${1#-s}" ;;
		-h | --help) usage 0 ;;
		-*) die "unknown option '$1' (try --help)" ;;
		*) [ -z "$VERSION" ] || die "unexpected argument '$1'"; VERSION="$1" ;;
	esac
	shift
done

[ -n "$VERSION" ] || usage 1
printf '%s' "$VERSION" | grep -Eq '^[0-9]+\.[0-9]+\.[0-9]+$' ||
	die "version must be a release version like 2.2.1 (got '$VERSION')"

IFS=. read -r MAJOR MINOR PATCH <<<"$VERSION"
NEXT="${MAJOR}.$((MINOR + 1)).0"
if [ "$PATCH" -ne 0 ]; then
	NEXT="${MAJOR}.${MINOR}.$((PATCH + 1))"
fi

command -v mvn >/dev/null || die "mvn not found on PATH"
command -v gpg >/dev/null || die "gpg not found on PATH - install gnupg (Debian/Ubuntu: apt install gnupg)"
command -v curl >/dev/null || die "curl not found on PATH"

# The settings file holding the Central token. -s wins, else the default, and
# every mvn call below gets it explicitly, so the file that is checked is the
# file that is used.
SETTINGS="${SETTINGS:-${HOME}/.m2/settings.xml}"
[ -f "$SETTINGS" ] ||
	die "no ${SETTINGS} - copy settings-release.xml.example and add your Central token (see README.release.md)"
grep -q '<id>central</id>' "$SETTINGS" ||
	die "${SETTINGS} has no <server> with <id>central</id> - copy settings-release.xml.example (see README.release.md)"

step "Preflight"

CURRENT=$(mvn -s "$SETTINGS" -q -N help:evaluate -Dexpression=project.version -DforceStdout 2>/dev/null |
	strip_ansi | tr -d '[:space:]')
[ -n "$CURRENT" ] || die "could not read the current version from pom.xml (is Maven working?)"
info "current version : ${CURRENT}"
case "$CURRENT" in
	*-SNAPSHOT) ;;
	*) die "current version '${CURRENT}' is not a -SNAPSHOT - bump back to a snapshot first" ;;
esac

if [ -n "$(git status --porcelain)" ]; then
	git status --porcelain
	die "working tree is not clean - commit or stash first"
fi

BRANCH=$(git rev-parse --abbrev-ref HEAD)
[ "$BRANCH" = "master" ] || [ "$BRANCH" = "main" ] ||
	die "releases must be cut from master/main (currently on '${BRANCH}')"

if git rev-parse "v${VERSION}" >/dev/null 2>&1; then
	die "tag v${VERSION} already exists"
fi

if curl -fsI --max-time 20 \
	"https://repo1.maven.org/maven2/${GROUP_PATH}/${PROBE_ARTIFACT}/${VERSION}/${PROBE_ARTIFACT}-${VERSION}.pom" \
	>/dev/null 2>&1; then
	die "${GROUP_PATH}:${PROBE_ARTIFACT}:${VERSION} is already on Maven Central - versions are immutable"
fi

# The keyring to sign with. Anything but $HOME/.gnupg has to be handed to both
# gpg and Maven, so resolve it once here and export it for the rest of the run.
export GNUPGHOME="${GNUPGHOME:-${HOME}/.gnupg}"
[ -d "$GNUPGHOME" ] ||
	die "no GPG home at '${GNUPGHOME}' - export GNUPGHOME=/path/to/keys"
# absolute, so a relative path or a stray trailing slash cannot be misread later
GNUPGHOME=$(cd "$GNUPGHOME" && pwd)
info "GPG home         : ${GNUPGHOME}"
GPG_HOME_MODE=$(stat -c %a "$GNUPGHOME" 2>/dev/null || stat -f %Lp "$GNUPGHOME" 2>/dev/null || true)
[ -z "$GPG_HOME_MODE" ] || [ "$GPG_HOME_MODE" = 700 ] ||
	info "                   mode ${GPG_HOME_MODE} - gpg complains until this is 700"

SIGNING_KEYS=$(gpg --batch --list-secret-keys --with-colons 2>/dev/null | grep -c '^sec' || true)
[ "$SIGNING_KEYS" -gt 0 ] ||
	die "no gpg secret key in GNUPGHOME='${GNUPGHOME}' - see README.release.md"
info "signing key      : $(gpg --batch --list-secret-keys --with-colons 2>/dev/null | grep '^fpr' | head -1 | cut -d: -f10)"

[ -n "${MAVEN_GPG_PASSPHRASE:-}" ] ||
	die "MAVEN_GPG_PASSPHRASE is not set - see README.release.md"

info "Central token    : configured (server id 'central' in ${SETTINGS})"
info "version to cut   : ${VERSION}"

if [ "$PUSH" = false ]; then
	info "--no-push given: the commit and tag will be created but not pushed"
fi

step "Bumping version to ${VERSION}"
mvn -s "$SETTINGS" -q versions:set -DnewVersion="${VERSION}" -DgenerateBackupPoms=false
# keep README's dependency snippets in sync with the release
if grep -qE '<version>[0-9]+\.[0-9]+\.[0-9]+</version>' README.md; then
	sed -i.bak -E "s|<version>[0-9]+\.[0-9]+\.[0-9]+</version>|<version>${VERSION}</version>|g" README.md
	rm -f README.md.bak
	info "updated version references in README.md"
fi
git --no-pager diff --stat

restore_version() {
	step "Restoring version ${CURRENT}"
	mvn -s "$SETTINGS" -q versions:set -DnewVersion="${CURRENT}" -DgenerateBackupPoms=false || true
	git checkout -- README.md 2>/dev/null || true
}

step "Building, signing and uploading to Maven Central"
if ! mvn -s "$SETTINGS" -DskipTests -P"${RELEASE_PROFILE}" clean deploy; then
	restore_version
	die "release build failed - nothing was tagged or pushed, fix and retry"
fi

step "Committing and tagging"
git commit -am "release ${VERSION}"
git tag -a "v${VERSION}" -m "Ardulink ${VERSION}"

if [ "$PUSH" = true ]; then
	info "pushing ${BRANCH} and v${VERSION}"
	git push origin "$BRANCH"
	git push origin "v${VERSION}"
fi

step "Done"
info "artifacts : https://central.sonatype.com/publishing/deployments"
if [ "$PUSH" = true ]; then
	info "git tag   : v${VERSION} (pushed)"
else
	info "git tag   : v${VERSION} (NOT pushed - 'git push origin master v${VERSION}')"
fi
cat <<EOF

pom.xml is now at ${VERSION}. When work continues, move to the next snapshot:

    mvn versions:set -DnewVersion=${NEXT}-SNAPSHOT -DgenerateBackupPoms=false
    git commit -am "next development version"

EOF