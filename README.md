# org.shipkit.auto.version Gradle plugin

This plugin deducts the version for the Gradle project to streamline continuous delivery.
Configure your project with version like `1.0.*`.
The plugin will resolve the `*` part of the version based on the latest release tag and commits.
This way you can set up the project for continuous delivery without the infamous "version bump" commits.

Discussion about this use case: https://github.com/mockito/shipkit/issues/395

This plugin is **tiny** and does not have any dependencies.

## Usage

1. Create `version.properties` file and drop it to your project root.
The contents should contain the version spec:

```
version=1.0.*
```

2. Apply `org.shipkit.auto.version` to the root project.

3. Prosper! When running Gradle build the plugin will resolve `*` part of the version and set this value on the Gradle's project.
 
## Implementation

When the plugin is applied to the project it will:

 - load the version spec from `version.properties`
    - if the spec does not contain the wildcard '*', we just use the version as is
    - if the spec has wildcard '*' patch version, we resolve the wildcard value from tags and commits:
    
 - run `git tag`
 - look for typical "version" tags in Git output (e.g. "v1.0.0", "v2.5.100")
 - identifies the latest (newest) version matching version spec
 - compares the version with the version spec:
 
 ```
case    spec       latest tag      # of commits     result  description
a       1.0.*      v1.0.5          2                1.0.7
b       1.1.*      v1.0.5          5                1.1.0   first x.y.0 version
c       2.0.*      v1.0.5          5                2.0.0   first z.0.0 version
d       1.*.5                                       error   unsupported format                   
```      
 
 - in case a) we are resolving the wildcard based on # of commits on top of the tag
    - run `git log` to identify # of commits 
    - add commit count to the patch version value from the latest tag
    - viola! we got the version to use!
 
 - identifies the total # of commits on top of the tag
 - bumps the version inferred from the tag by the # of commits on top of the tag
    - example - spec: `1.0.*`, tags: `v1.0.0`, `v1.0.1`, 5 commits on top of the tag, result: 1.0.1 + 5 = 1.0.6 
    