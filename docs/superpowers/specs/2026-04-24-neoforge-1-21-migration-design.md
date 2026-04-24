# Elevators NeoForge 1.21 Migration Design

## Goal

Migrate the existing `Elevators` mod from Forge `1.20.1` to NeoForge `1.21.x` in an isolated git worktree while preserving the current mod identity (`mod_id=elevators`) and keeping player-facing behavior unchanged.

## Current Context

- The main branch builds a Forge `1.20.1` mod from the repository root.
- The mod logic is compact and centered around four classes:
  - `Elevators`: mod bootstrap and configuration setup
  - `ForgeEventHandler`: player tick, jump, and command registration events
  - `ElevatorController`: elevator scanning and teleport logic
  - `Config`: common config storage plus command-driven runtime updates
- The repository also contains an untracked `mc_1_21_0/` directory with a NeoForge template project. It may be used as a reference for build conventions, but it is not the migration target and should not remain as the primary project structure.

## Recommended Approach

Use a dedicated git worktree branch (`neoforge-1.21`) and migrate the root project in place inside that worktree. This keeps the original `1.20.1` line intact, avoids turning the repository into a multi-project migration experiment, and gives the `1.21.x` branch a clean history.

## Alternatives Considered

### 1. Migrate the root project in a dedicated worktree branch

This is the recommended option.

- Pros:
  - Clean branch isolation
  - Preserves repository shape
  - Keeps mod identity stable
  - Easiest to review and merge
- Cons:
  - Requires adapting the existing root project to NeoForge conventions

### 2. Promote `mc_1_21_0/` into the new version branch

- Pros:
  - Reuses an existing NeoForge template
- Cons:
  - The directory is currently an untracked nested project with its own `.git` metadata and generated outputs
  - Requires extra cleanup before it is safe to treat as product code
  - Makes review history noisier

### 3. Migrate directly on the current branch

- Pros:
  - Fastest initial path
- Cons:
  - Breaks isolation
  - Increases rollback risk
  - Conflicts with the requested worktree-based workflow

## Architecture

The migration will keep the existing code layout unless NeoForge `1.21.x` requires small structural changes for event registration or metadata generation.

At a high level:

- Replace ForgeGradle and Forge dependency wiring with NeoForge moddev configuration
- Replace Forge metadata/resource conventions with NeoForge `1.21.x` equivalents
- Adapt runtime registration code to NeoForge event bus and registry APIs
- Keep elevator movement logic behaviorally identical unless an API change forces a compatibility adjustment

## Components and Responsibilities

### Build and metadata

Files in this area define the runtime target and packaging metadata.

- `build.gradle`
  - switch from ForgeGradle to NeoForge moddev plugin
  - configure Java version expected by the selected NeoForge release
  - generate mod metadata resources from templates if needed by the new plugin conventions
- `gradle.properties`
  - update Minecraft, NeoForge, loader, mappings, mod version, and metadata properties
- `settings.gradle`, `gradle/wrapper/*`, `gradlew*`
  - update only if required by the selected NeoForge setup
- `src/main/resources/...`
  - replace Forge `mods.toml` flow with the NeoForge `1.21.x` resource layout
  - update `pack.mcmeta` format

### Mod bootstrap and events

- `src/main/java/org/hhoao/mc/ironelevators/Elevators.java`
  - replace Forge bootstrap imports and config registration with NeoForge equivalents
  - initialize controller and event subscriptions in the NeoForge-supported way
- `src/main/java/org/hhoao/mc/ironelevators/ForgeEventHandler.java`
  - migrate event imports and subscriptions
  - preserve crouch-to-go-down and jump-to-go-up behavior
  - preserve the `/elevators` command tree

### Configuration and registry access

- `src/main/java/org/hhoao/mc/ironelevators/Config.java`
  - migrate config spec imports from Forge to NeoForge
  - migrate block registry lookups to NeoForge-supported registries
  - preserve the command-driven config mutation behavior

### Core gameplay logic

- `src/main/java/org/hhoao/mc/ironelevators/ElevatorController.java`
  - keep the elevator search and teleport rules stable
  - only change code here if a `1.21.x` API incompatibility requires it

## Data and Control Flow

The runtime behavior should remain:

1. Mod loads and registers common config.
2. The event handler listens for player tick and jump events on the server side.
3. When a player crouches on an elevator block, the mod searches downward.
4. When a player jumps on an elevator block, the mod searches upward.
5. The controller scans until it finds a configured elevator block with enough headroom or until it hits the configured bounds or a blocking rule.
6. Commands under `/elevators` update runtime config values and block-height mappings.

## Compatibility Constraints

- The final `1.21.x` branch should continue using `mod_id=elevators`.
- Existing package names may stay unchanged unless a rename is necessary for clarity; a rename is not part of the goal.
- The migration should not convert the mod into a multi-module build unless a hard NeoForge limitation requires it.
- The untracked `mc_1_21_0/` directory should not become a shipping dependency of the migrated mod.

## Error Handling

- Invalid or missing block registry lookups in config parsing should fail safely rather than crashing on malformed list entries.
- Command handlers should continue to send clear success messages after config updates.
- If NeoForge requires lifecycle-specific registration timing, bootstrap code should use the correct bus to avoid late-registration errors.

## Testing Strategy

Use TDD for behavior changes introduced during migration.

Minimum verification target:

- Add focused tests around config parsing and/or elevator search behavior where practical
- Verify the new tests fail before implementation and pass after implementation
- Run `bash ./gradlew test`
- Run at least one launch-oriented task such as `runServer` or `runClient` if the environment supports it, to catch metadata and registration issues that unit tests will not cover

## Implementation Boundaries

This migration includes:

- build script conversion
- metadata/resource conversion
- NeoForge API adaptation
- small compatibility fixes required for `1.21.x`
- tests needed to safely support the migration

This migration does not include:

- new gameplay features
- broad refactoring unrelated to the upgrade
- rewriting the mod around a new architecture

## Risks

- NeoForge `1.21.x` may require event or config registration changes that are not obvious from the current Forge `1.20.1` code
- The command registration and block registry access paths are the most likely API breakpoints
- The current project has no test suite yet, so migration safety depends on adding a small but meaningful test baseline during the work

## Success Criteria

- The branch builds successfully against NeoForge `1.21.x`
- The mod metadata identifies the mod as `elevators`
- The existing elevator behavior still works
- The `/elevators` commands still register and mutate config as expected
- The migration work lives cleanly on the `neoforge-1.21` worktree branch
