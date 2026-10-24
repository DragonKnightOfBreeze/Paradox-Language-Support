## 提供 `PushStream`

> - 模块：后端 > 通用
> - 版本：v3.0.5
> - 日期：2026.10.24
> - 状态：WIP

## 说明

为了在保持代码的简洁性和易读性的同时，保持性能，需要提供一种轻量级的 pushStream（推流）。

这样的 pushStream 不要求中间操作（例如首先遍历所有元素并收集到列表中），也不会引入状态机和堆分配（例如协程上下文和中间结果列表）。
相比常见的 pullStream（拉流），例如 Iterator/Stream/Sequence，这在一些场景（例如惰性遍历）下可以拥有更好的性能。
相比平台代码中常见的 processor 模式（例如 `psiClass.procesMethods(processor)`），还可以通过链式调用写法（类似 Stream/Sequence）来获得更好的可读性。

pushStream 依赖原生调用栈，只需要将元素作为参数压入当前栈帧，传递给下游流，而不需要暂存任何状态。
作为对比，pullStream 存在状态持久化的核心开销，必须将遍历的状态保存在堆内存的对象字段上（例如节点指针、协程的状态机闭包）。

pushStream 不存在中间开销，至于仍然存在的虚函数开销与闭包对象创建开销，往往微乎其微，因为 JIT 预期会对此进行优化（单态内联、标量替换、逃逸分析）。
作为对比，pullStream 的每次状态切换，都是在堆上读写状态，而这往往意味着额外的性能开销。
在极高的时间复杂度下，两者的性能差异显然会更加显著。

## 笔记

### 概述

- 提供简洁、灵活且更加符合直觉的 API 调用方式，同时保持性能
- 零开销的推模式流（区别于 Iterator/Stream/Sequence 等拉模式流）
- 拥有更高性能的同时，也能保持可读性
- 声明为内联类，接收 `dispatch: Processor<in T>.() -> Boolean`
- 提供一组预定义的内联高阶方法，如 map/filter/any/all/none
- 可以用来直接封装 processXxx 方法，也可以用来作为 searchXxx 方法的返回值
- 有限迁移：目前仅有限应用于强调“读”（而非“遍历”）的一些API
- 例如：expansion/relatedItems/presentableItems 的相关代码

### 实现方案

存在两种实现方案。

方案A（内联类包装函数）：

```kotlin
@JvmInline
value class PushStream<out T>(val dispatch: (Processor<in T>) -> Boolean)
```

方案B（直接使用函数式接口）：

```kotlin
fun interface PushStream<out T> {
    fun process(downstream: Processor<in T>): Boolean
}
```

在 JVM 字节码和 JIT 视角下，两者的开销几乎是一致的（除非发生内联类的意外装箱）。

为了避免引入过多的扩展函数，目前采用方案B。

### 实现示例

一个简单的 pushStream 的实现示例：

```kotlin
@JvmInline
value class PushStream<out T>(
    val dispatch: (Processor<in T>) -> Boolean
) {
    inline fun forEach(crossinline action: (T) -> Unit) {
        dispatch(Processor { item ->
            action(item)
            true
        })
    }

    inline fun <R> map(crossinline transform: (T) -> R): PushStream<R> = PushStream { downstream ->
        dispatch(Processor { item ->
            downstream.process(transform(item))
        })
    }

    inline fun filter(crossinline predicate: (T) -> Boolean): PushStream<T> = PushStream { downstream ->
        dispatch(Processor { item ->
            if (predicate(item)) downstream.process(item) else true
        })
    }

    inline fun any(crossinline predicate: (T) -> Boolean): Boolean {
        var found = false
        dispatch(Processor { item ->
            if (predicate(item)) {
                found = true
                false // 命中，立即中断整个生产者树
            } else {
                true
            }
        })
        return found
    }

    inline fun all(crossinline predicate: (T) -> Boolean): Boolean {
        var allMatch = true
        dispatch(Processor { item ->
            if (!predicate(item)) {
                allMatch = false
                false // 遇到反例，立即中断
            } else {
                true
            }
        })
        return allMatch
    }

    inline fun none(crossinline predicate: (T) -> Boolean): Boolean {
        return !any(predicate)
    }

    inline fun firstOrNull(crossinline predicate: (T) -> Boolean = { true }): T? {
        var result: T? = null
        dispatch(Processor { item ->
            if (predicate(item)) {
                result = item
                false // 找到第一个立刻短路
            } else {
                true
            }
        })
        return result
    }

    fun toList(): List<T> {
        val list = mutableListOf<T>()
        dispatch(Processor { item ->
            list.add(item)
            true
        })
        return list
    }
}

inline fun <T> pushStreamOf(crossinline producer: (Processor<in T>) -> Any?): PushStream<T> = PushStream { processor ->
      val res = producer(processor)
      if (res is Boolean) res else true
  }

fun <T> emptyPushStream(): PushStream<T> = PushStream { true }
```

## 任务

### 主要

## 参见

### 相关代码位置

- [ ] `icu.windea.pls.core.util.PushStream`
