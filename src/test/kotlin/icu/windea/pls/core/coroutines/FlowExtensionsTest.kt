package icu.windea.pls.core.coroutines

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FlowExtensionsTest {
    @Test
    fun chunked_basic_and_tail() = runBlocking {
        val result = (1..5).asFlow().chunked(2).toList()
        assertEquals(listOf(listOf(1, 2), listOf(3, 4), listOf(5)), result)
    }

    @Test
    fun chunked_invalid_size() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                (1..3).asFlow().chunked(0).toList()
            }
        }
    }

    @Test
    fun toLineFlow_basic_join() = runBlocking {
        val output = listOf("a", "b\nc", "d\n", "\n", "e").asFlow().toLineFlow().toList()
        assertEquals(listOf("ab", "cd", "", "e"), output)
    }

    @Test
    fun toLineFlow_trailing_without_newline() = runBlocking {
        val output = listOf("x", "y").asFlow().toLineFlow().toList()
        assertEquals(listOf("xy"), output)
    }
}
