package kr.lanthanide.mindugradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

abstract class RunServerTask : DefaultTask() {
    @get:InputDirectory
    abstract val gameDataDir: DirectoryProperty

    @get:InputDirectory
    abstract val cacheDir: DirectoryProperty

    @get:Input
    abstract val minGameVersion: Property<String>

    @get:Input
    @get:Optional
    abstract val jvmArgs: Property<String>

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun runServer() {
        logger.lifecycle("Starting Mindustry server...")

        val modJar = project.tasks.findByName("jar")?.outputs?.files?.singleFile
        if (modJar != null && modJar.exists()) {
            val modsDir = gameDataDir.get().dir("mods").asFile
            modsDir.mkdirs()

            project.copy {
                it.from(modJar)
                it.into(modsDir)
            }

            logger.lifecycle("Deployed mod to: ${modsDir.absolutePath}")
        }

        val version = minGameVersion.get()
        val serverJarFile = File(cacheDir.get().asFile, "mindustry-$version-server.jar")

        if (!serverJarFile.exists()) {
            throw IllegalStateException("Server JAR file not found. Run downloadMindustryJars task first.")
        }

        val jvmArgsList = jvmArgs.orNull?.split(" ") ?: listOf()

        val execArgs = mutableListOf<String>()
        execArgs.add("-jar")
        execArgs.add(serverJarFile.absolutePath)

        logger.lifecycle("Running: java [jvmArgs] [systemProps] ${execArgs.joinToString(" ")}")

        execOperations.exec {
            it.executable = "java"
            it.args = jvmArgsList + execArgs
            it.workingDir = gameDataDir.get().asFile
        }
    }

}