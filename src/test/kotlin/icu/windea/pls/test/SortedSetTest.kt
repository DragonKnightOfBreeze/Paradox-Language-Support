package icu.windea.pls.test

import org.junit.Test

class SortedSetTest {
    @Test
    fun test() {
        var v = ""
        val comparator = Comparator<String>{ o1, o2 ->
            if(o1 == o2) return@Comparator 0
            println(v)
            o1.compareTo(o2)
        }
        val s = sortedSetOf(comparator)
        v = "a"
        s += v
        v = "b"
        s += v
        v = "c"
        s += v
        
        //no "a" output in result
    }
}