package kr.lanthanide.mindugradle

import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@Suppress("unused", "Unchecked_Cast", "Unused_Variable")
class MinduGradle: Plugin<Project> {
    companion object {
        private const val GITHUB_API_URL = "https://api.github.com/repos/Anuken/Mindustry/releases"
        private const val CLIENT_JAR_NAME = "Mindustry.jar"
        private const val SERVER_JAR_NAME = "server-release.jar"

    }

    override fun apply(project: Project) {
        val extension = project.extensions.create("mindugradle", MindustryExtension::class.java)

        extension.gameDataDir.convention(project.layout.buildDirectory.dir("mindugradle/run"))
        extension.cacheDir.convention(project.layout.buildDirectory.dir("mindugradle/caches"))

        val modHjsonFile = project.file("mod.hjson")

        val downloadMindustryJarsTask = project.tasks.register("downloadMindustryJars") { task ->
            task.group = "mindugradle"
            task.description = "Download Mindustry client and server jars"

            task.doLast {
                val cacheDir = extension.cacheDir.get().asFile
                val gameDataDir = extension.gameDataDir.get().asFile
                cacheDir.mkdirs()
                gameDataDir.mkdirs()

                // minGameVersion from mod.hjson
                val minGameVersion = getMinGameVersion(project, extension, modHjsonFile)

                project.logger.lifecycle("Using Mindustry game version: $minGameVersion")

                // use GitHub API to get game jar
                val releasesUrl = URI.create(GITHUB_API_URL).toURL()
                val releases = JsonSlurper().parseText(releasesUrl.readText()) as List<Map<String, Any>>

                val matchingRelease = releases.find { release ->
                    val tagName = release["tag_name"] as String
                    val versionNumber = tagName.replace(Regex("[^0-9]"), "")
                    versionNumber == minGameVersion
                }

                if (matchingRelease == null) {
                    project.logger.warn("Could not find Mindustry release for version $minGameVersion")
                    return@doLast
                }

                val assets = matchingRelease["assets"] as List<Map<String, Any>>
                val clientAsset = assets.find { it["name"] == CLIENT_JAR_NAME }
                val serverAsset = assets.find { it["name"] == SERVER_JAR_NAME }

                if (clientAsset != null) {
                    val downloadUrl = clientAsset["browser_download_url"] as String
                    val clientJarFile = File(cacheDir, "mindustry-$minGameVersion-client.jar")

                    if (!clientJarFile.exists()) {
                        project.logger.lifecycle("Downloading Mindustry client v$minGameVersion...")
                        downloadFile(URI.create(downloadUrl).toURL(), clientJarFile)
                        project.logger.lifecycle("Downloaded client JAR to: ${clientJarFile.absolutePath}")
                    } else {
                        project.logger.lifecycle("Using cached Mindustry client v$minGameVersion")
                    }
                }

                if (serverAsset != null) {
                    val downloadUrl = serverAsset["browser_download_url"] as String
                    val serverJarFile = File(cacheDir, "mindustry-$minGameVersion-server.jar")

                    if (!serverJarFile.exists()) {
                        project.logger.lifecycle("Downloading Mindustry server v$minGameVersion...")
                        downloadFile(URI.create(downloadUrl).toURL(), serverJarFile)
                        project.logger.lifecycle("Downloaded server JAR to: ${serverJarFile.absolutePath}")
                    } else {
                        project.logger.lifecycle("Using cached Mindustry server v$minGameVersion")
                    }
                }
            }
        }

        val runClientTask = project.tasks.register("runClient", RunClientTask::class.java) { task ->
            task.group = "mindugradle"
            task.description = "Runs the Mindustry client with mod"
            task.dependsOn(downloadMindustryJarsTask)
            task.gameDataDir.set(extension.gameDataDir)
            task.jvmArgs.set(extension.clientJvmArgs)
            task.cacheDir.set(extension.cacheDir)
            task.minGameVersion.set(getMinGameVersion(project, extension, modHjsonFile))
        }

        val runServerTask = project.tasks.register("runServer", RunServerTask::class.java) { task ->
            task.group = "mindugradle"
            task.description = "Runs the Mindustry server with mod"
            task.dependsOn(downloadMindustryJarsTask)
            task.gameDataDir.set(extension.gameDataDir)
            task.jvmArgs.set(extension.serverJvmArgs)
            task.cacheDir.set(extension.cacheDir)
            task.minGameVersion.set(getMinGameVersion(project, extension, modHjsonFile))
        }

        project.tasks.register("deployMod") { task ->
            task.group = "mindugradle"
            task.description = "Builds and deploys the mod to the Mindustry mods directory"
            task.dependsOn("jar")

            task.doLast {
                val modJar = project.tasks.getByName("jar").outputs.files.singleFile
                val modsDir = File(extension.gameDataDir.get().asFile, "mods")
                modsDir.mkdirs()

                project.copy {
                    it.from(modJar)
                    it.into(modsDir)
                }

                project.logger.lifecycle("Mod deployed to: ${modsDir.absolutePath}")
            }
        }

    }

    private fun getMinGameVersion(project: Project, extension: MindustryExtension, modHjsonFile: File): String {
        if (extension.minGameVersion.isPresent) {
            return extension.minGameVersion.get()
        }

        if (modHjsonFile.exists()) {
            try {
                val hjsonText = modHjsonFile.readText()
                val versionRegex = """minGameVersion\s*:\s*["']?(\d+)["']?""".toRegex()
                val match = versionRegex.find(hjsonText)
                return match?.groupValues?.get(1) ?: "135"
            } catch (e: Exception) {
                project.logger.warn("Failed to parse mod.hjson: ${e.message}")
            }
        }

        return "135"
    }


    private fun downloadFile(url: URL, destination: File) {
        url.openStream().use { input ->
            Files.copy(input, destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }


}