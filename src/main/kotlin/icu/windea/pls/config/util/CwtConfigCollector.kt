package icu.windea.pls.config.util

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.CwtTagType
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.optionFlags
import icu.windea.pls.config.config.tagType
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.filePathExpressions
import icu.windea.pls.config.configGroup.parameterConfigs
import icu.windea.pls.core.pass

object CwtConfigCollector {
    fun postHandleConfig(config: CwtMemberConfig<*>) {
        applyInheritOptions(config)
        applyTagOption(config)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun applyInheritOptions(config: CwtMemberConfig<*>) {
        // TODO 1.3.18+
        // val configGroup = config.configGroup
        //
        // var inheritConfigsValue: String? = null
        // var inheritOptionsValue: String? = null
        // var inheritDocValue: String? = null
        //
        // val oldOptions = mutableListOf<CwtOptionMemberConfig<*>>()
        // config.options?.forEach { o ->
        //    when(o){
        //        is CwtOptionConfig -> when(o.key) {
        //            "inherit_configs" -> o.stringValue?.let { inheritConfigsValue = it }
        //            "inherit_options" -> o.stringValue?.let { inheritOptionsValue = it }
        //            "inherit_doc" -> o.stringValue?.let { inheritDocValue = it }
        //            else -> oldOptions += o
        //        }
        //        is CwtOptionValueConfig -> oldOptions += o
        //    }
        // }
        //
        // if(inheritConfigsValue == null && inheritOptionsValue == null && inheritDocValue == null) return config
        //
        // var newConfigs: List<CwtMemberConfig<*>>? = null
        // var newOptions: List<CwtOptionMemberConfig<*>>? = null
        // var newDocumentation: String? = null
        //
        // inheritDocValue?.let { pathExpression ->
        //    CwtConfigManager.getConfigByPathExpression(configGroup, pathExpression)?.let { newConfig ->
        //        newDocumentation = newConfig.documentation
        //    }
        // }
        //
        // if(newConfigs == null && newOptions == null && newDocumentation == null) return config
    }

    private fun applyTagOption(config: CwtMemberConfig<*>) {
        // #123 mark tag type as predefined for config
        if (config is CwtValueConfig && config.optionFlags.tag) {
            config.tagType = CwtTagType.Predefined
        }
    }

    fun processConfigWithConfigExpression(config: CwtConfig<*>, configExpression: CwtDataExpression) {
        val configGroup = config.configGroup
        when (configExpression.type) {
            CwtDataTypes.FilePath -> {
                configExpression.value?.let { configGroup.filePathExpressions.add(configExpression) }
            }
            CwtDataTypes.Icon -> {
                configExpression.value?.let { configGroup.filePathExpressions.add(configExpression) }
            }
            CwtDataTypes.Parameter -> {
                if (config is CwtPropertyConfig) {
                    configGroup.parameterConfigs.add(config)
                }
            }
            else -> pass()
        }
    }
}
