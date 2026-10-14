# 插件 jar 内置规则文件更新后读取仍为旧内容

> - 模块：后端

- **背景**
  - 插件打包的内置规则文件（位于插件 jar 中 `resources`）在构建后发生更改。
  - 项目重启后通过 `PsiFile.text` 或 `VirtualFile.readText()` 读取仍是旧内容；只有当该文件在编辑器中打开过且再重启，才会读到新内容。

- **现象**
  - 通过 VFS/PSI 获取到的内容与最新构建的 jar 内部文件不一致，呈现“缓存未刷新”的状态。

- **排查过程**
  - 该问题高度符合 IntelliJ 平台 VFS（`JarFileSystem`/`ZipHandler` + PersistentFS）对 jar 条目内容的缓存与失效策略：
    - VFS 根据“外层 jar 的长度/mtime”与“zip entry 的时间戳”决定是否失效缓存；判断“未变”则复用旧内容。
    - Gradle 归档默认启用可复现构建（reproducible archives），可能导致 entry 时间戳被规范化为常量（如 1980-01-01）。若外层 jar 的 mtime/长度也未被有效更新，VFS 误判为未变。
  - 编辑器中打开该文件会触发更积极的刷新路径，从而出现“打开过+重启后才是新内容”的错觉。

- **结论（根因）**
  - Jar VFS 基于 jar/entry 修改戳的缓存未失效，叠加 Gradle 可复现归档导致 entry mtime 恒定，VFS 继续复用旧的 PersistentFS 内容。

- **解决方案**
  - 方案 A（推荐）：绕过 VFS，使用插件 ClassLoader 直接读取资源流。
    ```kotlin
    val loader = this::class.java.classLoader
    val text = loader.getResourceAsStream("rules/your.cwt")
      ?.use { it.readBytes().toString(Charsets.UTF_8) }
      ?: error("resource not found")
    ```
    - 适用于“只读的内置资源”。进程启动时 ClassLoader 指向新 jar，可稳定获取最新内容。
  - 方案 B：在启动时对插件 jar 根做定向 VFS 刷新（保留用 `VirtualFile`/`PsiFile` 的通路）。
    ```kotlin
    import com.intellij.ide.plugins.PluginManagerCore
    import com.intellij.openapi.application.ApplicationManager
    import com.intellij.openapi.extensions.PluginId
    import com.intellij.openapi.vfs.JarFileSystem
    import com.intellij.openapi.vfs.LocalFileSystem
    import com.intellij.openapi.vfs.VfsUtil

    fun refreshPluginJar(pluginId: String, jarName: String) {
        val plugin = PluginManagerCore.getPlugin(PluginId.getId(pluginId)) ?: return
        val jarPath = plugin.pluginPath.resolve("lib").resolve(jarName)
        val local = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(jarPath.toFile()) ?: return
        val jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(local) ?: return
        ApplicationManager.getApplication().invokeAndWait {
            VfsUtil.markDirtyAndRefresh(true, true, true, jarRoot)
        }
    }
    ```
    - 可在 `projectOpened`/`StartupActivity` 中调用；刷新范围仅限插件自身 jar，避免全局 IO 压力。
  - 方案 C：从构建源头修复可复现归档对 mtime 的影响，使 VFS 能正确判变。
    ```kotlin
    // build.gradle.kts
    import org.gradle.api.tasks.bundling.AbstractArchiveTask

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = true        // 允许 entry 使用真实时间戳
        // isReproducibleFileOrder = false     // 如需，可视情况关闭稳定排序
    }
    ```
    - 同时建议调试期清理/替换 IDE 沙箱中的插件目录或提升插件版本号，确保新 jar 被复制。
  - 方案 D（兜底，不建议长期使用）：全局 VFS 刷新
    ```kotlin
    com.intellij.openapi.vfs.VirtualFileManager.getInstance().asyncRefresh(null)
    ```

- **注意事项**
  - 若必须依赖 PSI/VFS 能力（索引、引用等），建议“用 ClassLoader 读取文本 → 如需 PSI，再用该文本生成临时 PSI/或在定向刷新后访问 `VirtualFile`”。
  - 刷新不要在 EDT 做重 IO；如在读锁（read-action）上下文中，避免切到模态进度 EDT，可用后台进度执行刷新。
  - Windows 下本地调试时，注意 PowerShell 执行 Gradle 用 `./gradlew` 前缀（`.\gradlew`）。

- **参考链接**
  - IntelliJ Virtual File System 概述：https://plugins.jetbrains.com/docs/intellij/virtual-file-system.html
  - Gradle 可复现归档与时间戳选项：
    - https://docs.gradle.org/current/userguide/working_with_files.html#sec:reproducible_archives
    - `AbstractArchiveTask.isPreserveFileTimestamps`/`isReproducibleFileOrder`
