package icu.windea.pls.core.coroutines

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class FlowExtensionsTest {
    @Test
    fun chunked_basicAndTail_test() = runBlocking {
        val result = (1..5).asFlow().chunked(2).toList()
        assertEquals(listOf(listOf(1, 2), listOf(3, 4), listOf(5)), result)
    }

    @Test
    fun chunked_empty_test() = runBlocking {
        assertEquals(emptyList<List<Int>>(), emptyList<Int>().asFlow().chunked(2).toList())
    }

    @Test
    fun chunked_exactMultiple_test() = runBlocking {
        assertEquals(listOf(listOf(1, 2), listOf(3, 4)), listOf(1, 2, 3, 4).asFlow().chunked(2).toList())
    }

    @Test
    fun chunked_invalidSize_test() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                (1..3).asFlow().chunked(0).toList()
            }
        }
    }

    @Test
    fun toLineFlow_basicJoin_test() = runBlocking {
        val output = listOf("a", "b\nc", "d\n", "\n", "e").asFlow().toLineFlow().toList()
        assertEquals(listOf("ab", "cd", "", "e"), output)
    }

    @Test
    fun toLineFlow_trailingWithoutNewline_test() = runBlocking {
        val output = listOf("x", "y").asFlow().toLineFlow().toList()
        assertEquals(listOf("xy"), output)
    }

    @Test
    fun toLineFlow_empty_test() = runBlocking {
        assertEquals(emptyList<String>(), emptyList<String>().asFlow().toLineFlow().toList())
    }

    @Test
    fun toLineFlow_emptyChunksSkipped_test() = runBlocking {
        assertEquals(listOf("ab"), listOf("a", "", "b").asFlow().toLineFlow().toList())
    }

    @Test
    fun toLineFlow_multipleLinesInOneChunk_test() = runBlocking {
        assertEquals(listOf("a", "b", "c"), listOf("a\nb\nc").asFlow().toLineFlow().toList())
    }

    @Test
    fun toLineFlow_multipleLinesInOneChunk_withStartLineBreak_test() = runBlocking {
        assertEquals(listOf("a", "b", "c"), listOf("a\nb\nc\n").asFlow().toLineFlow().toList())
    }

    @Test
    fun toLineFlow_multipleLinesInOneChunk_withEndLineBreak_test() = runBlocking {
        assertEquals(listOf("a", "b", "c"), listOf("a\nb\nc\n").asFlow().toLineFlow().toList())
    }
}
