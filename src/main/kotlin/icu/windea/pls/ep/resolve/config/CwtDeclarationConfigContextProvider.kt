package icu.windea.pls.ep.resolve.config

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.base.annotations.WithGameTypeEP
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.CwtConfigContext
import icu.windea.pls.lang.resolve.CwtDeclarationConfigContext

/**
 * 提供声明规则的上下文。
 *
 * 说明：
 * - 通过 [CwtDeclarationConfigContext] 可以得到声明规则对应的最终的顶级成员规则，从而确定声明的结果。
 * - 在后续的语义解析流程中，这里得到的规则会作为顶级的上下文规则被进一步展开，涉及深拷贝、内联等操作。
 *
 * @see CwtConfigContext
 * @see CwtDeclarationConfigContext
 */
@WithGameTypeEP
interface CwtDeclarationConfigContextProvider {
    fun getContext(element: PsiElement, configGroup: CwtConfigGroup, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?): CwtDeclarationConfigContext?

    fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String

    fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<CwtDeclarationConfigContextProvider>("icu.windea.pls.declarationConfigContextProvider")
    }
}
