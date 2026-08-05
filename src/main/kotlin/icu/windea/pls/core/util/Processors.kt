@file:Suppress("unused")

package icu.windea.pls.core.util

import com.intellij.util.Processor

/**
 * 查找处理器：在处理到第一个满足条件的元素时终止迭代。
 *
 * 可通过重载 [accept] 指定匹配条件；匹配成功后结果存入 [result] 并返回 `false` 停止迭代。
 */
open class FindProcessor<T> : Processor<T> {
    private var _result: T? = null

    val result: T? get() = _result

    override fun process(e: T): Boolean {
        if (accept(e)) {
            _result = e
            return false
        }
        return true
    }

    protected open fun accept(e: T): Boolean {
        return true
    }
}

/**
 * 查重处理器：当处理到第二个满足条件的元素时终止迭代。
 *
 * 用于快速判断“是否存在重载/被重载项”（即至少存在两个匹配项）。
 */
open class DuplicateProcessor<T> : Processor<T> {
    private var _duplicated = false
    private var _count = 0

    val duplicated: Boolean get() = _duplicated

    override fun process(e: T): Boolean {
        if (accept(e)) {
            _count++
            if (_count >= 2) {
                _duplicated = true
                return false
            }
        }
        return true
    }

    protected open fun accept(e: T): Boolean {
        return true
    }
}

/**
 * 收集处理器：将满足条件的元素加入到给定集合 [collection]。
 *
 * 可通过重载 [accept] 指定过滤条件；处理始终返回 `true` 以继续迭代。
 */
open class CollectProcessor<T, C : MutableCollection<T>>(val collection: C) : Processor<T> {
    override fun process(e: T): Boolean {
        if (accept(e)) {
            collection.add(e)
        }
        return true
    }

    protected open fun accept(e: T): Boolean {
        return true
    }
}

open class AllProcessor<T> : Processor<T> {
    var result: Boolean = true

    override fun process(e: T): Boolean {
        if (accept(e)) {
            return true
        }
        result = false
        return false
    }

    protected open fun accept(e: T): Boolean {
        return true
    }
}

open class AnyProcessor<T> : Processor<T> {
    var result: Boolean = false

    override fun process(e: T): Boolean {
        if (accept(e)) {
            result = true
            return false
        }
        return true
    }

    protected open fun accept(e: T): Boolean {
        return true
    }
}

open class NoneProcessor<T> : Processor<T> {
    var result: Boolean = true

    override fun process(e: T): Boolean {
        if (accept(e)) {
            result = false
            return false
        }
        return true
    }

    protected open fun accept(e: T): Boolean {
        return true
    }
}
