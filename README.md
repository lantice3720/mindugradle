# mindugradle
[![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/kr.lanthanide.mindugradle)](https://plugins.gradle.org/plugin/kr.lanthanide.mindugradle)
[![Mindustry version](https://img.shields.io/badge/Mindustry-v139-blue)](https://mindustrygame.github.io/)

Your best modding partner.

Setting up modding environment every time you start developing a new Mindustry mod is a pain.<br/>
mindugradle will do boring, repetitive tasks for you!

- Automatically download mindustry jars according to your `mod.hjson`
- Provide Gradle tasks for easily running instanced Mindustry client/server with your mod installed

## Requirements
- Gradle 8.0+
- JDK 8 or later, works best in jbr 21

## Usage
First, add this plugin to your `build.gradle.kts`:
```kotlin
plugin {
    // other plugins
    id("kr.lanthanide.mindugradle") version("1.0")
}
```
And then, it's done! Enjoy developing your mod.<br/>
To run Mindustry client or server with your mod:
```bash
./gradlew runClient
./gradlew runServer
```

## Planned features
- Hotswap
  - Code changes without class structure edit
  - Class Redefinition
  - Assets
- Android build support

## Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

