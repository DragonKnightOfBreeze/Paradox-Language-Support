package icu.windea.pls.ep.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.lang.psi.ParadoxExpressionElement

/**
 * @see icu.windea.pls.lang.inspections.script.common.IncorrectExpressionInspection
 */
@WithGameTypeEP
interface ParadoxIncorrectExpressionChecker {
    fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder)

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxIncorrectExpressionChecker>("icu.windea.pls.incorrectExpressionChecker")
    }
}
