package icu.windea.pls.ep.config.config

import icu.windea.pls.config.CwtConfigApiStatus
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig

class CwtApiStatusBasedConfigFilterProvider : CwtConfigFilterProvider {
    // 按照 API 状态进行过滤
    // 目前仅限别名规则和单别名规则

    override fun filter(config: CwtConfig<*>): Boolean {
        if (config !is CwtSingleAliasConfig && config !is CwtAliasConfig) return false
        return config.config.optionData.apiStatus == CwtConfigApiStatus.Removed
    }
}

class CwtForcedConfigFilterProvider : CwtConfigFilterProvider {
    // 强制过滤掉某些不应被插件被视为定义的概念

    private val filteredNames = arrayOf(
        // defines -> filtered (neither define files, namespaces nor variables are definitions)
        "define", "defines",
        // inline defines -> filtered (inline script files are not definitions)
        "inline_script", "inline_scripts",
    )

    override fun filter(config: CwtConfig<*>): Boolean {
        return when (config) {
            is CwtTypeConfig -> config.name in filteredNames
            is CwtComplexEnumConfig -> config.name in filteredNames
            is CwtDeclarationConfig -> config.name in filteredNames
            else -> false
        }
    }
}
