[![CI](https://github.com/shipkit/shipkit-auto-version/workflows/CI/badge.svg)](https://github.com/shipkit/shipkit-auto-version/actions)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/org/shipkit/shipkit-auto-version/maven-metadata.xml.svg?label=Version)](https://plugins.gradle.org/plugin/org.shipkit.shipkit-auto-version)

<br>
<a href="https://github.com/shipkit">
<img src="https://raw.githubusercontent.com/shipkit/shipkit-resources/main/images/Shipkit-logo.png" width="240"
     alt="Shipkit Plugins"/>
</a>

## Vision

Software developers spend all their creative energy on productive work.
There is absolutely **zero** release overhead because all software is released *automatically*.

## Mission

Encourage and help software developers set up their releases to be fully automated.

# shipkit-auto-version Gradle Plugin

Our Gradle plugin ```shipkit-auto-version``` deducts the version for the Gradle project to streamline automation and continuous delivery of your code.
The idea is to infer the version from tags or from an optional `version.properties` file with a version spec like `1.0.*`.
This way, you don't need to do any "version bump" commits in every release!
This project is inspired on [Axion plugin](https://github.com/allegro/axion-release-plugin), and few others, listed later in this document.

```shipkit-auto-version``` plugin is **tiny** and has a single dependency on [jSemver](https://github.com/zafarkhaja/jsemver).
It is a safe dependency because it is tiny, has no dependencies, and it is final (no code changes since 2015 - it wraps semver protocol that had [no changes since 2013](https://github.com/semver/semver/tree/v2.0.0)).

Do you want to automate changelog generation?
Check out [shipkit-changelog](https://github.com/shipkit/shipkit-changelog) plugin that neatly integrate with ```shipkit-auto-version``` plugin.

## Customers / sample projects

- https://github.com/mockito/mockito
- https://github.com/mockito/mockito-scala
- https://github.com/mockito/mockito-testng
- https://github.com/mockito/mockito-kotlin
- https://github.com/linkedin/ambry
- https://github.com/shipkit/shipkit-demo (great example/reference project)
- https://github.com/shipkit/shipkit-changelog
- https://github.com/shipkit/shipkit-auto-version (this project)

Do you want to add your project? Send us a PR!

## Usage

Shipkit Auto Version plugin supports two release models.

- Release **every change**: in order to release every change (or every pull request), use the `version.properties` file and specify the version spec, for example `version=1.0.*`.
When building the project our plugin will set the version based on the current tags in the repo and the version spec in the version file.
This approach is suitable for teams that release every change.
- Release **every tag**: in order to release when a tag is pushed to the repo, delete the `version.properties` file or remove `version` property from its contents.
When building the project our plugin sets the version based on the currently checked out tag.
This approach is suitable for teams that prefer to cut release *on demand*, rather than with every change on master.

*If you are unsure what release model is good for you, start releasing every change taking full advantage of continuous delivery.*

Steps:

1. Apply `org.shipkit.shipkit-auto-version` to the root project.
   Use the *highest* version available in the Gradle Plugin Portal
   [![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/org/shipkit/shipkit-auto-version/maven-metadata.xml.svg?label=version)](https://plugins.gradle.org/plugin/org.shipkit.shipkit-auto-version)

```
plugins {
  id "org.shipkit.shipkit-auto-version" version "x.y.z"
}
```

2. Configure and use the plugin in a preferred way.
   
   \
   __If you release relese every change__
   
   Create `version.properties` file and drop it to your project root.
   The contents should contain the version spec, and optionally, the tag prefix.

   ```
   version=1.0.*
   ```

   You may optionally specify the tag prefix, our default is "v" for tags like "v1.2.3".
   To use "no prefix" convention (e.g. tags like "1.2.3") please use an empty value: `tagPrefix=`

   ```
   version=1.0.*
   #tag prefix is optional, the default is "v", empty value means no prefix
   tagPrefix=release-
   ```

   __If you release every tag__

   The `version.properties` file is optional. When using the plugin this way the version spec has to be empty
   (`version=`, no `version` property, or no file at all).
   By default, the plugin assumes 'v' prefix for tag naming conveniton.
   To specify a different convention, use `tagPrefix` property in `version.properties` file.

3. For your CI, make sure that all tags are fetched (see the next section)

4. Prosper! When running Gradle the plugin will pick desired version and set this value on the Gradle's project.

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

## Properties exposed by the plugin

### shipkit-auto-version.previous-version
This plugin exposes an 'ext' property `shipkit-auto-version.previous-version` that can be used to get access to the previous version.
Example:

```groovy
println project.ext.'shipkit-auto-version.previous-version'
```
### shipkit-auto-version.previous-tag
Shipkit Auto Version exposes also `shipkit-auto-version.previous-tag` 'ext' property that gives access to the previous
version's tag. It allows to get previous revision in convenient way (eg. for generating changelog with Shipkit Changelog
plugin as in example below).
Example:

```groovy
tasks.named("generateChangelog") {
   previousRevision = project.ext.'shipkit-auto-version.previous-tag'
   //...
}
```

## Version overriding

It is sometimes useful to manually specify the version when building / publishing (e.g. to publish a `-SNAPSHOT`
locally). This can be done by setting the gradle project version on the command line with the `-Pversion`
flag, e.g. `./gradlew publishToMavenLocal -Pversion=1.0.0-SNAPSHOT`.

## Implementation details

### When releasing every change

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

### When releasing every tag

Version is not specified in `version.properties` and the file is optional when using default tag prefix "v".
For custom tag naming conventions you still need `tagPrefix` property in the version file.
The plugin runs `git describe --tags` to identify the tag you checked out.
For example, let's say the project uses default tag naming convention prefix "v".
If the code is checked out at "v1.5.2" the plugin sets version "1.5.2" on the Gradle project.

When the code is checked ahead of the tag, the plugin will pick last matching tag version number, increment the patch version and add "-SNAPSHOT" suffix.
For example, when `git describe --tags` yields `v1.5.2-4-sha123` the plugin uses "1.5.3-SNAPSHOT" version.

When tag does not match the convention (`tagPrefix`) the plugin picks fallback version number which is "0.0.1-SNAPSHOT".

#### Examples:

| case | spec (version, tagPrefix) | checked out on  | -Pversion       | result
|------|---------------------------|-----------------|-----------------|------------------------------------------------------
| h    | none, none                | v1.0.5          |                 | 1.0.5 (no 'tagPrefix' specified, default is 'v')
| i    | version= , none           | v1.0.5          |                 | 1.0.5 (no 'tagPrefix' specified, default is 'v')
| j    | none, tagPrefix=ver-      | ver-1.0.5       |                 | 1.0.5 ('tagPrefix' matches the tag)
| k    | none, none                | v1.0.5-2-sha123 |                 | 1.0.6-SNAPSHOT (ahead of "v1.0.5" tag)
| l    | none, tagPrefix=          | v1.0.5          |                 | 0.0.1-SNAPSHOT (empty tag prefix doesn't match 'v')
| m    | none, none                | v1.0.2          | 1.0.5-SNAPSHOT  | 1.0.5-SNAPSHOT (version overridden from CLI argument)

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

# Contributing

This project loves contributions!
For more info how to work with this project check out the *contributing* section in the sibling plugin [shipkit-changelog](https://github.com/shipkit/shipkit-changelog#contributing).
