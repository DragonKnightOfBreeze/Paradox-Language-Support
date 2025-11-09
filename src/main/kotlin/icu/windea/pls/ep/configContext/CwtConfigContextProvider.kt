package icu.windea.pls.ep.configContext

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configContext.CwtConfigContext
import icu.windea.pls.config.configContext.provider
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.resolve.ParadoxScriptService
import icu.windea.pls.model.paths.ParadoxElementPath
import icu.windea.pls.script.psi.ParadoxScriptMember

/**
 * 用于提供脚本表达式所在的CWT规则上下文。
 *
 * @see CwtConfigContext
 */
@WithGameTypeEP
interface CwtConfigContextProvider {
    fun getContext(element: ParadoxScriptMember, elementPath: ParadoxElementPath, file: PsiFile): CwtConfigContext?

    fun getCacheKey(context: CwtConfigContext, matchOptions: Int = ParadoxMatchOptions.Default): String?

    fun getConfigs(context: CwtConfigContext, matchOptions: Int = ParadoxMatchOptions.Default): List<CwtMemberConfig<*>>?

    fun skipMissingExpressionCheck(context: CwtConfigContext) = false

    fun skipTooManyExpressionCheck(context: CwtConfigContext) = false

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtConfigContextProvider>("icu.windea.pls.configContextProvider")

        fun getContext(element: ParadoxScriptMember): CwtConfigContext? {
            val file = element.containingFile ?: return null
            val elementPath = ParadoxScriptService.getElementPath(element) ?: return null
            val gameType = selectGameType(file)
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!PlsAnnotationManager.check(ep, gameType)) return@f null
                ep.getContext(element, elementPath, file)?.also { it.provider = ep }
            }
        }
    }
}
