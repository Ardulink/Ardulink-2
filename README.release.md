# Releasing Ardulink

How to cut a release: publish the jars to Maven Central and publish the zip
distribution on GitHub.

The short version, once [set up](#one-time-setup):

```bash
export MAVEN_GPG_PASSPHRASE='...'
export GNUPGHOME=/path/to/keys      # only if your keys are not in ~/.gnupg
./release.sh 2.2.1
```

That single command bumps every pom to `2.2.1`, builds, GPG-signs and uploads
all 21 modules plus the parent pom to Maven Central, waits until they are
published, then commits, tags `v2.2.1` and pushes. Pushing the tag makes GitHub
Actions build the zip and attach it to the GitHub release.

`GNUPGHOME` is optional: if your signing key is already in `~/.gnupg`, leave it
unset and ignore the line above - see [using a keyring outside your home
directory](#using-a-keyring-outside-your-home-directory).

---

## Why this is not just `mvn deploy`

Two things about Maven Central bite everyone once. Both are handled for you now.

**1. The old endpoint is dead.** Releases used to go to
`https://oss.sonatype.org/service/local/staging/deploy/maven2/`. Sonatype shut
OSSRH down on 2025-06-30; that host now answers `HTTP 402`. Publishing now goes
through the [Central Publisher Portal](https://central.sonatype.com) using
`central-publishing-maven-plugin`, configured in the `release` profile of
`pom.xml`.

**2. The build cache will happily replay a stale deploy.** This project enables
`maven-build-cache-extension` (`.mvn/extensions.xml`), which is great for day to
day builds and actively dangerous for releases: it restores already-signed
artifacts and a cached `deploy` from a previous snapshot build instead of
running the real thing. The `release` profile sets
`maven.build.cache.enabled=false`, so this cannot happen.

---

## One-time setup

### 1. Maven

Use the bundled wrapper, `./mvnw` (pins Maven 3.9.16). JDK 11 or newer.

### 2. GPG signing key

Central only accepts signatures from a key that is published on a public
keyserver, so generate the key **on the machine that will run the release** and
make sure the public part is uploaded:

```bash
gpg --full-generate-key     # RSA 3072+, no expiry, real name + e-mail
gpg --list-secret-keys      # note the key id
gpg --keyserver keyserver.ubuntu.com --send-keys <KEY-ID>
```

Verify it is visible publicly:

```bash
gpg --keyserver keyserver.ubuntu.com --recv-keys <KEY-ID>
```

> **Do not copy a `.gnupg` folder between operating systems.** A `gpg-agent`
> socket and its trust database do not survive the move, and the usual symptom
> is a hang or an unexplained `Inappropriate ioctl for device`. If a key already
> exists only on another machine, export and re-import it instead:
>
> ```bash
> # on the old machine
> gpg --armor --export-secret-keys <KEY-ID> > ~/ardulink-secret-key.asc
> # on the release machine
> gpg --import ~/ardulink-secret-key.asc
> ```
>
> Keep that `.asc` somewhere safe and private. Anyone holding it can publish
> under your identity.

Tell Maven the passphrase non-interactively:

```bash
export MAVEN_GPG_PASSPHRASE='...'
```

`maven-gpg-plugin` reads `MAVEN_GPG_PASSPHRASE`, and the `release` profile
passes `--pinentry-mode loopback` so it never tries to open a pinentry window.

#### Using a keyring outside your home directory

If your keys live somewhere other than `~/.gnupg`, export `GNUPGHOME` pointing at
the directory that holds them - it is the standard gpg variable, so both `gpg`
and Maven follow it:

```bash
export GNUPGHOME=/path/to/keys   # the directory holding pubring.kbx
export MAVEN_GPG_PASSPHRASE='...'
./release.sh 2.2.1
```

`release.sh` uses `GNUPGHOME` if it is set and falls back to `~/.gnupg`, exports
whichever it settled on so the preflight check and the Maven build cannot end up
on different keyrings, prints the path during preflight, and aborts if the
directory does not exist or holds no secret key. For a one-off
`mvn -Prelease deploy` outside the script, just export `GNUPGHOME` yourself -
Maven passes its environment to gpg.

Two things about a relocated keyring:

- The directory must be mode `700`. Anything else makes gpg print
  `WARNING: unsafe permissions on homedir` on every call.
- Do **not** move a `.gnupg` folder that came from another machine - see the
  warning below about the `gpg-agent` socket.

### 3. Central credentials

Create `~/.m2/settings.xml` from the checked-in template:

```bash
cp settings-release.xml.example ~/.m2/settings.xml
```

Then get a token from <https://central.sonatype.com> → *Account* → *Generate
User Token* → *Central Publishing Token*, and fill in:

```xml
<server>
    <id>central</id>
    <username>your token name</username>
    <password>your token password</password>
</server>
```

Two things people get wrong:

- These are the **token** name and password, not your Sonatype account login.
- The `<id>` must be exactly `central`. `pom.xml` refers to it via
  `<publishingServerId>central</publishingServerId>`.

If you already have a `~/.m2/settings.xml`, merge the `<servers>` block into it
instead of overwriting the file.

Confirm the namespace `org.ardulink` is active at
<https://central.sonatype.com/publishing/deployments>.

---

## Cutting a release

```bash
export MAVEN_GPG_PASSPHRASE='...'
./release.sh 2.2.1
```

Before touching anything, the script checks that:

- the version looks like `2.2.1` (no `-SNAPSHOT`)
- `pom.xml` is currently at a `-SNAPSHOT` version
- the working tree is clean and you are on `master`
- `v2.2.1` does not already exist as a tag
- `2.2.1` is not already on Maven Central (versions are immutable, this cannot
  be undone)
- a GPG secret key is present in `GNUPGHOME` (default `~/.gnupg`) and
  `MAVEN_GPG_PASSPHRASE` is set
- `~/.m2/settings.xml` has a `central` server

Then it bumps all 23 poms, runs `mvn -DskipTests -Prelease clean deploy`, and
only commits and tags **after** the upload succeeded. If the build fails it
restores the previous version and leaves git untouched.

To build and upload without pushing anything to git:

```bash
./release.sh 2.2.1 --no-push
```

### After the release

The script leaves `pom.xml` at the released version. Move to the next snapshot
before continuing development:

```bash
mvn versions:set -DnewVersion=2.2.2-SNAPSHOT -DgenerateBackupPoms=false
git commit -am "next development version"
```

### Watching the deployment

Every module is uploaded as one deployment named `Ardulink <version>`:

<https://central.sonatype.com/publishing/deployments>

The `release` profile sets `autoPublish=true` and `waitUntil=published`, so the
release is live by the time `release.sh` returns. If validation fails, the
reason is in the `release.sh` output and on that page.

Indexing into `search.maven.org` takes a while, so
`repo1.maven.org/maven2/...` may lag behind even after the release is published.

---

## Doing it by hand

If you need to run the steps separately:

```bash
# 1. bump the version everywhere
mvn versions:set -DnewVersion=2.2.1 -DgenerateBackupPoms=false

# 2. build, sign and publish in one pass
export MAVEN_GPG_PASSPHRASE='...'
export GNUPGHOME=/path/to/keys      # only if your keys are not in ~/.gnupg
mvn -DskipTests -Prelease clean deploy

# 3. tag and push - this triggers the GitHub release with the zip
git commit -am "release 2.2.1"
git tag -a v2.2.1 -m "Ardulink 2.2.1"
git push origin master
git push origin v2.2.1
```

Useful flags:

| Flag | Effect |
| --- | --- |
| `-DskipTests` | skip tests (CI already ran them) |
| `-DskipPublishing=true` | build and sign, create the bundle, do **not** upload |
| `-Dgpg.passphrase=...` | passphrase inline instead of via the environment |

### Dry run

`-DskipPublishing=true` still stages every artifact and writes the bundle, so it
is the safe way to check signing and packaging:

```bash
mvn -DskipTests -Prelease clean deploy -DskipPublishing=true
ls -lh target/central-publishing/central-bundle.zip
unzip -l target/central-publishing/central-bundle.zip | head -40
```

Verify a signature by hand (add `--homedir /path/to/keys`, or `GNUPGHOME`, if
that is not `~/.gnupg`):

```bash
gpg --verify ardulink-core-base/target/ardulink-core-base-2.2.1.jar.asc \
             ardulink-core-base/target/ardulink-core-base-2.2.1.jar
```

---

## What ends up where

| Artifact | Destination |
| --- | --- |
| 21 modules + the parent pom, each with jar, sources, javadoc and signatures | Maven Central |
| `ardulink-*.zip` | GitHub release, built by `.github/workflows/release.yml` on tag push |

`deploy-dist` is excluded from the Central bundle. It only produces the zip, and
although it sets `maven.deploy.skip`, that property is only honoured by
`maven-deploy-plugin` — which `central-publishing-maven-plugin` replaces. The
exclusion is set as `<excludeArtifacts>` in the `release` profile; without it
the ~33 MB zip would be uploaded to Central on every release.

---

## Troubleshooting

**`Unable to get publisher server properties for server id: central`**
`~/.m2/settings.xml` is missing or has no `<server>` with `<id>central</id>`.

**`401 Unauthorized`**
The token name or password is wrong, or you pasted your Sonatype account login
instead of the generated token.

**`gpg: signing failed: Inappropriate ioctl for device`**
gpg tried to use a pinentry prompt. Make sure `MAVEN_GPG_PASSPHRASE` is exported
in the shell that runs Maven, and that the `release` profile (which adds
`--pinentry-mode loopback`) is actually active.

**`no gpg secret key found`**
`gpg --list-secret-keys` is empty. Either `GNUPGHOME` points somewhere else, or
the key has to be imported - see [step 2](#2-gpg-signing-key). Check which
keyring gpg is actually using with `gpgconf --list-dirs homedir`.

**`gpg: WARNING: unsafe permissions on homedir`**
The keyring directory is not mode `700`:

```bash
chmod 700 "${GNUPGHOME:-$HOME/.gnupg}"
```

**`gpg: signing failed: ... No secret key`**
The key exists, but not in the keyring the build used. The variable has to be
exported, not just set in one shell, and `release.sh` has to be able to see it:
run `GNUPGHOME=/path/to/keys ./release.sh 2.2.1` and check the `GPG home` line
in the preflight output.

**The release is stuck waiting**
`waitMaxTime` is 3600s. Past that `release.sh` fails, but the deployment is
still on the Central Portal page and can be published by hand.

**Central rejects the bundle**
Read the validation errors on the deployments page first - it names the exact
file. The usual causes are a missing signature, or a version that already
exists.

---

## Reference

- [Central Publisher Portal](https://central.sonatype.com)
- [Publishing with the Maven plugin](https://central.sonatype.org/publish/publish-portal-maven/)
- [Central requirements](https://central.sonatype.org/publish/requirements/)
- [OSSRH end-of-life notice](https://central.sonatype.org/pages/ossrh-eol/)