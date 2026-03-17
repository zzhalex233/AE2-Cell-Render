# Compatibility Test Environment Design

## Background
- need a safe way to load AE2-compatibility mods for local testing without shipping them or making them formal dependencies
- drive-rendering rewrite should be validated against CrazyAE, LazyAE2, and AE Additions ExtraCells2 Fork in the dev environment

## Goals
1. Keep compatibility mods out of the published jar while still resolving them via Gradle.
2. Make it easy to drop a local LazyAE2 jar for testing.
3. Ensure runClient pulls all test mods into un/client/mods/1.12.2/ before start.
4. Document the test targets for future reference (drive block/tile/model info table placeholder).

## Architecture

1. Create a compatibilityTestMods configuration that is not part of the publish or runtime classpath.
2. Declare the CurseMaven coordinates for CrazyAE and AE Additions ExtraCells2 Fork in this configuration.
3. Add an SyncCompatibilityMods task that copies resolved dependencies from compatibilityTestMods and anything inside local-test-mods/1.12.2/ into un/client/mods/1.12.2/ before unClient executes.
4. Hook the task as a dependency of the unClient run configuration in the Unimined DSL to guarantee the sync happens before the client starts.
5. Add a .gitignore entry for local-test-mods/ so the LazyAE2 drop-in doesn't accidentally get committed.
6. Capture the compatibility targets in docs/compat/drive-targets.md, listing the Maven coordinates, the local LazyAE2 requirement, and providing a table for later drive metadata.

## Testing
Running the new sync task directly (./gradlew syncCompatibilityMods) should resolve the compatibility dependencies and copy them into the test un directory, demonstrating the configuration is wired. No production rendering code is touched yet.
