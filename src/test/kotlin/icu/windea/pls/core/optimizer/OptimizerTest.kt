package icu.windea.pls.core.optimizer

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import icu.windea.pls.core.ReadWriteAccess
import org.junit.Assert.*
import org.junit.Test

class OptimizerTest {
    // ========== String ==========
    @Test
    fun testStringOptimizer_equivalentInstancesInterned() {
        val optimizer = OptimizerFactory.forString()
        val s1 = String(charArrayOf('a', 'b', 'c'))
        val s2 = String(charArrayOf('a', 'b', 'c'))
        val r1 = optimizer.optimize(s1)
        val r2 = optimizer.optimize(s2)
        assertSame("Equivalent strings should be interned to the same reference", r1, r2)
        assertEquals("abc", r1)
    }

    @Test
    fun testStringOptimizer_idempotent() {
        val optimizer = OptimizerFactory.forString()
        val s = String(charArrayOf('x'))
        val r1 = optimizer.optimize(s)
        val r2 = optimizer.optimize(s)
        assertSame("Optimize should be idempotent for the same content", r1, r2)
        assertEquals("x", r1)
    }

    @Test
    fun testStringOptimizer_emptyString_shortCircuit() {
        val optimizer = OptimizerFactory.forString()
        val r = optimizer.optimize("")
        // 应直接返回标准空串单例
        assertSame("", r)
        assertEquals(0, r.length)
    }

    // ========== List ==========
    @Test
    fun testListOptimizer_emptyLists_valueEquality_only() {
        val optimizer = OptimizerFactory.forList<String>()
        val input1 = emptyList<String>()
        val input2 = mutableListOf<String>()
        val r1 = optimizer.optimize(input1)
        val r2 = optimizer.optimize(input2)
        assertTrue(r1.isEmpty())
        assertTrue(r2.isEmpty())
        assertEquals(input1, r1)
        assertEquals(input2, r2)
        // 引用相等性在“空”情形不可确定，不做断言
    }

    @Test
    fun testListOptimizer_singleton_transformsToKotlinSingleton_valueEqual() {
        val optimizer = OptimizerFactory.forList<String>()
        val input = mutableListOf("a")
        val result = optimizer.optimize(input)
        assertEquals(listOf("a"), result)
    }

    @Test
    fun testListOptimizer_multiElements_copiesToImmutable_valueEqual() {
        val optimizer = OptimizerFactory.forList<String>()
        val input = arrayListOf("a", "b")
        val result = optimizer.optimize(input)
        assertEquals(listOf("a", "b"), result)
        assertTrue("Should convert to Guava ImmutableList", result is ImmutableList<*>)
    }

    @Test
    fun testListOptimizer_ignore_immutable_returnsSelf() {
        val optimizer = OptimizerFactory.forList<Int>()
        val input = ImmutableList.of(1, 2, 3)
        val result = optimizer.optimize(input)
        assertSame("ImmutableList input should be returned as-is", input, result)
    }

    // ========== Set ==========
    @Test
    fun testSetOptimizer_emptySets_valueEquality_only() {
        val optimizer = OptimizerFactory.forSet<String>()
        val input1 = emptySet<String>()
        val input2 = mutableSetOf<String>()
        val r1 = optimizer.optimize(input1)
        val r2 = optimizer.optimize(input2)
        assertTrue(r1.isEmpty())
        assertTrue(r2.isEmpty())
        assertEquals(input1, r1)
        assertEquals(input2, r2)
        // 引用相等性在“空”情形不可确定，不做断言
    }

    @Test
    fun testSetOptimizer_multiElements_copiesToImmutable_valueEqual() {
        val optimizer = OptimizerFactory.forSet<String>()
        val input = linkedSetOf("a", "b")
        val result = optimizer.optimize(input)
        assertEquals(setOf("a", "b"), result)
        assertTrue("Should convert to Guava ImmutableSet", result is ImmutableSet<*>)
    }

    @Test
    fun testSetOptimizer_ignore_immutable_returnsSelf() {
        val optimizer = OptimizerFactory.forSet<Int>()
        val input = ImmutableSet.of(1, 2, 3)
        val result = optimizer.optimize(input)
        assertSame("ImmutableSet input should be returned as-is", input, result)
    }

