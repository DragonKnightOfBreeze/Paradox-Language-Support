# 按条件忽略的测试被不正确地忽略了

> 所属类别：`测试`
> 上级导航：[问题解决与避坑索引 (Solutions Index)](README.md)

应当是配置缓存的问题

- 在执行测试命令之前停止守护进程（`./gradlew --stop`）
- 在测试命令中添加 `--no-configuration-cache`
- 通过添加系统属性来按需忽略测试（`-Dchronicle.test.include.all=true`）

```shell
./gradlew --stop
./gradlew test --tests "icu.windea.pls.ai.providers.ChatModelProviderTest" --no-configuration-cache
```
