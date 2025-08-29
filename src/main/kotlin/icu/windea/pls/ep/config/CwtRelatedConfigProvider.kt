package icu.windea.pls.ep.config

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.core.annotations.WithGameTypeEP
import icu.windea.pls.lang.codeInsight.navigation.GotoRelatedConfigsAction
import icu.windea.pls.lang.codeInsight.navigation.GotoRelatedConfigsHandler
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.supportsByAnnotation

/**
 * 用于基于上下文提供相关的CWT规则。
 *
 * @see GotoRelatedConfigsAction
 * @see GotoRelatedConfigsHandler
 */
@WithGameTypeEP
interface CwtRelatedConfigProvider {
    fun getRelatedConfigs(file: PsiFile, offset: Int): Collection<CwtConfig<*>>

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtRelatedConfigProvider>("icu.windea.pls.relatedConfigProvider")

        fun getRelatedConfigs(file: PsiFile, offset: Int): Collection<CwtConfig<*>> {
            val gameType = selectGameType(file) ?: return emptySet()
            val result = mutableSetOf<CwtConfig<*>>()
            EP_NAME.extensionList.forEach f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f
                val r = ep.getRelatedConfigs(file, offset)
                result += r
            }
            return result
        }
    }
}
