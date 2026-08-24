package icu.windea.pls.core

import com.intellij.openapi.util.UserDataHolderBase
import org.junit.Assert
import org.junit.Test

class RecursionExtensionsTest {
    // 注意：
    // `icu.windea.pls.core.util.recursion` 并非 `icu.windea.pls.core` 下的"组件"包（不同于 `optimizer`、`match` 等）。
    // `RecursionGuard` / `RecursionGuardContext` / `RecursionService` 仅出于隐藏实现细节、保持代码整洁清晰的考虑被移入其中，
    // 具体逻辑仍通过 `icu.windea.pls.core.RecursionExtensions.kt` 暴露，因此这里直接测试具体逻辑，而不是在 `util.recursion` 下另建测试类。

    @Test
    fun withRecursionGuard_returnsResult() {
        Assert.assertEquals("hello", withRecursionGuard("guard") { "hello" })
    }

    @Test
    fun runWithRecursionGuard_nonRecursive() {
        var count = 0
        val r = runWithRecursionGuard("guard", "key") { count++; 42 }
        Assert.assertEquals(42, r)
        Assert.assertEquals(1, count)
    }

    @Test
    fun runWithRecursionGuard_recursionReturnsNull() {
        var innerRan = false
        val r = runWithRecursionGuard("guard", "key") {
            // 内层使用同名同键的守卫，检测到递归，返回 null 且不执行 action
            val inner = runWithRecursionGuard("guard", "key") { innerRan = true; 1 }
            Assert.assertNull(inner)
            "outer"
        }
        Assert.assertEquals("outer", r)
        Assert.assertFalse(innerRan)
    }

    @Test
    fun runWithRecursionGuard_differentNamesIndependent() {
        var innerRan = false
        val r = runWithRecursionGuard("guardA", "key") {
            // 不同名字的守卫拥有独立的调用栈，相同键不构成递归
            val inner = runWithRecursionGuard("guardB", "key") { innerRan = true; 1 }
            Assert.assertEquals(1, inner)
            "outer"
        }
        Assert.assertEquals("outer", r)
        Assert.assertTrue(innerRan)
    }

    @Test
    fun withRecursionGuard_recursionCheckThrowsPrevented() {
        val r = withRecursionGuard("guard") {
            recursionCheck("key")
            recursionCheck("key") // 第二次检测到重复键，抛出 StackOverflowPreventedException
            "unreachable"
        }
        Assert.assertNull(r)
    }

    @Test
    fun withRecursionGuard_nullKeyRunsAction() {
        var ran = false
        val r = withRecursionGuard("guard") {
            withRecursionCheck(null) { ran = true; "done" }
        }
        Assert.assertEquals("done", r)
        Assert.assertTrue(ran)
    }

    @Test
    fun withContextRecursionGuard_returnsResult() {
        val context = UserDataHolderBase()
        Assert.assertEquals("hello", withContextRecursionGuard(context, "guard") { "hello" })
    }

    @Test
    fun runWithContextRecursionGuard_recursionReturnsNull() {
        val context = UserDataHolderBase()
        var innerRan = false
        val r = runWithContextRecursionGuard(context, "guard", "key") {
            val inner = runWithContextRecursionGuard(context, "guard", "key") { innerRan = true; 1 }
            Assert.assertNull(inner)
            "outer"
        }
        Assert.assertEquals("outer", r)
        Assert.assertFalse(innerRan)
    }
}
