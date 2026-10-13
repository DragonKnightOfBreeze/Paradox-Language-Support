# 集合选型（collection selection）

> 所属模块：`后端 > 通用`
> 上级导航：[架构决策索引 (ADR Index)](README.md)

> - 版本：v3.0.1
> - 日期：2026-07-20
> - 状态：记录中

主要考量是在特定场景下的内存占用的细节，其次是作为一种统一的规范。

- `ArrayList` - 不需要保证线程安全的可变列表的默认选择。通常是但并不总是符合前述条件时的最佳选择。
- `SmartList` - 当长度为1时，相比 `ArrayList` 有着更少的内存占用。如果存在大量长度为1的不需要保证线程安全的不可变列表，这会是更好的选择。
- `ImmutableList` (guava) - 不可变列表，拥有更少的内存占用。对于反序列化后的索引数据、不可变缓存、以及部分轻量的无需缓存的计算结果，这会是更好（且优先）的选择。
- `IntArrayList` 等 (fastutil) - 不适合直接使用数组，且元素是原始类型时，这会是更好（且优先）的选择。
- `ObjectArrayList` (fastutil) - 相比 `ArrayList` 并没有明显的性能优势，仅在绝对必要时使用。例如需要使用到独有的API（如 `trim()`）。或者，作为一种统一的规范。
- `HashSet` - 不需要保证线程安全且不需要保证元素顺序的可变集的默认选择。通常是但同样并不总是符合前述条件时的最佳选择。
- `LinkedHashSet` - 在前者的基础上，如果需要保证元素顺序。
- `ImmutableList` (guava) - 不可变集，拥有更少的内存占用。对于反序列化后的索引数据、不可变缓存、以及部分轻量的无需缓存的计算结果，这会是更好（且优先）的选择。
- `IntOpenHashSet` 等 (fastutil) - 元素是原始类型时，这会是更好（且优先）的选择。
- `ObjectOpenHashSet` (fastutil) - 拥有相对较少的内存占用。如果存在大量集，可以考虑使用。
  - 参见：`com.intellij.util.containers.CollectionFactory.createSmallMemoryFootprintSet()`
- `ObjectOpenLinkedHashSet` (fastutil) - 在前者的基础上，如果需要保证元素顺序。
  - 参见：`com.intellij.util.containers.CollectionFactory.createSmallMemoryFootprintLinkedSet()`
- `ObjectLinkedOpenCustomHashSet` (fastutil) - 可以用来创建大小写不敏感的字符串集。
  - 参见：`com.intellij.util.containers.CollectionFactory.createCaseInsensitiveStringSet()`
- `ObjectArraySet` (fastutil) - 拥有更少的内存占用。如果存在大量集，并且重排序时的性能相比之下并不重要，这会是更好的选择。
- `ImmutableMap` (guava) - 不可变映射，拥有更少的内存占用。对于反序列化后的索引数据、不可变缓存、以及部分轻量的无需缓存的计算结果，这会是更好（且优先）的选择。
- `Int2ObjectOpenHashMap` 等 (fastutil) - 元素是原始类型时，这会是更好（且优先）的选择。
- `Object2ObjectOpenHashMap` (fastutil) - 拥有相对较少的内存占用。如果存在大量映射，可以考虑使用。
  - 参见：`com.intellij.util.containers.CollectionFactory.createSmallMemoryFootprintMap()`
- `Object2ObjectLinkedOpenHashMap` (fastutil) - 在前者的基础上，如果需要保证元素顺序。
  - 参见：`com.intellij.util.containers.CollectionFactory.createSmallMemoryFootprintLinkedMap()`
- `Object2ObjectOpenCustomHashMap` (fastutil) - 可以用来创建键为忽略大小写的字符串的映射。
  - 参见：`com.intellij.util.containers.CollectionFactory.createCaseInsensitiveStringMap()`
- `Object2ObjectArrayMap` (fastutil) - 拥有更少的内存占用。如果存在大量映射，并且重排序时的性能相比之下并不重要，这会是更好的选择。
