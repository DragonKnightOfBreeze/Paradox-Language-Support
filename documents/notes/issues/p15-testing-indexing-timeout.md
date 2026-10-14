# P15 测试过程中的 `java.lang.AssertionError: Indexing timeout`

> - 版本：v3.0.0
> - 日期：2026-07-13
> - 状态：已解决
> - 相关问题：https://github.com/JetBrains/intellij-platform-gradle-plugin/issues/2188

描述：执行平台测试时，在第一个测试用例之后，会发生 `java.lang.AssertionError: Indexing timeout`，导致无限等待。这会导致CI测试最终因超时而失败。

```
[AWT-EventQueue-0]

com.intellij.testFramework.PlatformTestUtil.waitWithEventsDispatching(java.util.function.Supplier<java.lang.String>, java.util.function.BooleanSupplier, int, java.lang.Runnable)
 com.intellij.testFramework.PlatformTestUtil.waitWithEventsDispatching(java.util.function.Supplier<java.lang.String>, java.util.function.BooleanSupplier, int)
  com.intellij.testFramework.PlatformTestUtil.waitWithEventsDispatching(java.lang.String, java.util.function.BooleanSupplier, int)
   com.intellij.testFramework.IndexWaiter.waitNow
// 其中的 condition 在执行第一次测试用例之后便无法获取？

com.intellij.testFramework.IndexWaiter.shouldWait
// scannerExecutor.hasQueuedTasks -> true
// scanningTask.value != null -> true

com.intellij.util.indexing.UnindexedFilesScannerExecutorImpl
com.intellij.util.indexing.UnindexedFilesScannerExecutorImpl.ScheduledScanningTask

// com.intellij.util.indexing.UnindexedFilesScannerExecutorImpl L129 // scanningTask.getAndUpdate { null }
// com.intellij.util.indexing.UnindexedFilesScannerExecutorImpl startTaskInSmartMode L350 // val updated = scanningTask.compareAndSet(old, new)  // 然后就不管 scaningTask 了？或者，为什么上一行指向的循环结束了？
// com.intellij.util.indexing.UnindexedFilesScannerExecutorImpl submitTask // isFullIndexUpdate -> cancelAllTasks -> startTaskInSmartMode

com.intellij.testFramework.LightPlatformTestCase.doSetup // 导致 ->
com.intellij.openapi.roots.impl.ProjectRootManagerImpl.makeRootsChange // 导致 ->
com.intellij.openapi.roots.impl.ProjectRootManagerImpl.fireRootsChanged // 导致 ->
com.intellij.util.indexing.ProjectEntityIndexingService.indexChanges // 导致 ->
com.intellij.util.indexing.UnindexedFilesScanner.queue // 导致 ->
com.intellij.util.indexing.UnindexedFilesScanner.submitTask

ScheduledScanningTask.futureHistory

// com.intellij.util.indexing.UnindexedFilesScannerExecutorImpl L129
```

解决方案：将 Gradle 插件 `org.jetbrains.intellij.platform` 的版本回退到 2.17.0 即可解决。

解决方案（补充）：更新到 2.18.0 或以上版本后，需要在 `build.gradle.kts` 中为 `testImplementation("org.jetbrains.kotlin:kotlin-test-junit")` 显式排除对 Kotlin 标准库（以及协程库）的依赖。
