# Drive Compatibility Targets

## CurseMaven Mod Coordinates
- `curse.maven:crazyae-1111042:6840104` (CrazyAE)
- `curse.maven:ae-additions-extra-cells-2-fork-493962:3814371` (AE Additions - ExtraCells2 Fork)

## Local Drop-in Testing
- LazyAE2 (drop a jar into `local-test-mods/1.12.2/`; not resolved through Maven)

## Active Test Jars
- `crazyae-1111042-6840104.jar`
- `ae-additions-extra-cells-2-fork-493962-3814371.jar`
- `forgelin-continuous-456403-7700023.jar` (runtime support for AE Additions)

## Drive Target Metadata
Confirmed against the actual test jars currently staged under `run/client/mods/1.12.2`.

| Mod | Drive Block Class | Tile Entity Class | Renderer / Model Entry |
| --- | --- | --- | --- |
| CrazyAE | `dev.beecube31.crazyae2.common.blocks.storage.BlockDriveImproved` | `dev.beecube31.crazyae2.common.tile.storage.TileImprovedDrive` | `dev.beecube31.crazyae2.client.rendering.ImprovedDriveRendering` / model `crazyae:builtin/driveimp` |
| AE Additions - ExtraCells2 Fork | `com.the9grounds.aeadditions.block.BlockHardMEDrive` | `com.the9grounds.aeadditions.tileentity.TileEntityHardMeDrive` | `com.the9grounds.aeadditions.models.drive.HardDriveModel` / model `aeadditions:builtin/hard_drive` |
| LazyAE2 | TODO | TODO | TODO |
