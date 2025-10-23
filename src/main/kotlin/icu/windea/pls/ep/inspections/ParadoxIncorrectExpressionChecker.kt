package icu.windea.pls.ep.inspections

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.annotations.PlsAnnotationManager
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

        fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
            val gameType = config.configGroup.gameType
            EP_NAME.extensionList.forEach f@{ ep ->
                if (!PlsAnnotationManager.check(ep, gameType)) return@f
                ep.check(element, config, holder)
            }
        }
    }
}
