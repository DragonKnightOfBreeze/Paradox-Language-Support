package icu.windea.pls.core.collections

import com.intellij.util.containers.TreeTraversal
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
    fun generateSequence_bfs() {
        val result = generateSequenceFromSeed(TreeTraversal.PLAIN_BFS, 1) { n -> children[n] ?: emptyList() }.toList()
        assertEquals(listOf(1, 2, 3, 4, 5, 6), result)
    }

    @Test
    fun generateSequence_dfs_preOrder() {
        val result = generateSequenceFromSeed(TreeTraversal.PRE_ORDER_DFS, 1) { n -> children[n] ?: emptyList() }.toList()
        // Preorder: root, then each subtree in order
        assertEquals(listOf(1, 2, 4, 5, 3, 6), result)
    }

    @Test
    fun generateSequence_bfs_null_seed_returns_empty() {
        val result = generateSequenceFromSeed<Int>(TreeTraversal.PLAIN_BFS, null) { emptyList() }.toList()
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun generateSequence_dfs_null_seed_returns_empty() {
        val result = generateSequenceFromSeed<Int>(TreeTraversal.PRE_ORDER_DFS, null) { emptyList() }.toList()
        assertEquals(emptyList<Int>(), result)
    }
}
