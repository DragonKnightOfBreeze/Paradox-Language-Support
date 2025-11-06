package icu.windea.pls.core.optimizer

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import org.junit.Assert.*
import org.junit.Test

class OptimizerTest {
    // ========== String ==========
    @Test
    fun testStringOptimizer_equivalentInstancesInterned() {
        val optimizer = OptimizerRegistry.forString()
        val s1 = String(charArrayOf('a', 'b', 'c'))
        val s2 = String(charArrayOf('a', 'b', 'c'))
        val r1 = optimizer.optimize(s1)
        val r2 = optimizer.optimize(s2)
        assertSame("Equivalent strings should be interned to the same reference", r1, r2)
        assertEquals("abc", r1)
    }

    @Test
    fun testStringOptimizer_idempotent() {
        val optimizer = OptimizerRegistry.forString()
        val s = String(charArrayOf('x'))
        val r1 = optimizer.optimize(s)
        val r2 = optimizer.optimize(s)
        assertSame("Optimize should be idempotent for the same content", r1, r2)
        assertEquals("x", r1)
    }

    @Test
    fun testStringOptimizer_emptyString_shortCircuit() {
        val optimizer = OptimizerRegistry.forString()
        val r = optimizer.optimize("")
        // 应直接返回标准空串单例
        assertSame("", r)
        assertEquals(0, r.length)
    }

    // ========== List ==========
    @Test
    fun testListOptimizer_emptyLists_valueEquality_only() {
        val optimizer = OptimizerRegistry.forList<String?>()
        val input1 = emptyList<String?>()
        val input2 = mutableListOf<String?>()
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
        val optimizer = OptimizerRegistry.forList<String>()
        val input = mutableListOf("a")
        val result = optimizer.optimize(input)
        assertEquals(listOf("a"), result)
    }

    @Test
    fun testListOptimizer_multiElements_copiesToImmutable_valueEqual() {
        val optimizer = OptimizerRegistry.forList<String>()
        val input = arrayListOf("a", "b")
        val result = optimizer.optimize(input)
        assertEquals(listOf("a", "b"), result)
        assertTrue("Should convert to Guava ImmutableList", result is ImmutableList<*>)
    }

    @Test
    fun testListOptimizer_ignore_immutable_returnsSelf() {
        val optimizer = OptimizerRegistry.forList<Int>()
        val input = ImmutableList.of(1, 2, 3)
        val result = optimizer.optimize(input)
        assertSame("ImmutableList input should be returned as-is", input, result)
    }

    @Test
    fun testListOptimizer_nullElement_singleton_allowed() {
        val optimizer = OptimizerRegistry.forList<String?>()
        val input: List<String?> = listOf(null)
        val result = optimizer.optimize(input)
        assertEquals(1, result.size)
        assertNull(result[0])
    }

    @Test
    fun testListOptimizer_nullElement_multiple_throwsNpe() {
        val optimizer = OptimizerRegistry.forList<String?>()
        val input: List<String?> = listOf(null, "x")
        assertThrows(NullPointerException::class.java) { optimizer.optimize(input) }
    }

    // ========== Set ==========
    @Test
    fun testSetOptimizer_emptySets_valueEquality_only() {
        val optimizer = OptimizerRegistry.forSet<String>()
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
        val optimizer = OptimizerRegistry.forSet<String>()
        val input = linkedSetOf("a", "b")
        val result = optimizer.optimize(input)
        assertEquals(setOf("a", "b"), result)
        assertTrue("Should convert to Guava ImmutableSet", result is ImmutableSet<*>)
    }

    @Test
    fun testSetOptimizer_ignore_immutable_returnsSelf() {
        val optimizer = OptimizerRegistry.forSet<Int>()
        val input = ImmutableSet.of(1, 2, 3)
        val result = optimizer.optimize(input)
        assertSame("ImmutableSet input should be returned as-is", input, result)
    }

    @Test
    fun testSetOptimizer_nullElement_throwsNpe() {
        val optimizer = OptimizerRegistry.forSet<String?>()
        val input: Set<String?> = setOf(null)
        assertThrows(NullPointerException::class.java) { optimizer.optimize(input) }
    }

    // ========== Map ==========
    @Test
    fun testMapOptimizer_emptyMaps_valueEquality_only() {
        val optimizer = OptimizerRegistry.forMap<String, Int>()
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
        val optimizer = OptimizerRegistry.forMap<String, Int>()
        val input = hashMapOf("a" to 1)
        val result = optimizer.optimize(input)
        assertEquals(mapOf("a" to 1), result)
        assertTrue("Should convert to Guava ImmutableMap", result is ImmutableMap<*, *>)
    }

    @Test
    fun testMapOptimizer_ignore_immutable_returnsSelf() {
        val optimizer = OptimizerRegistry.forMap<String, Int>()
        val input = ImmutableMap.of("x", 2)
        val result = optimizer.optimize(input)
        assertSame("ImmutableMap input should be returned as-is", input, result)
    }

    @Test
    fun testMapOptimizer_nullKey_throwsNpe() {
        val optimizer = OptimizerRegistry.forMap<Any?, Any?>()
        val input = hashMapOf<Any?, Any?>(null to 1)
        assertThrows(NullPointerException::class.java) { optimizer.optimize(input) }
    }

    @Test
    fun testMapOptimizer_nullValue_throwsNpe() {
        val optimizer = OptimizerRegistry.forMap<String, Any?>()
        val input = hashMapOf("a" to null)
        assertThrows(NullPointerException::class.java) { optimizer.optimize(input) }
    }

    // ========== String List (small-size interning) ==========
    @Test
    fun testStringListOptimizer_smallLists_interned_referenceEqual() {
        val optimizer = OptimizerRegistry.forStringList()
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
        val optimizer = OptimizerRegistry.forStringList()
        val input = (1..9).map { it.toString() } // size = 9 > SMALL_INTERN_THRESHOLD(8)
        val result = optimizer.optimize(input)
        // 大集合不驻留，应直接返回自身
        assertSame(input, result)
        assertEquals(9, result.size)
    }

    // ========== String Set (small-size interning) ==========
    @Test
    fun testStringSetOptimizer_smallSets_interned_referenceEqual() {
        val optimizer = OptimizerRegistry.forStringSet()
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
        val optimizer = OptimizerRegistry.forStringSet()
        val input = (1..9).map { it.toString() }.toSet() // size = 9 > SMALL_INTERN_THRESHOLD(8)
        val result = optimizer.optimize(input)
        // 大集合不驻留，应直接返回自身
        assertSame(input, result)
        assertEquals(9, result.size)
    }

    // ========== Platform Access ==========
    @Test
    fun testAccessOptimizer_roundTrip_andValues() {
        val optimizer = OptimizerRegistry.forAccess()

        val read = ReadWriteAccessDetector.Access.Read
        val write = ReadWriteAccessDetector.Access.Write
        val rw = ReadWriteAccessDetector.Access.ReadWrite

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
        val optimizer = OptimizerRegistry.forAccess()
        val result = optimizer.deoptimize(100.toByte())
        assertEquals(ReadWriteAccessDetector.Access.ReadWrite, result)
    }
}
