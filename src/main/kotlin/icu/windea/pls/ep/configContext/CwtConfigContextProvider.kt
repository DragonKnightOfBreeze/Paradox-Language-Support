package icu.windea.pls.ep.configContext

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configContext.CwtConfigContext
import icu.windea.pls.config.configContext.provider
import icu.windea.pls.core.annotations.WithGameTypeEP
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.supportsByAnnotation
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Options
import icu.windea.pls.lang.util.ParadoxExpressionPathManager
import icu.windea.pls.model.paths.ParadoxExpressionPath
import icu.windea.pls.script.psi.ParadoxScriptMemberElement

/**
 * 用于提供脚本表达式所在的CWT规则上下文。
 *
 * @see CwtConfigContext
 */
@WithGameTypeEP
interface CwtConfigContextProvider {
    fun getContext(element: ParadoxScriptMemberElement, elementPath: ParadoxExpressionPath, file: PsiFile): CwtConfigContext?

    fun getCacheKey(context: CwtConfigContext, matchOptions: Int = Options.Default): String?

    fun getConfigs(context: CwtConfigContext, matchOptions: Int = Options.Default): List<CwtMemberConfig<*>>?

    fun skipMissingExpressionCheck(context: CwtConfigContext) = false

    fun skipTooManyExpressionCheck(context: CwtConfigContext) = false

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtConfigContextProvider>("icu.windea.pls.configContextProvider")

        fun getContext(element: ParadoxScriptMemberElement): CwtConfigContext? {
            val file = element.containingFile ?: return null
            val elementPath = ParadoxExpressionPathManager.get(element) ?: return null
            val gameType = selectGameType(file)
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                ep.getContext(element, elementPath, file)
                    ?.also { it.provider = ep }
            }
        }
    }
}
