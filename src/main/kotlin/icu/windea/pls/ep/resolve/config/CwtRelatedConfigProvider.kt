package icu.windea.pls.ep.resolve.config

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.core.addExtensionPointListener
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.lang.codeInsight.navigation.GotoRelatedConfigsAction
import icu.windea.pls.lang.codeInsight.navigation.GotoRelatedConfigsHandler
import icu.windea.pls.model.ParadoxGameType

/**
 * 提供相关的规则。
 *
 * @see GotoRelatedConfigsAction
 * @see GotoRelatedConfigsHandler
 */
interface CwtRelatedConfigProvider {
    fun supports(gameType: ParadoxGameType): Boolean = true

    /**
     * 得到相关的规则列表。
     *
     * @param file 指定的 PSI 文件。
     * @param offset 指定的偏移。
     * @return 相关规则列表。
     */
    fun getRelatedConfigs(file: PsiFile, offset: Int): Collection<CwtConfig<*>>

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<CwtRelatedConfigProvider>("icu.windea.pls.relatedConfigProvider")
        @JvmField val CACHE = LazyValue<List<CwtRelatedConfigProvider>>()

        fun getAll(): List<CwtRelatedConfigProvider> = CACHE.get().orEmpty()

        // region Implementations

        init {
            CACHE.reinitialize { compute() }
            EP_NAME.addExtensionPointListener { CACHE.reinitialize { compute() } }
        }

        private fun compute(): List<CwtRelatedConfigProvider> {
            return EP_NAME.extensionList.optimized()
        }

        // endregion
    }
}
