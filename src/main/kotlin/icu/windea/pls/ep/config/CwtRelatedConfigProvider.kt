package icu.windea.pls.ep.config

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.lang.codeInsight.navigation.GotoRelatedConfigsAction
import icu.windea.pls.lang.codeInsight.navigation.GotoRelatedConfigsHandler
import icu.windea.pls.lang.selectGameType

/**
 * 用于提供相关的规则。
 *
 * @see GotoRelatedConfigsAction
 * @see GotoRelatedConfigsHandler
 */
@WithGameTypeEP
interface CwtRelatedConfigProvider {
    /**
     * 得到相关规则列表。
     *
     * @param file 指定的 PSI 文件。
     * @param offset 指定的偏移。
     * @return 相关规则列表。
     */
    fun getRelatedConfigs(file: PsiFile, offset: Int): Collection<CwtConfig<*>>

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtRelatedConfigProvider>("icu.windea.pls.relatedConfigProvider")

        fun getRelatedConfigs(file: PsiFile, offset: Int): Collection<CwtConfig<*>> {
            val gameType = selectGameType(file) ?: return emptySet()
            val result = mutableSetOf<CwtConfig<*>>()
            EP_NAME.extensionList.forEach f@{ ep ->
                if (!PlsAnnotationManager.check(ep, gameType)) return@f
                val r = ep.getRelatedConfigs(file, offset)
                result += r
            }
            return result
        }
    }
}