    // ========== Map ==========
    @Test
    fun testMapOptimizer_emptyMaps_valueEquality_only() {
        val optimizer = OptimizerFactory.forMap<String, Int>()
        val input1 = emptyMap<String, Int>()
        val input2 = hashMapOf<String, Int>()
        val r1 = optimizer.optimize(input1)
        val r2 = optimizer.optimize(input2)
        assertTrue(r1.isEmpty())
        assertTrue(r2.isEmpty())
        assertEquals(input1, r1)
        assertEquals(input2, r2)
        // 引用相等性在“空”情形不可确定，不做断言
    }

    @Test
    fun testMapOptimizer_singleton_copiesToImmutable_valueEqual() {
        val optimizer = OptimizerFactory.forMap<String, Int>()
        val input = hashMapOf("a" to 1)
        val result = optimizer.optimize(input)
        assertEquals(mapOf("a" to 1), result)
        assertTrue("Should convert to Guava ImmutableMap", result is ImmutableMap<*, *>)
    }

    @Test
    fun testMapOptimizer_ignore_immutable_returnsSelf() {
        val optimizer = OptimizerFactory.forMap<String, Int>()
        val input = ImmutableMap.of("x", 2)
        val result = optimizer.optimize(input)
        assertSame("ImmutableMap input should be returned as-is", input, result)
    }

    // ========== String List (small-size interning) ==========
    @Test
    fun testStringListOptimizer_smallLists_interned_referenceEqual() {
        val optimizer = OptimizerFactory.forStringList()
        val l1 = listOf("a", "b")
        val l2 = listOf("a", "b")
        val r1 = optimizer.optimize(l1)
        val r2 = optimizer.optimize(l2)
        assertEquals(l1, r1)
        assertEquals(l2, r2)
        // 小集合应被驻留，等价内容返回同一引用
        assertSame(r1, r2)
    }

    @Test
    fun testStringListOptimizer_threshold_largeList_returnsSelf() {
        val optimizer = OptimizerFactory.forStringList()
        val input = (1..9).map { it.toString() } // size = 9 > SMALL_INTERN_THRESHOLD(8)
        val result = optimizer.optimize(input)
        // 大集合不驻留，应直接返回自身
        assertSame(input, result)
        assertEquals(9, result.size)
    }

    // ========== String Set (small-size interning) ==========
    @Test
    fun testStringSetOptimizer_smallSets_interned_referenceEqual() {
        val optimizer = OptimizerFactory.forStringSet()
        val s1 = setOf("a", "b")
        val s2 = setOf("b", "a")
        val r1 = optimizer.optimize(s1)
        val r2 = optimizer.optimize(s2)
        assertEquals(s1, r1)
        assertEquals(s2, r2)
        // 小集合应被驻留，等价内容返回同一引用
        assertSame(r1, r2)
    }

    @Test
    fun testStringSetOptimizer_threshold_largeSet_returnsSelf() {
        val optimizer = OptimizerFactory.forStringSet()
        val input = (1..9).map { it.toString() }.toSet() // size = 9 > SMALL_INTERN_THRESHOLD(8)
        val result = optimizer.optimize(input)
        // 大集合不驻留，应直接返回自身
        assertSame(input, result)
        assertEquals(9, result.size)
    }

    // ========== Platform Access ==========
    @Test
    fun testAccessOptimizer_roundTrip_andValues() {
        val optimizer = OptimizerFactory.forReadWriteAccess()

        val read = ReadWriteAccess.Read
        val write = ReadWriteAccess.Write
        val rw = ReadWriteAccess.ReadWrite

        val br = optimizer.optimize(read)
        val bw = optimizer.optimize(write)
        val brw = optimizer.optimize(rw)

        assertEquals(0.toByte(), br)
        assertEquals(1.toByte(), bw)
        assertEquals(2.toByte(), brw)

        assertEquals(read, optimizer.deoptimize(br))
        assertEquals(write, optimizer.deoptimize(bw))
        assertEquals(rw, optimizer.deoptimize(brw))
    }

    @Test
    fun testAccessOptimizer_deoptimize_unknownByte_fallsBackToReadWrite() {
        val optimizer = OptimizerFactory.forReadWriteAccess()
        val result = optimizer.deoptimize(100.toByte())
        assertEquals(ReadWriteAccess.ReadWrite, result)
    }
}
