@file:Suppress("unused")

package icu.windea.pls.gradle.cwt

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar

/**
 * `chronicle.cwt-config` 插件。
 *
 * 负责注册 CWT 配置的下载/解压任务，并将配置内容包含到插件 JAR 中。
 */
class CwtConfigPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("de.undercouch.download")

        val extension = project.extensions.create("cwtConfig", CwtConfigExtension::class.java)
        extension.downloadIfMissing.convention(true)
        extension.acceptAnyCertificate.convention(false)
        extension.repositories.convention(defaultRepositories)
        extension.includeInJar.convention(true)

        val generatedCwtDir = project.layout.buildDirectory.dir("generated/cwt")
        val prepareCwtConfigs = project.tasks.register("prepareCwtConfigs") {
            description = "Prepare CWT configs."
        }

        project.afterEvaluate {
            extension.repositories.get().filter { it.downloadable }.forEach { repository ->
                registerRepositoryTasks(project, extension, generatedCwtDir, repository, prepareCwtConfigs)
            }
        }

        project.tasks.matching { it.name == "jar" || it.name == "instrumentedJar" }.configureEach {
            if (this is Jar && extension.includeInJar.get()) {
                dependsOn(prepareCwtConfigs)
                CwtConfigJarSupport.addToJar(this, extension, generatedCwtDir, project)
            }
        }
    }

    private fun registerRepositoryTasks(
        project: Project,
        extension: CwtConfigExtension,
        generatedCwtDir: Provider<Directory>,
        repository: CwtRepository,
        prepareCwtConfigs: TaskProvider<Task>,
    ) {
        val localDir = project.layout.projectDirectory.dir("cwt/${repository.repoDir}")
        val zipFile = project.layout.buildDirectory.file("tmp/cwt/${repository.repoDir}.zip")
        val unzipDir = generatedCwtDir.map { it.dir(repository.repoDir) }

        val download = project.tasks.register("downloadCwtConfig_${repository.gameTypeId}", Download::class.java) {
            description = "Download CWT configs for game type id ${repository.gameTypeId}."
            src(project.providers.provider { repository.zipUrl })
            dest(zipFile)
            overwrite(false)
            if (extension.acceptAnyCertificate.get()) {
                acceptAnyCertificate(true)
            }
            onlyIf {
                extension.downloadIfMissing.get() &&
                    !localDir.asFile.exists() &&
                    !zipFile.get().asFile.exists()
            }
        }
        val unzip = project.tasks.register("unzipCwtConfig_${repository.gameTypeId}", Copy::class.java) {
            description = "Unzip CWT configs for game type id ${repository.gameTypeId}."
            dependsOn(download)
            onlyIf {
                extension.downloadIfMissing.get() && !localDir.asFile.exists()
            }
            from(project.zipTree(zipFile.get().asFile))
            into(unzipDir)
        }
        prepareCwtConfigs.configure { dependsOn(unzip) }
    }

    private companion object {
        val defaultRepositories = listOf(
            CwtRepository("core", "core", downloadable = false), // core is in this repository; no download needed
            CwtRepository("cwtools-ck2-config", "ck2"),
            CwtRepository("cwtools-ck3-config", "ck3"),
            CwtRepository("cwtools-eu4-config", "eu4"),
            CwtRepository("cwtools-eu5-config", "eu5"),
            CwtRepository("cwtools-hoi4-config", "hoi4"),
            CwtRepository("cwtools-ir-config", "ir"),
            CwtRepository("cwtools-stellaris-config", "stellaris"),
            CwtRepository("cwtools-vic2-config", "vic2"),
            CwtRepository("cwtools-vic3-config", "vic3"),
        )
    }
}
