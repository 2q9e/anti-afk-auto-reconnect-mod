# Version layout

The repository keeps one Gradle project per supported Minecraft version.

```text
anti-afk-auto-reconnect-mod/
|-- build.gradle
|-- settings.gradle
|-- shared/
|   `-- src/main/java/com/q9e/antiafk/
`-- versions/
    |-- 1.21.4/
    |   |-- build.gradle
    |   |-- gradle.properties
    |   `-- src/
    |-- 1.21.11/
    |   |-- build.gradle
    |   |-- gradle.properties
    |   `-- src/
    `-- 26.3/
        |-- build.gradle
        |-- gradle.properties
        `-- src/
```

`./gradlew build` builds all three modules. Each version owns its Minecraft/Fabric dependency pins, mod metadata, resources, and Minecraft-facing Java sources. Keep only behavior and data classes that do not depend on Minecraft APIs in `shared/`.

Minecraft 1.21.4 and 1.21.11 use Mojang mappings and the remapping Loom plugin. Minecraft 26.3 uses Fabric's non-remapping Loom setup. The Loom plugin runs on Java 25; module compilation targets Java 21 for both 1.21 versions and Java 25 for 26.3.
