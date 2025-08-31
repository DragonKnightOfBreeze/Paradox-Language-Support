package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.copy
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.inlineConfig
import icu.windea.pls.config.config.singleAliasConfig
import icu.windea.pls.config.util.CwtConfigManipulator
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull

internal class CwtSingleAliasConfigResolverImpl : CwtSingleAliasConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig): CwtSingleAliasConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtSingleAliasConfig? {
        // 从 key 中提取单别名名：single_alias[<name>]
        val key = config.key
        val name = key.removeSurroundingOrNull("single_alias[", "]")?.orNull()?.intern() ?: return null
        return CwtSingleAliasConfigImpl(config, name)
    }
}

private class CwtSingleAliasConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String
) : UserDataHolderBase(), CwtSingleAliasConfig {
    override fun inline(config: CwtPropertyConfig): CwtPropertyConfig {
        // 内联：将 single_alias 对应的值与子配置深拷贝到调用处，并保持选项与父子关系不变
        val other = this.config
        val inlined = config.copy(
            value = other.value,
            valueType = other.valueType,
            configs = CwtConfigManipulator.deepCopyConfigs(other),
            optionConfigs = config.optionConfigs,
        )
        // 维护父子关系：
        inlined.parentConfig = config.parentConfig
        inlined.configs?.forEach { it.parentConfig = inlined }
        // 保留 inline/alias 标记，标识其来源，便于后续跳转/提示
        inlined.inlineConfig = config.inlineConfig
        inlined.aliasConfig = config.aliasConfig
        inlined.singleAliasConfig = this
        return inlined
    }

    override fun toString() = "CwtSingleAliasConfigImpl(name='$name')"
}
