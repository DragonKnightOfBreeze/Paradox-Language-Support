package icu.windea.pls.core

import org.junit.Assert
import org.junit.Test

class ComparisonExtensionsTest {
    @Test
    fun thenPossible_basic() {
        val byLength = Comparator<String> { a, b -> a.length - b.length }
        val byLex = Comparator<String> { a, b -> a.compareTo(b) }

        // null handling
        Assert.assertSame(byLex, (null as Comparator<String>?).thenPossible(byLex))
        Assert.assertSame(byLength, byLength.thenPossible(null))

        // chaining
        val chained = (byLength.thenPossible(byLex))!!
        val list = listOf("b", "aa", "a", "ab")
        val sorted = list.sortedWith(chained)
        Assert.assertEquals(listOf("a", "b", "aa", "ab"), sorted)
    }

    @Test
    fun complexCompareBy_pinned_and_nulls() {
        val list = listOf(3, null, 2, 42, 5)
        val comp = complexCompareBy<Int?, Int, Int>(
            selector = { it },
            comparableSelector = { it },
            pinPredicate = { it == 42 }
        )
        val sorted = list.sortedWith(comp)
        Assert.assertEquals(listOf(42, 2, 3, 5, null), sorted)
    }

    @Test
    fun complexCompareByDescending_pinned_and_order() {
        val list = listOf(3, null, 2, 42, 5)
        val comp = complexCompareByDescending<Int?, Int, Int>(
            selector = { it },
            comparableSelector = { it },
            pinPredicate = { it == 42 }
        )
        val sorted = list.sortedWith(comp)
        // pinned first
        Assert.assertEquals(42, sorted.first())
        // null last
        Assert.assertNull(sorted.last())
        // remaining elements should be in descending order
        val remaining = sorted.filterNotNull().filter { it != 42 }
        Assert.assertEquals(listOf(5, 3, 2), remaining)
    }
}
