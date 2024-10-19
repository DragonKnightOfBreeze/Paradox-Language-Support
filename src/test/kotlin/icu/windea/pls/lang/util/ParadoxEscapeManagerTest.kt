package icu.windea.pls.lang.util

import com.intellij.codeInsight.*
import org.junit.*

class ParadoxEscapeManagerTest {
    @Test
    fun parseScriptExpressionCharacters() {
        val s = """###\"\\\\\"
\"custom_tooltip\" = {}"""
        val out = StringBuilder()
        val sourceOffsets = IntArray(s.length + 1)
        ParadoxEscapeManager.parseScriptExpressionCharacters(s, out, sourceOffsets)
        println(out)
        println(sourceOffsets.contentToString())

        val out1 = StringBuilder()
        val sourceOffsets1 = IntArray(s.length + 1)
        CodeInsightUtilCore.parseStringCharacters(s, out1, sourceOffsets1)
        println(out1)
        println(sourceOffsets1.contentToString())
    }
}
