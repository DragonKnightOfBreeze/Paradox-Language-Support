package icu.windea.pls.core

import org.junit.Assert.*
import org.junit.Test

class StdlibFastExtensionsTest {
    // region equalsFast

    @Test
    fun equalsFast_identity_test() {
        // 完全相同引用时走快速路径
        val s = "abc"
        assertTrue(s.equalsFast(s))
        assertTrue(s.equalsFast(s, ignoreCase = true))
    }

    @Test
    fun equalsFast_basic_test() {
        assertTrue(null.equalsFast(null))
        assertFalse(null.equalsFast(""))
        assertFalse("".equalsFast(null))
        assertTrue("".equalsFast(""))
        assertTrue("abc".equalsFast("abc"))
        assertTrue("abc".equalsFast(String("abc".toCharArray())))
        assertFalse("".equalsFast("abc"))
        assertFalse("".equalsFast(String("abc".toCharArray())))

        assertTrue("hello world".equalsFast("hello world"))
        assertTrue("hello world".equalsFast("Hello World", ignoreCase = true))
        assertFalse("hello world".equalsFast("hello world!"))
        assertFalse("hello world".equalsFast("Hello World!", ignoreCase = true))

        assertTrue("prompt: 你好，世界".equalsFast("prompt: 你好，世界"))
        assertTrue("prompt: 你好，世界".equalsFast("PROMPT: 你好，世界", ignoreCase = true))
        assertFalse("prompt: 你好，世界".equalsFast("prompt: 你好，世界！"))
        assertFalse("prompt: 你好，世界".equalsFast("PROMPT: 你好，世界！", ignoreCase = true))
    }

    @Test
    fun equalsFast_length_mismatch_test() {
        // 长度不同应快速返回 false
        assertFalse("abc".equalsFast("ab"))
        assertFalse("ab".equalsFast("abc"))
        assertFalse("abc".equalsFast("ABCd", ignoreCase = true))
    }

    @Test
    fun equalsFast_ignore_case_ascii_test() {
        assertTrue("abc".equalsFast("ABC", ignoreCase = true))
        assertTrue("AbC".equalsFast("aBc", ignoreCase = true))
        assertFalse("abc".equalsFast("ABd", ignoreCase = true))
        assertFalse("abc".equalsFast("ABC", ignoreCase = false))
        // 大写字母与小写字母之外的其他字符不受忽略大小写影响
        assertTrue("a-b_c".equalsFast("A-B_C", ignoreCase = true))
        assertFalse("a-b_c".equalsFast("a.b_c", ignoreCase = true))
    }

    @Test
    fun equalsFast_unicode_fallback_test() {
        // 非 ASCII 字符不同时退化为标准库比较
        assertTrue("你好".equalsFast("你好"))
        assertTrue("你好".equalsFast("你好", ignoreCase = true))
        assertTrue("é".equalsFast("É", ignoreCase = true))
        assertFalse("é".equalsFast("É", ignoreCase = false))
        assertFalse("你好".equalsFast("您好"))
        assertTrue("aé".equalsFast("Aé", ignoreCase = true))
        assertFalse("aéx".equalsFast("Aéy", ignoreCase = true))
    }

    @Test
    fun equalsFast_interface_consistency_test() {
        // 与标准库 String?.equals(other, ignoreCase) 在接口层面保持一致
        val strings = listOf(
            null, "", "a", "abc", "ABC", "AbC", "hello world", "Hello World!",
            "prompt: 你好，世界", "PROMPT: 你好，世界！", "é", "É", "aé", "éx", "长字符串",
        )
        for (a in strings) {
            for (b in strings) {
                for (ignoreCase in listOf(false, true)) {
                    val expected = a.equals(b, ignoreCase)
                    val actual = a.equalsFast(b, ignoreCase)
                    assertEquals("a=$a, b=$b, ignoreCase=$ignoreCase", expected, actual)
                }
            }
        }
    }

    // endregion

    // region equalsAnyFast

    @Test
    fun equalsAnyFast_basic_test() {
        val array = arrayOf("a", "bb", "ccc")
        assertTrue(array.equalsAnyFast("bb"))
        assertTrue(array.equalsAnyFast("BB", ignoreCase = true))
        assertFalse(array.equalsAnyFast("bbb"))
        assertFalse(array.equalsAnyFast("d"))
        assertTrue(array.equalsAnyFast("A", ignoreCase = true))
    }

    @Test
    fun equalsAnyFast_empty_array_test() {
        assertFalse(emptyArray<String>().equalsAnyFast("a"))
    }

    @Test
    fun equalsAnyFast_interface_consistency_test() {
        // 与标准库 any { it.equals(other, ignoreCase) } 保持一致
        val arrays = listOf(
            emptyArray<String>(),
            arrayOf("a"),
            arrayOf("a", "b", "c"),
            arrayOf("x", "y", "z", "a"),
        )
        val others = listOf("a", "b", "c", "A", "Z", "xyz", "", "abc")
        for (array in arrays) {
            for (other in others) {
                for (ignoreCase in listOf(false, true)) {
                    val expected = array.any { it.equals(other, ignoreCase) }
                    val actual = array.equalsAnyFast(other, ignoreCase)
                    assertEquals("array=${array.contentToString()}, other=$other, ignoreCase=$ignoreCase", expected, actual)
                }
            }
        }
    }

    // endregion

    // region trimFast

