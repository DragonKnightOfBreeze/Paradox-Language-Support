package icu.windea.pls.core.optimizer

import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test

@Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
class OptimizerRelaxTest {
    companion object {
        private const val propName = "pls.relax.optimize"
        private var oldProp: String? = null

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            oldProp = System.getProperty(propName)
            System.setProperty(propName, "true")
        }

        @JvmStatic
        @AfterClass
        fun afterClass() {
            val oldProp = oldProp
            if (oldProp == null) System.clearProperty(propName) else System.setProperty(propName, oldProp)
        }
    }

    // ========== List ==========
    @Test
    fun testList_kotlin_empty_returnsSelf() {
        val optimizer = OptimizerRegistry.forList<String>()
        val input = emptyList<String>() // kotlin.collections.EmptyList
        val result = optimizer.optimize(input)
        assertSame(input, result)
    }

    @Test
    fun testList_kotlin_singleton_returnsSelf() {
        val optimizer = OptimizerRegistry.forList<String>()
        val input = listOf("a") // kotlin.collections.SingletonList
        val result = optimizer.optimize(input)
        assertSame(input, result)
    }

    @Test
    fun testList_jdk_immutable_returnsSelf() {
        val optimizer = OptimizerRegistry.forList<String>()
        val input = java.util.List.of("a", "b") // java.util.ImmutableCollections$ListN
        val result = optimizer.optimize(input)
        assertSame(input, result)
    }

    // ========== Set ==========
    @Test
    fun testSet_kotlin_empty_returnsSelf() {
        val optimizer = OptimizerRegistry.forSet<String>()
        val input = emptySet<String>() // kotlin.collections.EmptySet
        val result = optimizer.optimize(input)
        assertSame(input, result)
    }

    @Test
    fun testSet_kotlin_singleton_returnsSelf() {
        val optimizer = OptimizerRegistry.forSet<String>()
        val input = setOf("a") // kotlin.collections.SingletonSet
        val result = optimizer.optimize(input)
        assertSame(input, result)
    }

    @Test
    fun testSet_jdk_immutable_returnsSelf() {
        val optimizer = OptimizerRegistry.forSet<String>()
        val input = java.util.Set.of("a", "b") // java.util.ImmutableCollections$SetN
        val result = optimizer.optimize(input)
        assertSame(input, result)
    }

    // ========== Map ==========
    @Test
    fun testMap_kotlin_empty_returnsSelf() {
        val optimizer = OptimizerRegistry.forMap<String, Int>()
        val input = emptyMap<String, Int>() // kotlin.collections.EmptyMap
        val result = optimizer.optimize(input)
        assertSame(input, result)
    }

    @Test
    fun testMap_kotlin_singleton_returnsSelf() {
        val optimizer = OptimizerRegistry.forMap<String, Int>()
        val input = mapOf("a" to 1) // kotlin.collections.SingletonMap
        val result = optimizer.optimize(input)
        assertSame(input, result)
    }

    @Test
    fun testMap_jdk_immutable_returnsSelf() {
        val optimizer = OptimizerRegistry.forMap<String, Int>()
        val input = java.util.Map.of("a", 1) // java.util.ImmutableCollections$Map1
        val result = optimizer.optimize(input)
        assertSame(input, result)
    }
}
