package icu.windea.pls.ep.resolve.config

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.lang.resolve.CwtConfigContext
import icu.windea.pls.lang.resolve.CwtDeclarationConfigContext

/**
 * 提供声明规则的上下文。
 *
 * 说明：
 * - 通过 [CwtDeclarationConfigContext] 可以得到声明规则对应的处理后的顶级成员规则。
 * - 最终得到的顶级成员规则可以直接用于确定定义声明的结构，另外也会作为定义的顶级上下文规则。
 *
 * @see CwtConfigContext
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
