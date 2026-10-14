# 懒加载的魔法与24字节的摩擦

> - 模块：后端 > 通用
> - 版本：v3.0.2
> - 日期：2026-08-10
> - 状态：记录中

说明：
- Kotlin 标准库中提供的 `lazy {...}` 会带来额外的24字节的内存开销（对象本身+3个字段）。
- 在大多数情况下，通过使用内存友好的懒加载属性（基于 `@Volatile`，或者基于 `@Volatile` 和双重检查锁），可以避免这些内存开销。
- `LazyValue` 的伴生对象上提供了一组内联构建方法，用于以统一且相对简洁的格式，实现内存友好的懒加载属性。

参见：
- `kotlin.lazy`
- `icu.windea.pls.core.util.values.LazyValue.Builder.ofBoolean`
- `icu.windea.pls.core.util.values.LazyValue.Builder.ofNullable`
- `icu.windea.pls.core.util.values.LazyValue.Builder.of`
