package icu.windea.pls.lang.resolve

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.extended.CwtExtendedGameRuleConfig
import icu.windea.pls.config.config.extended.CwtExtendedOnActionConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue
import icu.windea.pls.ep.resolve.config.CwtDeclarationConfigContextProvider
import icu.windea.pls.model.ParadoxGameType

/**
 * 声明规则上下文。
 *
 * 用于后续获取声明规则对应的最终的顶级成员规则，确定声明的结构，从而提供各种高级语言功能。
 *
 * @property provider 上下文提供者。
 *
 * @see CwtDeclarationConfigContextProvider
 * @see CwtDeclarationConfig
 */
interface CwtDeclarationConfigContext : UserDataHolder {
    val configGroup: CwtConfigGroup
    val definitionName: String?
    val definitionType: String
    val definitionSubtypes: List<String>?

    val provider: CwtDeclarationConfigContextProvider

    val project: Project get() = configGroup.project
    val gameType: ParadoxGameType get() = configGroup.gameType

    /** 得到最终的顶级成员规则。 */
    fun getConfig(declarationConfig: CwtDeclarationConfig): CwtPropertyConfig

    object Keys : KeyRegistry()

    companion object {
        @JvmStatic
        fun create(
            configGroup: CwtConfigGroup,
            definitionType: String,
            definitionSubtypes: List<String>?,
            provider: CwtDeclarationConfigContextProvider,
        ): CwtDeclarationConfigContext {
            return CwtBaseDeclarationConfigContext(configGroup, definitionType, definitionSubtypes, provider)
        }

        @JvmStatic
        fun createNamed(
            configGroup: CwtConfigGroup,
            definitionName: String,
            definitionType: String,
            definitionSubtypes: List<String>?,
            provider: CwtDeclarationConfigContextProvider,
        ): CwtDeclarationConfigContext {
            return CwtNamedDeclarationConfigContext(configGroup, definitionName, definitionType, definitionSubtypes, provider)
        }
    }
}

// region Accessors

var CwtDeclarationConfigContext.gameRuleConfig: CwtExtendedGameRuleConfig? by registerKey(CwtDeclarationConfigContext.Keys)
var CwtDeclarationConfigContext.onActionConfig: CwtExtendedOnActionConfig? by registerKey(CwtDeclarationConfigContext.Keys)

// endregion

// region Implementations

// 12 + 5 * 4 = 32 -> 32
private sealed class CwtDeclarationConfigContextBase(
    override val configGroup: CwtConfigGroup,
    override val definitionType: String,
    override val definitionSubtypes: List<String>?,
    override val provider: CwtDeclarationConfigContextProvider,
) : UserDataHolderBase(), CwtDeclarationConfigContext {
    override fun getConfig(declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        return ParadoxConfigService.getConfigForDeclarationConfigContext(this, declarationConfig)
    }

    override fun toString(): String {
        return "CwtDeclarationConfigContext(configGroup=$configGroup" +
            ", definitionName=$definitionName" +
            ", definitionType=$definitionType" +
            ", definitionSubtypes=$definitionSubtypes" +
            ", provider=$provider" +
            ")"
    }
}

// 12 + 5 * 4 = 32 -> 32
private class CwtBaseDeclarationConfigContext(
    configGroup: CwtConfigGroup,
    definitionType: String,
    definitionSubtypes: List<String>?,
    provider: CwtDeclarationConfigContextProvider,
) : CwtDeclarationConfigContextBase(configGroup, definitionType, definitionSubtypes, provider) {
    override val definitionName: String? get() = null
}

// 12 + 6 * 4 = 36 -> 40
private class CwtNamedDeclarationConfigContext(
    configGroup: CwtConfigGroup,
    definitionName: String,
    definitionType: String,
    definitionSubtypes: List<String>?,
    provider: CwtDeclarationConfigContextProvider,
) : CwtDeclarationConfigContextBase(configGroup, definitionType, definitionSubtypes, provider) {
    override val definitionName: String = definitionName
}

// endregion
