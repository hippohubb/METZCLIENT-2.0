# Metzclient — Fabric 1.21.1

A client-side Fabric mod project targeting Minecraft 1.21.1 and Java 21.

## Controls
- **Right Shift** — open/close the Metzclient menu.
- **K** — save a waypoint at your current position. Waypoints persist in `config/metzclient-waypoints.json`.
- **G** — inspect flourish for the enabled butterfly/bayonet cosmetic.

## Implemented in this revision
- Dark multi-column module menu with live search and module toggles.
- Persistent waypoint saving and waypoint HUD distance.
- Coordinates, FPS, clock, armor, and status-effect HUD lines.
- Client-side nearby container outlines for already-loaded blocks when Storage ESP is enabled.
- Sprint Assist, Toggle Sneak, Safe Walk, low-food and low-durability notices.
- Auto Miner interaction helper that continues vanilla block breaking on the block under the crosshair; normal reach/tool/server rules still apply.
- A nearby storage-cluster heuristic for Base Finder. This is not a hidden-base detector and never loads chunks.
- Procedural first-person HUD cosmetics for butterfly knife and bayonet, with a G-key inspect flourish. These are stylized 2D HUD drawings, **not CS2-quality 3D item models** and do not change the held Minecraft item.
- Blueprint text-file parser and validation in `config/metzclient/blueprints`; the example file is `examples/tiny-platform.mcbp`.

## Blueprint file format
First line: `width height length`. The following lines contain exactly one namespaced block ID per block, for example `minecraft:stone`. IDs are validated as text by the loader; automatic survival placement is not implemented in this revision.

## Build a JAR using GitHub (recommended)

1. Create a new **GitHub repository** and upload/push the contents of this `Metzclient` folder (the `build.gradle` file must be at the repository root, not inside an extra nested folder).
2. Open the repository's **Actions** tab.
3. The included workflow `.github/workflows/build.yml` runs automatically after a push to `main` or `master`. You can also choose **Build Metzclient JAR** and click **Run workflow**.
4. Wait for the workflow to finish. If it fails, open the failed job and read the first Gradle/compiler error; the source has not been compile-verified in this package.
5. On a successful run, open the workflow run and scroll to **Artifacts**. Download `metzclient-fabric-1.21.1` and unzip it.
6. Put the regular mod JAR (not the `-sources.jar`) into your Minecraft instance's `mods` folder. Make sure that instance uses **Minecraft 1.21.1**, **Fabric Loader**, and **Java 21**, with Fabric API installed.

The workflow uses Gradle 8.10.2 and Java 21, then runs `gradle clean build`. Locally, from this folder with Java 21 and Gradle installed, run `gradle clean build`; outputs are written to `build/libs/`.

## Important status note
The project has not been compiled or launched in the current environment, so treat this as a development build and report any Gradle/compiler errors for correction. The 100-item menu list is a catalog, not a claim that every listed module is implemented. Full Baritone-equivalent pathfinding, automatic schematic placement, robust base detection, and actual animated 3D CS2 knife models still require further implementation and testing. Use visual/automation features only in worlds and servers where you have permission.
