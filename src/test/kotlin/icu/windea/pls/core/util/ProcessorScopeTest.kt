package icu.windea.pls.core.util

import org.junit.Assert
import org.junit.Test

/**
 * @see ProcessorFactory
 * @see ProcessorScope
 */
class ProcessorScopeTest {
    // region findFrom

    @Test
    fun findFrom_singleLambda_test() {
        // 单 lambda（尾随）风格：predicate 使用默认值 { true }，类型由 @BuilderInference 推断
        val result = ProcessorScope.findFrom { listOf(1, 2, 3).forEach { process(it) } }
        Assert.assertEquals(3, result)
    }

    @Test
    fun findFrom_twoLambdas_test() {
        // 双 lambda 风格：第一个是 source（buildAction），第二个是 predicate（非尾随 buildAction 需显式类型）
        val result = ProcessorScope.findFrom({ listOf(1, 2, 3, 4).forEach { process(it) } }) { it % 2 == 0 }
        Assert.assertEquals(4, result) // 源迭代不因 process 返回 false 而中断，结果为最后一个匹配
    }

    @Test
    fun findFrom_noMatch_returnsNull_test() {
        Assert.assertNull(ProcessorScope.findFrom<Int>({ listOf(1, 3, 5).forEach { process(it) } }) { it % 2 == 0 })
        Assert.assertNull(ProcessorScope.findFrom<Int> { }) // 空源
    }

    // endregion

    // region collectFrom

    @Test
    fun collectFrom_singleLambda_test() {
        val result = ProcessorScope.collectFrom { listOf(1, 2, 3).forEach { process(it) } }
        Assert.assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun collectFrom_twoLambdas_test() {
        val result = ProcessorScope.collectFrom({ listOf(1, 2, 3, 4).forEach { process(it) } }) { it % 2 == 0 }
        Assert.assertEquals(listOf(2, 4), result)
    }

    @Test
    fun collectFrom_emptySource_returnsEmptyList_test() {
        Assert.assertEquals(emptyList<Int>(), ProcessorScope.collectFrom<Int> { })
    }

    // endregion

    // region duplicateFrom

    @Test
    fun duplicateFrom_singleLambda_test() {
        Assert.assertTrue(ProcessorScope.duplicateFrom { listOf(1, 2).forEach { process(it) } })
        Assert.assertFalse(ProcessorScope.duplicateFrom { listOf(1).forEach { process(it) } })
    }

    @Test
    fun duplicateFrom_twoLambdas_test() {
        Assert.assertTrue(ProcessorScope.duplicateFrom({ listOf(1, 2, 3, 4).forEach { process(it) } }) { it % 2 == 0 })
        Assert.assertFalse(ProcessorScope.duplicateFrom({ listOf(1, 3, 5).forEach { process(it) } }) { it % 2 == 0 })
    }

    @Test
    fun duplicateFrom_emptySource_returnsFalse_test() {
        Assert.assertFalse(ProcessorScope.duplicateFrom<Int> { })
    }

    // endregion

    // region allFrom

    @Test
    fun allFrom_singleLambda_test() {
        Assert.assertTrue(ProcessorScope.allFrom { listOf(1, 2, 3).forEach { process(it) } })
    }

    @Test
    fun allFrom_twoLambdas_test() {
        Assert.assertTrue(ProcessorScope.allFrom({ listOf(1, 2, 3).forEach { process(it) } }) { it > 0 })
        Assert.assertFalse(ProcessorScope.allFrom({ listOf(1, -1).forEach { process(it) } }) { it > 0 })
    }

    @Test
    fun allFrom_emptySource_returnsTrue_test() {
        Assert.assertTrue(ProcessorScope.allFrom<Int> { }) // 空源默认视为全满足
    }

    // endregion

    // region anyFrom

    @Test
    fun anyFrom_singleLambda_test() {
        Assert.assertTrue(ProcessorScope.anyFrom { listOf(1, 2, 3).forEach { process(it) } })
    }

    @Test
    fun anyFrom_twoLambdas_test() {
        Assert.assertTrue(ProcessorScope.anyFrom({ listOf(1, 2, 3).forEach { process(it) } }) { it > 2 })
        Assert.assertFalse(ProcessorScope.anyFrom({ listOf(1, 2).forEach { process(it) } }) { it > 10 })
    }

    @Test
    fun anyFrom_emptySource_returnsFalse_test() {
        Assert.assertFalse(ProcessorScope.anyFrom<Int> { })
    }

    // endregion

    // region noneFrom

    @Test
    fun noneFrom_singleLambda_test() {
        // 单 lambda 风格下 predicate 恒为 true，源非空时必有“匹配”，因此 none 不成立
        Assert.assertFalse(ProcessorScope.noneFrom { listOf(1, 2).forEach { process(it) } })
    }

    @Test
    fun noneFrom_twoLambdas_test() {
        Assert.assertTrue(ProcessorScope.noneFrom({ listOf(1, 2).forEach { process(it) } }) { it > 10 })
        Assert.assertFalse(ProcessorScope.noneFrom({ listOf(1, 2).forEach { process(it) } }) { it > 1 })
    }

    @Test
    fun noneFrom_emptySource_returnsTrue_test() {
        Assert.assertTrue(ProcessorScope.noneFrom<Int> { })
    }

    // endregion

    // region others

    @Test
    fun repeatedCalls_areIndependent_test() {
        // 每次调用都基于全新的处理器，不共享状态
        Assert.assertEquals(2, ProcessorScope.findFrom { process(1); process(2) })
        Assert.assertEquals(4, ProcessorScope.findFrom { process(3); process(4) })
        Assert.assertEquals(listOf(1), ProcessorScope.collectFrom { process(1) })
        Assert.assertEquals(listOf(2), ProcessorScope.collectFrom { process(2) })
    }

    // endregion
}
