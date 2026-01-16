package icu.windea.pls.ep.resolve.config

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.lang.resolve.CwtDeclarationConfigContext

/**
 * 提供声明规则的上下文。
 *
 * 通过声明规则的上下文（[CwtDeclarationConfigContext]）可以得到最终使用的用于验证定义声明的规则。
 *
 * @see CwtDeclarationConfig
 * @see CwtDeclarationConfigContext
 */
@WithGameTypeEP
interface CwtDeclarationConfigContextProvider {
    fun getContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, configGroup: CwtConfigGroup): CwtDeclarationConfigContext?

    fun getCacheKey(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): String

    fun getConfig(context: CwtDeclarationConfigContext, declarationConfig: CwtDeclarationConfig): CwtPropertyConfig

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtDeclarationConfigContextProvider>("icu.windea.pls.declarationConfigContextProvider")
    }
}
