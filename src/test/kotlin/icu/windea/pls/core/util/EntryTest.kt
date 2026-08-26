package icu.windea.pls.core.util

import org.junit.Assert
import org.junit.Test

/**
 * @see Entry
 */
class EntryTest {
    @Test
    fun list_toMap_test() {
        val list = listOf(Entry("a", 1), Entry("b", 2))
        Assert.assertEquals(mapOf("a" to 1, "b" to 2), list.toMap())
        Assert.assertEquals(mutableMapOf("a" to 1, "b" to 2), list.toMutableMap())
    }

    @Test
    fun map_toEntryList_test() {
        val map = mapOf("a" to 1, "b" to 2)
        Assert.assertEquals(listOf(Entry("a", 1), Entry("b", 2)), map.toEntryList())
        Assert.assertEquals(mutableListOf(Entry("a", 1), Entry("b", 2)), map.toMutableEntryList())
    }
}
