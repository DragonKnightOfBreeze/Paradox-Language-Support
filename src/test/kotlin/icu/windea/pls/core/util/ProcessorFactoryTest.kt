package icu.windea.pls.core.util

import org.junit.Assert
import org.junit.Test

/**
 * @see ProcessorFactory
 */
class ProcessorFactoryTest {
    // region FindProcessor

    @Test
    fun find_noPredicate_test() {
        val p = ProcessorFactory.find<Int>()
        Assert.assertFalse(p.process(1)) // 无过滤条件时首个元素即匹配并终止
        Assert.assertEquals(1, p.result)
    }

    @Test
    fun find_withPredicate_test() {
        val p = ProcessorFactory.find<Int> { it > 2 }
        Assert.assertTrue(p.process(1)) // 不匹配，继续迭代
        Assert.assertNull(p.result)
        Assert.assertTrue(p.process(2))
        Assert.assertFalse(p.process(3)) // 匹配后终止
        Assert.assertEquals(3, p.result)
    }

    @Test
    fun find_noMatch_resultIsNull_test() {
        val p = ProcessorFactory.find<Int> { it > 10 }
        Assert.assertTrue(p.process(1))
        Assert.assertTrue(p.process(2))
        Assert.assertNull(p.result) // 无匹配时结果仍为 null
    }

    // endregion

    // region DuplicateProcessor

    @Test
    fun duplicate_noPredicate_test() {
        val p = ProcessorFactory.duplicate<Int>()
        Assert.assertFalse(p.duplicated)
        Assert.assertTrue(p.process(1)) // 第一个元素
        Assert.assertFalse(p.duplicated)
        Assert.assertFalse(p.process(2)) // 第二个元素 -> duplicated 并终止
        Assert.assertTrue(p.duplicated)
    }

    @Test
    fun duplicate_withPredicate_test() {
        val p = ProcessorFactory.duplicate<Int> { it > 1 }
        Assert.assertTrue(p.process(1)) // 不匹配，忽略
        Assert.assertFalse(p.duplicated)
        Assert.assertTrue(p.process(2)) // 第一个匹配
        Assert.assertFalse(p.duplicated)
        Assert.assertFalse(p.process(3)) // 第二个匹配 -> duplicated 并终止
        Assert.assertTrue(p.duplicated)
    }

    @Test
    fun duplicate_singleMatch_notDuplicated_test() {
        val p = ProcessorFactory.duplicate<Int> { it > 1 }
        Assert.assertTrue(p.process(2)) // 仅一个匹配
        Assert.assertFalse(p.duplicated)
    }

    // endregion

    // region CollectProcessor

    @Test
    fun collect_withCollection_test() {
        val target = mutableSetOf<Int>()
        val p = ProcessorFactory.collect(target)
        Assert.assertTrue(p.process(1))
        Assert.assertTrue(p.process(2))
        Assert.assertTrue(p.process(3))
        Assert.assertSame(target, p.collection) // 直接复用传入的集合
        Assert.assertEquals(setOf(1, 2, 3), p.collection)
    }

    @Test
    fun collect_withCollectionAndPredicate_test() {
        val p = ProcessorFactory.collect(mutableListOf<Int>()) { it % 2 == 0 }
        Assert.assertTrue(p.process(1))
        Assert.assertTrue(p.process(2))
        Assert.assertTrue(p.process(3))
        Assert.assertTrue(p.process(4))
        Assert.assertEquals(listOf(2, 4), p.collection)
    }

    @Test
    fun collect_noPredicate_test() {
        val p = ProcessorFactory.collect<Int>() // 默认使用可变列表
        Assert.assertTrue(p.process(1))
        Assert.assertTrue(p.process(2))
        Assert.assertEquals(listOf(1, 2), p.collection)
    }

    @Test
    fun collect_withPredicate_test() {
        val p = ProcessorFactory.collect<Int> { it % 2 == 0 }
        Assert.assertTrue(p.process(1))
        Assert.assertTrue(p.process(2))
        Assert.assertTrue(p.process(3))
        Assert.assertEquals(listOf(2), p.collection)
    }

    // endregion

    // region AllProcessor

    @Test
    fun all_noPredicate_test() {
        val p = ProcessorFactory.all<Int>()
        Assert.assertTrue(p.process(1)) // 无过滤条件时始终满足
        Assert.assertTrue(p.process(2))
        Assert.assertTrue(p.result)
    }

    @Test
    fun all_withPredicate_allMatch_test() {
        val p = ProcessorFactory.all<Int> { it > 0 }
        Assert.assertTrue(p.process(1))
        Assert.assertTrue(p.process(2))
        Assert.assertTrue(p.result)
    }

    @Test
    fun all_withPredicate_hasMismatch_test() {
        val p = ProcessorFactory.all<Int> { it > 0 }
        Assert.assertTrue(p.process(1))
        Assert.assertFalse(p.process(-1)) // 不匹配 -> result = false 并终止
        Assert.assertFalse(p.result)
    }

    // endregion

    // region AnyProcessor

    @Test
    fun any_noPredicate_test() {
        val p = ProcessorFactory.any<Int>()
        Assert.assertFalse(p.process(1)) // 无过滤条件时首个元素即匹配并终止
        Assert.assertTrue(p.result)
    }

    @Test
    fun any_withPredicate_match_test() {
        val p = ProcessorFactory.any<Int> { it > 2 }
        Assert.assertTrue(p.process(1))
        Assert.assertTrue(p.process(2))
        Assert.assertFalse(p.process(3)) // 匹配 -> result = true 并终止
        Assert.assertTrue(p.result)
    }

    @Test
    fun any_withPredicate_noMatch_test() {
        val p = ProcessorFactory.any<Int> { it > 10 }
        Assert.assertTrue(p.process(1))
        Assert.assertFalse(p.result)
    }

    // endregion

    // region NoneProcessor

    @Test
    fun none_noPredicate_test() {
        val p = ProcessorFactory.none<Int>()
        Assert.assertFalse(p.process(1)) // 无过滤条件时首个元素即“匹配” -> result = false 并终止
        Assert.assertFalse(p.result)
    }

    @Test
    fun none_withPredicate_noMatch_test() {
        val p = ProcessorFactory.none<Int> { it > 10 }
        Assert.assertTrue(p.process(1))
        Assert.assertTrue(p.process(2))
        Assert.assertTrue(p.result) // 无匹配 -> none 成立
    }

    @Test
    fun none_withPredicate_hasMatch_test() {
        val p = ProcessorFactory.none<Int> { it > 1 }
        Assert.assertTrue(p.process(1))
        Assert.assertFalse(p.process(2)) // 匹配 -> result = false 并终止
        Assert.assertFalse(p.result)
    }

    // endregion
}
