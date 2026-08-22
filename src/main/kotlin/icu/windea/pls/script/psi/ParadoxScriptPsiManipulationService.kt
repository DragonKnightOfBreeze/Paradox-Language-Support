package icu.windea.pls.script.psi

import icu.windea.pls.core.quote

object ParadoxScriptPsiManipulationService {
    private const val FORCE_QUOTED_CHARS = "@#=<>!?{}[\""

    fun needQuote(expression: String): Boolean {
        return expression.any { it.isWhitespace() || it in FORCE_QUOTED_CHARS }
    }

    fun quoteIfNeeded(expression: String): String {
        return if (needQuote(expression)) expression.quote() else expression
    }
}
