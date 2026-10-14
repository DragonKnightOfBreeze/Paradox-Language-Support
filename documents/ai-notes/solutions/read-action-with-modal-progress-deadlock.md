# 在读操作中使用 withModalProgress 导致可能的死锁（IllegalStateException）

> - 模块：后端

- **背景**
  - 在初始化规则分组时，需要“先刷新规则目录（同步、可取消）再读取解析”。相关代码位于：
    - `src/main/kotlin/icu/windea/pls/ep/configGroup/CwtconfigGroupProcessors.FileBased.kt` 中 `FileBasedCwtconfigGroupProcessor.process()`。

- **现象**
  - 调试时抛出：
    ```text
    java.lang.IllegalStateException: Switching to `Dispatchers.EDT` from `runBlockingCancellable` inside in a read-action leads to possible deadlock.
    ```

- **排查过程**
  - `process()` 可能在读锁（read-action）下被调用；
  - 刷新时使用了：
    ```kotlin
    runBlockingCancellable {
        withModalProgress(project, title) {
            rootDirectory.refresh(false, true)
        }
    }
    ```
  - `withModalProgress(...)` 会切到 EDT（`Dispatchers.EDT`），而当前又处于 `runBlockingCancellable` 且在读锁中，平台为避免潜在死锁直接抛出异常。

- **结论（根因）**
  - 在读操作持锁期间，尝试从 `runBlockingCancellable` 切到 EDT（模态进度）是危险的，触发平台的死锁防护。

- **解决方案（已采用）**
  - 改用后台进度，不切到 EDT：
    - 导入：`com.intellij.platform.ide.progress.withBackgroundProgress`
    - 代码：
      ```kotlin
      runBlockingCancellable {
          withBackgroundProgress(configGroup.project, PlsBundle.message("configGroup.refresh.files.progressTitle", configGroup.gameType.id)) {
              fileProvidersAndRootDirectories.forEach { (_, rootDirectory) ->
                  rootDirectory.refresh(false, true) // 同步、递归刷新；后台线程执行
              }
          }
      }
      ```
  - 这保证“可取消 + 刷新完成后再继续”，并避免在读锁内切到 EDT。

- **备选方案**
  - 将“刷新 VFS”的步骤移出读锁：先刷新，后 `runReadAction { ... }` 解析；
  - 使用 `ReadAction.nonBlocking { ... }` 组织后续读取解析，避免长时间持有读锁；
  - 若确需模态进度（UI 阻塞）展示，必须确保当时不在读锁内，再切到 EDT 执行。

- **注意事项**
  - `VirtualFile.refresh(asynchronous = false, recursive = true)` 可在后台线程同步刷新，但不要在 EDT 调用以免卡 UI；
  - 读写锁配合进度与协程时，遵循“在后台线程做重 IO/CPU，UI/EDT 只做轻量操作”的原则；
  - 若上游调用链增加了读锁，需再次审视是否有隐式 EDT 切换的 API（模态对话框、同步 UI 操作等）。

- **参考链接**
  - IntelliJ 平台进度与协程 API（`runBlockingCancellable`/`withBackgroundProgress`）文档与源码注释
  - 相关变更：`CwtconfigGroupProcessors.FileBased.kt` 将 `withModalProgress` 替换为 `withBackgroundProgress`
