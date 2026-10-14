# 测试代码补全时的注意事项

> - 模块：测试
> - 版本：v3.0.1
> - 日期：2026-07-25
> - 更新日期：2026-10-03

说明：
- 如果在测试代码补全时，只有唯一一个候选项，调用 `myFixture.complete` 后默认会直接插入这个候选项，并且修改 PSI。
- 这可能会导致意外报错：PSI and index do not match.
- 错误堆栈显示，文件内容是插入这个候选项之后的，在修改 PSI 时未通过断言。
- 参见：`icu.windea.pls.test.issues.Issue374Test.testCompletion_DynamicValue`

可行的解决方案：
- 临时禁用自动补全（`CodeInsightSettings.getInstance().AUTOCOMPLETE_ON_CODE_COMPLETION = false`）
