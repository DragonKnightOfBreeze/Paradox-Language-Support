package icu.windea.pls.lang.resolve

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtExtendedGameRuleConfig
import icu.windea.pls.config.config.delegated.CwtExtendedOnActionConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue
import icu.windea.pls.ep.resolve.config.CwtDeclarationConfigContextProvider

/**
 * 声明规则上下文。
 *
 * 用于后续获取处理后的声明规则，从而确定符合条件的定义声明的结构。
 *
 * @see CwtDeclarationConfigContext
 */
class CwtDeclarationConfigContext(
    val definitionName: String?,
    val definitionType: String,
    val definitionSubtypes: List<String>?,
    val configGroup: CwtConfigGroup,
) : UserDataHolderBase() {
    lateinit var provider: CwtDeclarationConfigContextProvider

    /**
     * 得到按子类型列表合并后的声明规则。
     */
    fun getConfig(declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        return ParadoxConfigService.getConfigForDeclarationConfigContext(this, declarationConfig)
    }

    object Keys : KeyRegistry()
}

// Accessors

var CwtDeclarationConfigContext.gameRuleConfig: CwtExtendedGameRuleConfig? by registerKey(CwtDeclarationConfigContext.Keys)
var CwtDeclarationConfigContext.onActionConfig: CwtExtendedOnActionConfig? by registerKey(CwtDeclarationConfigContext.Keys)


