#!/usr/bin/env bash
#
# Cut an Ardulink release: bump the version, build + GPG-sign, upload to the
# Maven Central Publisher Portal, then commit, tag and push.
#
# See README.release.md for prerequisites and for the manual equivalent.
#
#   ./release.sh 2.2.1
#   ./release.sh 2.2.1 --no-push     # build + upload only, no git push
#
set -euo pipefail

cd "$(dirname "$0")"

RELEASE_PROFILE="release"
GROUP_PATH="org/ardulink"
PROBE_ARTIFACT="ardulink-core-base"

PUSH=true
VERSION=""

die() { printf '\033[31merror\033[0m %s\n' "$*" >&2; exit 1; }
info() { printf '\033[32m==>\033[0m %s\n' "$*"; }
step() { printf '\n\033[1m%s\033[0m\n' "$*"; }
strip_ansi() { sed -E $'s/\x1b\\[[0-9;]*m//g'; }

usage() {
	sed -n '3,9p' "$0" | sed 's/^# \{0,1\}//'
	exit "${1:-1}"
}

while [ $# -gt 0 ]; do
	case "$1" in
		--no-push) PUSH=false ;;
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

step "Preflight"

CURRENT=$(mvn -q -N help:evaluate -Dexpression=project.version -DforceStdout 2>/dev/null |
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

SIGNING_KEYS=$(gpg --batch --list-secret-keys --with-colons 2>/dev/null | grep -c '^sec' || true)
[ "$SIGNING_KEYS" -gt 0 ] ||
	die "no gpg secret key found in GNUPGHOME=${GNUPGHOME:-$HOME/.gnupg} - see README.release.md"
info "signing key      : $(gpg --batch --list-secret-keys --with-colons 2>/dev/null | grep '^fpr' | head -1 | cut -d: -f10)"

[ -n "${MAVEN_GPG_PASSPHRASE:-}" ] ||
	die "MAVEN_GPG_PASSPHRASE is not set - see README.release.md"

SETTINGS="${HOME}/.m2/settings.xml"
[ -f "$SETTINGS" ] ||
	die "no ${SETTINGS} - copy settings-release.xml.example and add your Central token (see README.release.md)"
grep -q '<id>central</id>' "$SETTINGS" ||
	die "${SETTINGS} has no <server> with <id>central</id> - copy settings-release.xml.example (see README.release.md)"

info "Central token    : configured (server id 'central')"
info "version to cut   : ${VERSION}"

if [ "$PUSH" = false ]; then
	info "--no-push given: the commit and tag will be created but not pushed"
fi

step "Bumping version to ${VERSION}"
mvn -q versions:set -DnewVersion="${VERSION}" -DgenerateBackupPoms=false
# keep README's dependency snippets in sync with the release
if grep -qE '<version>[0-9]+\.[0-9]+\.[0-9]+</version>' README.md; then
	sed -i.bak -E "s|<version>[0-9]+\.[0-9]+\.[0-9]+</version>|<version>${VERSION}</version>|g" README.md
	rm -f README.md.bak
	info "updated version references in README.md"
fi
git --no-pager diff --stat

restore_version() {
	step "Restoring version ${CURRENT}"
	mvn -q versions:set -DnewVersion="${CURRENT}" -DgenerateBackupPoms=false || true
	git checkout -- README.md 2>/dev/null || true
}

step "Building, signing and uploading to Maven Central"
if ! mvn -DskipTests -P"${RELEASE_PROFILE}" clean deploy; then
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