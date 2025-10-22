package icu.windea.pls.core.collections

import org.junit.Assert.*
import org.junit.Test

class SequenceBuildersTest {
    private val children: Map<Int, List<Int>> = mapOf(
        1 to listOf(2, 3),
        2 to listOf(4, 5),
        3 to listOf(6),
        4 to emptyList(),
        5 to emptyList(),
        6 to emptyList(),
    )

    @Test
    fun breadthFirstSequence_level_order() {
        val result = generateBreathFirstSequence(1) { n -> children[n] ?: emptyList() }.toList()
        assertEquals(listOf(1, 2, 3, 4, 5, 6), result)
    }

    @Test
    fun depthFirstSequence_preorder() {
        val result = generateDepthFirstSequence(1) { n -> children[n] ?: emptyList() }.toList()
        // Preorder: root, then each subtree in order
        assertEquals(listOf(1, 2, 4, 5, 3, 6), result)
    }

    @Test
    fun breadthFirstSequence_null_seed_returns_empty() {
        val result = generateBreathFirstSequence<Int>(null) { emptyList() }.toList()
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun depthFirstSequence_null_seed_returns_empty() {
        val result = generateDepthFirstSequence<Int>(null) { emptyList() }.toList()
        assertEquals(emptyList<Int>(), result)
    }
}
