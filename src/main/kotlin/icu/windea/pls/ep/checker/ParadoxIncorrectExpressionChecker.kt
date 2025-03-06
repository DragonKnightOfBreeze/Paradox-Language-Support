package icu.windea.pls.ep.checker

import com.intellij.codeInspection.*
import com.intellij.openapi.extensions.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * @see icu.windea.pls.lang.inspections.script.common.IncorrectExpressionInspection
 */
@WithGameTypeEP
interface ParadoxIncorrectExpressionChecker {
    fun check(element: ParadoxScriptExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder)

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxIncorrectExpressionChecker>("icu.windea.pls.incorrectExpressionChecker")

        fun check(element: ParadoxScriptExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
            val gameType = config.configGroup.gameType ?: return
            EP_NAME.extensionList.forEach f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f
                ep.check(element, config, holder)
            }
        }
    }
}
