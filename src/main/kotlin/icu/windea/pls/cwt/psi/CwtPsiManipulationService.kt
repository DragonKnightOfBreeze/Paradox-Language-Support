package icu.windea.pls.cwt.psi

import icu.windea.pls.core.isQuoted
import icu.windea.pls.core.quote

object CwtPsiManipulationService {
    private const val FORCE_QUOTED_CHARS = "#={}\""

    fun needQuote(expression: String): Boolean {
        return expression.any { it.isWhitespace() || it in FORCE_QUOTED_CHARS }
    }

    fun quoteIfNeeded(expression: String): String {
        return if (!expression.isQuoted() && needQuote(expression)) expression.quote() else expression
    }
}