    @Test
    fun trimFast_basic_test() {
        assertEquals("abc", "///abc///".trimFast('/'))
        assertEquals("a/b/c", "/a/b/c/".trimFast('/'))
        assertEquals("abc", "abc".trimFast('/'))
        assertEquals("", "".trimFast('/'))
        assertEquals("", "////".trimFast('/'))
        assertEquals("a", "/a/".trimFast('/'))
    }

    @Test
    fun trimFast_char_not_found_test() {
        // 字符串中不包含指定字符时原样返回
        assertEquals("abc", "abc".trimFast('x'))
        assertEquals("", "".trimFast('x'))
    }

    @Test
    fun trimFast_all_chars_match_test() {
        // 所有字符都匹配时返回空字符串
        assertEquals("", "aaaa".trimFast('a'))
        assertEquals("", "a".trimFast('a'))
    }

    @Test
    fun trimFast_only_trim_specific_char_test() {
        // 仅去除指定的字符，不去除其他字符（包括空白）
        assertEquals(" /a/ ", " /a/ ".trimFast('/'))
        assertEquals("a/b", " a/b ".trimFast(' '))
        assertEquals("abc ", "abc ".trimFast('/'))
        assertEquals(" abc", " abc".trimFast('/'))
    }

    @Test
    fun trimFast_interface_consistency_test() {
        // 与标准库 trim { it == c } 保持一致
        val strings = listOf("", "/", "//", "///", "abc", "/a/b/", "a/b/", "/a/b", " /a/ ", "//a//b//", "a")
        for (s in strings) {
            for (c in listOf('/', ' ', 'a')) {
                assertEquals("s=$s, c=$c", s.trim { it == c }, s.trimFast(c))
            }
        }
    }

    // endregion

    // region splitFast

    @Test
    fun splitFast_basic_test() {
        assertEquals(listOf("a", "b", "c"), "a|b|c".splitFast('|'))
        assertEquals(listOf("abc"), "abc".splitFast('|'))
        assertEquals(listOf("", "b", ""), "|b|".splitFast('|'))
        assertEquals(listOf("", ""), "|".splitFast('|'))
        assertEquals(listOf(""), "".splitFast('|'))
        assertEquals(listOf("a", "", "b"), "a||b".splitFast('|'))
        assertEquals(listOf("a", ""), "a|".splitFast('|'))
        assertEquals(listOf("", "a"), "|a".splitFast('|'))
    }

    @Test
    fun splitFast_ignore_case_and_limit_test() {
        assertEquals(listOf("a", "c"), "aBc".splitFast('b', ignoreCase = true))
        assertEquals(listOf("a", "c"), "aBc".splitFast('B', ignoreCase = true))
        assertEquals(listOf("aB|c"), "aB|c".splitFast('|', limit = 1))
        assertEquals(listOf("a", "B|c"), "a|B|c".splitFast('|', limit = 2))
        assertEquals(listOf("a", "b", "c|d"), "a|b|c|d".splitFast('|', limit = 3))
        assertEquals(listOf("a", "b"), "a|b".splitFast('|', limit = 5))
    }

    @Test
    fun splitFast_interface_consistency_test() {
        // 与标准库 split(delimiter, ignoreCase, limit) 保持一致
        val strings = listOf("", "a", "abc", "a|b|c", "|b|", "a||b", "a|", "|", "a|b|c|d|e", "aBc", "aB|c")
        val delimiters = listOf('|', ',', 'b')
        val limits = listOf(0, 1, 2, 3, 10)
        for (s in strings) {
            for (d in delimiters) {
                for (ignoreCase in listOf(false, true)) {
                    for (limit in limits) {
                        val expected = s.split(d, ignoreCase = ignoreCase, limit = limit)
                        val actual = s.splitFast(d, ignoreCase = ignoreCase, limit = limit)
                        assertEquals("s=$s, d=$d, ignoreCase=$ignoreCase, limit=$limit", expected, actual)
                    }
                }
            }
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun splitFast_negative_limit_test() {
        "a|b".splitFast('|', limit = -1)
    }

    // endregion

    // region joinToStringFast

    @Test
    fun joinToStringFast_basic_test() {
        assertEquals("", emptyList<String>().joinToStringFast(", "))
        assertEquals("a", listOf("a").joinToStringFast(", "))
        assertEquals("a, b, c", listOf("a", "b", "c").joinToStringFast(", "))
        assertEquals("a;b", listOf("a", "b").joinToStringFast(";"))
    }

    @Test
    fun joinToStringFast_transform_test() {
        assertEquals("(a)-(b)", listOf("a", "b").joinToStringFast("-") { "($it)" })
        assertEquals("(a)", listOf("a").joinToStringFast("-") { "($it)" })
        // 元素为 null 时与标准库一致地渲染为 "null"
        assertEquals("null, a", listOf(null, "a").joinToStringFast(", "))
    }

    @Test
    fun joinToStringFast_interface_consistency_test() {
        // 与标准库 joinToString(separator, transform) 保持一致
        val lists = listOf(
            emptyList(),
            listOf(1),
            listOf(1, 2, 3),
            listOf(1, 2, 3, 4),
        )
        val separators = listOf(",", "; ", " => ")
        for (list in lists) {
            for (sep in separators) {
                assertEquals(list.joinToString(separator = sep), list.joinToStringFast(sep))
                assertEquals(list.joinToString(separator = sep, transform = { "($it)" }), list.joinToStringFast(sep) { "($it)" })
            }
        }
    }

    // endregion
}
