package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

object ParadoxComplexExpressionManager {
    fun completeStellarisNameFormatExpression(context: ProcessingContext, result: CompletionResultSet) {
        val element = context.contextElement as? ParadoxScriptStringExpressionElement ?: return
        val config = context.config ?: return
        val formatName = config.configExpression?.value ?: return
        val defType = "${formatName}_name_parts_list"

        // caret position inside expression
        val caretInExpr = context.offsetInParent - context.expressionOffset
        if (caretInExpr < 0) return
        val exprText = element.value
        val caret = caretInExpr.coerceIn(0, exprText.length)

        fun lastUnclosedIndex(open: Char, close: Char, until: Int): Int {
            var depth = 0
            var lastOpen = -1
            var i = 0
            while (i < until) {
                when (exprText[i]) {
                    open -> { depth++; lastOpen = i }
                    close -> if (depth > 0) depth--
                }
                i++
            }
            return if (depth > 0) lastOpen else -1
        }

        // inside [...]
        run {
            val leftSq = lastUnclosedIndex('[', ']', caret)
            if (leftSq >= 0) {
                val innerStart = leftSq + 1
                val keywordToUse = exprText.substring(innerStart, caret)
                val bakKeyword = context.keyword
                val bakKeywordOffset = context.keywordOffset
                context.keyword = keywordToUse
                context.keywordOffset = innerStart
                val resultToUse = result.withPrefixMatcher(keywordToUse)
                ParadoxCompletionManager.completeCommandExpression(context, resultToUse)
                context.keyword = bakKeyword
                context.keywordOffset = bakKeywordOffset
                return
            }
        }

        // inside <...>
        run {
            val leftAngle = lastUnclosedIndex('<', '>', caret)
            if (leftAngle >= 0) {
                val innerStart = leftAngle + 1
                val keywordToUse = exprText.substring(innerStart, caret)
                val cfg = CwtValueConfig.resolve(emptyPointer(), config.configGroup, "<${defType}>")
                val bakConfig = context.config
                val bakKeyword = context.keyword
                val bakKeywordOffset = context.keywordOffset
                context.config = cfg
                context.keyword = keywordToUse
                context.keywordOffset = innerStart
                val resultToUse = result.withPrefixMatcher(keywordToUse)
                ParadoxCompletionManager.completeDefinition(context, resultToUse)
                context.keyword = bakKeyword
                context.keywordOffset = bakKeywordOffset
                context.config = bakConfig
                return
            }
        }

        // otherwise, treat as localisation name
        run {
            fun isLocChar(ch: Char): Boolean {
                return ch.isLetterOrDigit() || ch == '_' || ch == '-' || ch == '.' || ch == '\''
            }
            var start = caret
            while (start > 0 && isLocChar(exprText[start - 1])) start--
            val keywordToUse = exprText.substring(start, caret)
            val cfg = CwtValueConfig.resolve(emptyPointer(), config.configGroup, "localisation")
            val bakConfig = context.config
            val bakKeyword = context.keyword
            val bakKeywordOffset = context.keywordOffset
            context.config = cfg
            context.keyword = keywordToUse
            context.keywordOffset = start
            val resultToUse = result.withPrefixMatcher(keywordToUse)
            ParadoxCompletionManager.completeLocalisation(context, resultToUse)
            ParadoxCompletionManager.completeSyncedLocalisation(context, resultToUse)
            context.keyword = bakKeyword
            context.keywordOffset = bakKeywordOffset
            context.config = bakConfig
        }
    }
}
