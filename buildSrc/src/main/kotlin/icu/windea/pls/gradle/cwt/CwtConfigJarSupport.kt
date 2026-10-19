package icu.windea.pls.gradle.cwt

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.bundling.Jar

/**
 * 将 CWT 配置内容添加到 JAR 任务中。
 */
object CwtConfigJarSupport {
    fun addToJar(
        task: Jar,
        extension: CwtConfigExtension,
        generatedCwtDir: Provider<Directory>,
        project: Project,
    ) {
        extension.repositories.get().forEach { repository ->
            val gameTypeId = repository.gameTypeId
            val localDir = project.layout.projectDirectory.dir("cwt/${repository.repoDir}")

            task.into("config/$gameTypeId") {
                // 优先使用本地配置，其次使用下载并解压到 build 目录的配置
                val effectiveSource = project.providers.provider {
                    val local = localDir.asFile
                    if (local.exists()) local else generatedCwtDir.get().dir(repository.repoDir).asFile
                }
                from(effectiveSource) {
                    includeEmptyDirs = false
                    include("**/*.cwt", "**/LICENSE", "**/*.md")
                    eachFile {
                        // 展平 `{repoDir}-{branch}` 目录以及可选的 `config` 目录
                        path = path.replace("/$gameTypeId/${repository.repoDir}-${repository.branch}/", "/$gameTypeId/", ignoreCase = true)
                        path = path.replace("/$gameTypeId/config/", "/$gameTypeId/", ignoreCase = true)
                    }
                }
            }
        }

        task.into("config") {
            from("cwt/README_zh.md", "cwt/LICENSE")
        }
    }
}
