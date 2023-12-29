package icu.windea.pls.core

import com.intellij.openapi.util.*
import org.junit.*

class PlatformExtensionsTest {
    @Test
    fun getTextFragmentsTest() {
        Assert.assertEquals(listOf(TextRange.create(0, 3) to "abc"), "abc".getTextFragments(0))
        Assert.assertEquals(listOf(TextRange.create(2, 5) to "abc"), "abc".getTextFragments(2))
        
        Assert.assertEquals(listOf(TextRange.create(0, 3) to "abc", TextRange.create(4, 8) to "\"def"), "abc\\\"def".getTextFragments(0))
        Assert.assertEquals(listOf(TextRange.create(2, 5) to "abc", TextRange.create(6, 10) to "\"def"), "abc\\\"def".getTextFragments(2))
        
        Assert.assertEquals(listOf(TextRange.create(0, 3) to "abc", TextRange.create(4, 5) to "\\", TextRange.create(6, 10) to "\"def"), "abc\\\\\\\"def".getTextFragments(0))
        Assert.assertEquals(listOf(TextRange.create(2, 5) to "abc", TextRange.create(6, 7) to "\\", TextRange.create(8, 12) to "\"def"), "abc\\\\\\\"def".getTextFragments(2))
    }
    
    @Test
    fun replaceAndQuoteIfNecessaryTest() {
        Assert.assertEquals("def", TextRange.create(0, 3).replaceAndQuoteIfNecessary("abc", "def"))
        Assert.assertEquals("\"e\"", TextRange.create(0, 3).replaceAndQuoteIfNecessary("\"b\"", "\"e\""))
        Assert.assertEquals("\"dec\"", TextRange.create(1, 3).replaceAndQuoteIfNecessary("\"abc\"", "de"))
        Assert.assertEquals("\"d\\\"c\"", TextRange.create(1, 3).replaceAndQuoteIfNecessary("\"abc\"", "d\""))
    }
}