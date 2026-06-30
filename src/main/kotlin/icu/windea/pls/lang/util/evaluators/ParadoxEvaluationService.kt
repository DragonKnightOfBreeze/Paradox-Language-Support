package icu.windea.pls.lang.util.evaluators

import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommandText
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

object ParadoxEvaluationService {
    fun isEvaluableForInlineMath(element: ParadoxScriptInlineMath): Boolean {
        if (!element.isValid) return false
        if (element.inlineMathExpression == null) return false
        return true
    }

    fun isEvaluableForDefineReference(element: ParadoxExpressionElement): Boolean {
        if (element !is ParadoxScriptStringExpressionElement && element !is ParadoxLocalisationCommandText) return false
        if (!element.isValid) return false
        val value = element.value
        if (value.isEmpty()) return false
        if (value.count { it == '|' } < 1) return false // fast return
        return true
    }

    fun isEvaluableForArrayDefineReference(element: ParadoxExpressionElement): Boolean {
        if (element !is ParadoxScriptStringExpressionElement && element !is ParadoxLocalisationCommandText) return false
        if (!element.isValid) return false
        val value = element.value
        if (value.isEmpty()) return false
        if (value.count { it == '|' } < 2) return false // fast return
        return true
    }
}
