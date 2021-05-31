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

Our Gradle plugin ```shipkit-auto-version``` deducts the version for the Gradle project to streamline continuous delivery.
You drop a ```version.properties``` file to your repo with content like `version=1.0.*`.
The plugin will resolve the `*` part of the version based on the latest release tag and the number of commits.
This way you can set up the project for continuous delivery and release every merged pull request with nicely incremented version.
No more infamous "version bump" commits in every release!

```shipkit-auto-version``` plugin is **tiny** and has a single dependency on [jSemver](https://github.com/zafarkhaja/jsemver).
It is a safe dependency because it is tiny, has no dependencies, and it is final (no code changes since 2015 - it wraps semver protocol that had [no changes since 2013](https://github.com/semver/semver/tree/v2.0.0)).

Do you want to automate changelog generation?
Check out [shipkit-changelog](https://github.com/shipkit/shipkit-changelog) plugin that neatly integrate with ```shipkit-auto-version``` plugin.

## Customers / sample projects

- https://github.com/shipkit/shipkit-demo (great example/reference project)
- https://github.com/shipkit/shipkit-changelog
- https://github.com/shipkit/shipkit-auto-version (this project)
- https://github.com/linkedin/ambry
- https://github.com/mockito/mockito-scala
- https://github.com/mockito/mockito-testng
- https://github.com/mockito/mockito-kotlin

## USAGE
Shipkit Auto Version plugin supports two ways of releasing.
The basic usage of the plugin, reinforces releasing with every pull request merged to master - it is fully automated and depends on version spec in `version.properties` file.
The second one, that uses annotated tags to deduce version, is suitable for teams that prefer to cut release "on demand", rather than with every change on master.
Version is deduced from tag that code is checked out on. If the tag is valid (has same prefix as configured or default 'v' and version number is valid) deduced version is same as version in tag.

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
   __Basic usage__
   
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

   __Usage based on annotated tags__

   The `version.properties` file is optional. When using the plugin this way the version spec has to be empty
   (`version=`, no version spec in the file or no file at all).
   If a tag prefix other than 'v' is used it has to be specified in `version.properties` (same way as for "basic usage", eg. for "no prefix" please use `tagPrefix=`)


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

## Version Overriding

It is sometimes useful to manually specify the version when building / publishing (e.g. to publish a `-SNAPSHOT`
locally). This can be done by setting the gradle project version on the command line with the `-Pversion`
flag, e.g. `./gradlew publishToMavenLocal -Pversion=1.0.0-SNAPSHOT`.

## Implementation details

### Basic usage with version specified in properties file

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

### Usage based on annotated tag

Version is not specified in `version.properties`, the file is optional
(when other tag prefix than default 'v' is used for tags in a project it has to be specified with `tagPrefix`).
When version spec is not configured the plugin checks if code is checked out on annotated tag (with `git describe --tags`) and if tag's prefix is the same as this specified in `version.properties` the plugin picks the same version as version number in tag
(e.g. no `version.properties` file in project and code is checked out on "v1.5.2" - the plugin picks version number "1.5.2"; more possible cases in table below).

When the code is not checked out on annotated tag but ahead of it, the plugin will pick last matching tag version number
with patch version number increased by 1 and with added "-SNAPSHOT" suffix (e.g. "git describe --tags" result is "v1.5.2-4-sha123" -> plugin deduces "1.5.3-SNAPSHOT").

When tag's prefix doesn't match `tagPrefix` the plugin picks fallback version number which is "0.0.1-SNAPSHOT".

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
