package icu.windea.pls.ep.resolve.config

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.lang.codeInsight.navigation.GotoRelatedConfigsAction
import icu.windea.pls.lang.codeInsight.navigation.GotoRelatedConfigsHandler

/**
 * 提供相关的规则。
 *
 * @see GotoRelatedConfigsAction
 * @see GotoRelatedConfigsHandler
 */
@WithGameTypeEP
interface CwtRelatedConfigProvider {
    /**
     * 得到相关的规则列表。
     *
     * @param file 指定的 PSI 文件。
     * @param offset 指定的偏移。
     * @return 相关规则列表。
     */
    fun getRelatedConfigs(file: PsiFile, offset: Int): Collection<CwtConfig<*>>

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtRelatedConfigProvider>("icu.windea.pls.relatedConfigProvider")
    }
}
