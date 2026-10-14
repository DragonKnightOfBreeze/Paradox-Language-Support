# Gradle + JUnit4 测试无法读取 -D 系统属性

> - 模块：测试

- **背景**
  - 在命令行通过 Gradle 执行 JUnit4 测试时传入 `-Dkey=value`，期望在测试代码中通过 `System.getProperty("key")` 读取。
  - 项目中在 `build.gradle.kts` 里对 `tasks.withType<Test>` 做了自定义，仅设置了若干固定的 `systemProperty`，并未把命令行 `-D` 自动转发到测试 JVM。
- **现象**
  - 测试代码中 `System.getProperty("key")` 得到 `null`；或误用 `System.getenv("key")` 也读取不到。
- **排查过程**
  - 检查 `build.gradle.kts` 中的 `tasks.withType<Test> { ... }` 配置，发现只对 `pls.is.debug` 等固定键设值；`doFirst { systemProperties.filter { it.key.startsWith("pls.test.") } ... }` 读取的是 Test 任务自身属性，无法感知 Gradle 进程上的命令行 `-D`。
  - 确认差异：`-D` 为 JVM 系统属性，需用 `System.getProperty()`；环境变量应使用 `System.getenv()`，两者互不相通。
  - 在 IDE 里直接用 JUnit 运行器执行测试不会继承 Gradle 命令行的 `-D`；需“Delegate test execution to Gradle”或在运行配置里手动填写 VM Options。
  - Windows PowerShell 下需用 `\.\gradlew` 并对包含空格/反斜杠的值加引号。
- **结论（根因）**
  - 命令行 `-D` 仅作用于 Gradle 守护进程，未被转发到测试 JVM；现有脚本也未从 `System.getProperties()` 把指定前缀的属性注入到 Test 任务。
  - 部分场景还存在“把系统属性当作环境变量读取”的误用。
- **解决方案**
  - 方案 A（按前缀批量转发，推荐）
    ```kotlin
    // build.gradle.kts
    tasks.withType<Test>().configureEach {
        useJUnit()
        // 保留你已有的固定属性
        systemProperty("ide.slow.operations.assertion", "false")
        systemProperty("idea.log.debug.categories", "icu.windea.pls")
        systemProperty("pls.is.debug", "true")
        // 将命令行传入、以 pls.test. 开头的 -D 属性转发到测试 JVM
        val sys = System.getProperties()
        sys.stringPropertyNames()
            .filter { it.startsWith("pls.test.") }
            .forEach { k -> systemProperty(k, sys.getProperty(k)) }
    }
    ```
    - 调用示例：
      ```powershell
      .\gradlew test "-Dpls.test.foo=bar"
      ```
  - 方案 B（白名单键精确转发）
    ```kotlin
    tasks.withType<Test>().configureEach {
        useJUnit()
        listOf("key", "another.key").forEach { k ->
            System.getProperty(k)?.let { systemProperty(k, it) }
        }
    }
    ```
  - 方案 C（改用 Gradle 项目属性 `-P`）
    ```kotlin
    tasks.withType<Test>().configureEach {
        useJUnit()
        project.findProperty("key")?.let { systemProperty("key", it.toString()) }
    }
    ```
    - 调用：`\.\gradlew test -Pkey=value`
  - 方案 D（IDE）
    - 在 Run/Debug 配置的 VM Options 中填写 `-Dkey=value`，或勾选“Delegate to Gradle”。
- **验证**
  ```powershell
  .\gradlew test --tests icu.windea.pls.SomeTest "-Dpls.test.foo=bar" -i --stacktrace
  ```
  ```kotlin
  // 在测试中
  assertEquals("bar", System.getProperty("pls.test.foo"))
  ```
- **注意事项**
  - 使用 `System.getProperty` 读取 `-D`，不要误用 `System.getenv`。
  - PowerShell 下执行 Gradle 建议前缀 `\.\`；带空格/特殊字符的值请加引号。
