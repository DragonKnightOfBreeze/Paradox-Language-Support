# P17 启动IDE时报错 `Cannot find scheme: Islands Dark from plugins: icu.windea.pls;icu.windea.pls`

> 上级导航：[问题追踪与分析索引 (Issues Index)](README.md)

> - IDE 版本：IDEA 2025.2
> - 版本：v3.0.2
> - 日期：2026-09-02
> - 状态：可忽略

```
2026-09-02 18:52:26,935 [  23976]   WARN - #c.i.o.e.c.i.EditorColorsManagerImpl - Cannot find scheme: Islands Dark from plugins: icu.windea.pls;icu.windea.pls
```

分析：
- 这可能与 `runIde` 时使用的 IDE 版本有关，需要使用最新版本的 IDE 验证。

结论：
- 经验证，在 IDEA 2026.2.1 上不存在此问题（Color Scheme 设置页面的代码高亮符合预期）。应当是 IDE 版本兼容性问题，可以忽略。
