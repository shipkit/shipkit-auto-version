[![CI](https://github.com/shipkit/shipkit-auto-version/workflows/CI/badge.svg)](https://github.com/shipkit/shipkit-auto-version/actions)
[![Gradle Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/org/shipkit/shipkit-auto-version/maven-metadata.xml.svg?label=Gradle%20Plugins)](https://plugins.gradle.org/plugin/org.shipkit.shipkit-auto-version)

# Shipkit auto-version Gradle plugin

This plugin deducts the version for the Gradle project to streamline continuous delivery.
You drop a ```version.properties``` file to your repo with content like `version=1.0.*`. 
The plugin will resolve the `*` part of the version based on the latest release tag and commits.
This way you can set up the project for continuous delivery and release every merged pull request with nicely incremented version. 
No more infamous "version bump" commits in every release!

This plugin is **tiny** and has a single dependency on https://github.com/zafarkhaja/jsemver.
It is a safe dependency because it is tiny, has no dependencies, and it is final (no code changes since 2015 - it wraps semver protocol that had [no changes since 2013](https://github.com/semver/semver/tree/v2.0.0)).

## Customers / sample projects

- https://github.com/shipkit/shipkit-demo (great example/reference project)
- https://github.com/shipkit/shipkit-changelog
- https://github.com/shipkit/shipkit-auto-version (this project)
- https://github.com/linkedin/ambry
- https://github.com/mockito/mockito-scala

## Usage

1. Create `version.properties` file and drop it to your project root.
The contents should contain the version spec:

```
version=1.0.*
```

2. Apply `org.shipkit.shipkit-auto-version` to the root project.

3. For your CI, make sure that all tags are fetched (see the next section)

4. Prosper! When running Gradle build the plugin will resolve `*` part of the version and set this value on the Gradle's project.

### Fetch depth on CI

CI systems are often configured by default to perform Git fetch with minimum amount of commits/tags.
However, our plugin needs tags in order to generate the release notes.
When using GH actions, please configure your checkout action to fetch the entire history.
Based on our tests in Mockito project, the checkout of the *entire* Mockito history (dating 2008)
has negligible performance implication (adds ~2 secs to the checkout).

```yaml
- uses: actions/checkout@v2   # docs: https://github.com/actions/checkout
  with:
    fetch-depth: '0' # will fetch the entire history
```

## shipkit-auto-version.previous-version

This plugin exposes an 'ext' property `shipkit-auto-version.previous-version` that can be used to get access to the previous version.
Example:

```
println project.ext.'shipkit-auto-version.previous-version'
```

## Version Overriding

It is sometimes useful to manually specify the version when building / publishing (e.g. to publish a `-SNAPSHOT`
locally). This can be done by setting the gradle project version on the command line with the `-Pversion`
flag, e.g. `./gradlew publishToMavenLocal -Pversion=1.0.0-SNAPSHOT`.
 
## Implementation details

When the plugin is applied to the project it will:

 - load the version spec from `version.properties`
    - if the gradle project already has a set version (e.g. from the command line), use that version
    - if no file or wrong format fail
    - if the spec does not contain the wildcard '*', we just use the version "as is" and return
    - if the spec has wildcard '*' patch version, we resolve the wildcard value from tags and commits:
    
 - run `git tag`
    - look for typical "version" tags in Git output (e.g. "v1.0.0", "v2.5.100")
    - identifies the latest (newest) version matching version spec
    - compares the version with the version spec:
 
| case | spec  | latest tag | -Pversion       | # of commits    | result          | description                          |
|------|-------|------------|-----------------|-----------------|-----------------|--------------------------------------|
| a    | 1.0.* | v1.0.5     |                 | 0               | 1.0.5           | zero new commits                     |
| b    | 1.0.* | v1.0.5     |                 | 2               | 1.0.7           | two new commits                      |
| c    | 1.0.* | v1.0.5     |                 | 5 (2 merge + 1) | 1.0.8           | two merge commits and new one on top |
| d    | 1.1.* | v1.0.5     |                 | 5               | 1.1.0           | first x.y.0 version                  |
| e    | 2.0.* | v1.0.5     |                 | 5               | 2.0.0           | first z.0.0 version                  |
| f    | 1.*.5 |            |                 |                 | error           | unsupported format                   |
| g    | 1.0.* | v1.0.5     | 1.0.10-SNAPSHOT | [any]           | 1.0.10-SNAPSHOT | version overridden from CLI argument |

 - in case a),b) we are resolving the wildcard based on # of commits on top of the tag
    - run `git log` to identify # of commits 
    - add commit count to the patch version value from the latest tag
    - viola! we got the version to use!
 - in case c) we have following situation (`git log` output):
    - 64e7eb517 Commit without a PR
    - 2994de4df Merge pull request #123 from mockito/gradle-wrapper-validation
    - 67bd4e96c Adds Gradle Wrapper Validation
    - 64e7eb517 Merge pull request #99 from mockito/ongoing-stubbing
    - dd8b07887 Add OngoingStubbing
    - 084e8af18 (tag: v1.0.5) Merge pull request #88 from mockito/mockito-88
    
    On top of v1.0.5 tag there are 5 commits, i.e. 2 merge commits (`64e7eb517` and `2994de4df`) and 1 new commit 
    (`64e7eb517`) without Pull Request on top. 
    The patch version will be `8` i.e. 5 plus a sum of those two numbers. 

 - in case d),e) use '0' as patch version
 - in case g) the user manually specified the version on the command line

## Similar plugins

There are other plugins out there that are similar:

1. Below plugins are great, but they (mostly) require to push a tag to make a release.
Our plugin can release every change. 
 - https://github.com/ajoberstar/reckon
 - https://github.com/allegro/axion-release-plugin
 - https://github.com/cinnober/semver-git
 - https://github.com/nemerosa/versioning
 
2. Below plugin can release every change, but the resulting version is not as nice (e.g. ```1.0.0+3bb4161```).
The plugin has many features and thus is much more complex than our plugin.  
 - https://github.com/tschulte/gradle-semantic-release-plugin

Use the plugin that works best for you and push every change to production!

Discussion about this use case: https://github.com/mockito/shipkit/issues/395 
