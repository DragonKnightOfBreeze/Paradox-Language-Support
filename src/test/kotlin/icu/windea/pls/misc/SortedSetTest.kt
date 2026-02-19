package icu.windea.pls.misc

import org.junit.Assert
import org.junit.Test

class SortedSetTest {
    @Test
    fun test() {
        val result = mutableListOf<String>()

        var v = ""
        val comparator = Comparator<String> { o1, o2 ->
            if (o1 == o2) return@Comparator 0
            result += v
            o1.compareTo(o2)
        }
        val s = sortedSetOf(comparator)
        v = "a"
        s += v
        v = "b"
        s += v
        v = "c"
        s += v

        // no "a" output in result
        Assert.assertFalse(result.contains("a"))
    }
}
