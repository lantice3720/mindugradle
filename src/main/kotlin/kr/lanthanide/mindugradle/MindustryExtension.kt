package kr.lanthanide.mindugradle

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

abstract class MindustryExtension {
    /// game data directory
    @get:Input
    abstract val gameDataDir: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val clientJvmArgs: Property<String>

    @get:Input
    @get:Optional
    abstract val serverJvmArgs: Property<String>

    @get:Input
    @get:Optional
    abstract val minGameVersion: Property<String>

    @get:Input
    abstract val cacheDir: DirectoryProperty
}