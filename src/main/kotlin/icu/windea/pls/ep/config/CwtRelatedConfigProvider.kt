package icu.windea.pls.ep.config

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.navigation.*

/**
 * 用于基于上下文提供相关的CWT规则。
 *
 * @see GotoRelatedConfigsAction
 * @see GotoRelatedConfigsHandler
 */
@WithGameTypeEP
interface CwtRelatedConfigProvider {
    fun getRelatedConfigs(file: PsiFile, offset: Int): List<CwtConfig<*>>

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtRelatedConfigProvider>("icu.windea.pls.relatedConfigProvider")

        fun getRelatedConfigs(file: PsiFile, offset: Int): List<CwtConfig<*>> {
            val gameType = selectGameType(file) ?: return emptyList()
            val result = mutableListOf<CwtConfig<*>>()
            EP_NAME.extensionList.forEach f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f
                val r = ep.getRelatedConfigs(file, offset)
                result += r
            }
            return result
        }
    }
}
