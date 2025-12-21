package icu.windea.pls.ep.config

import icu.windea.pls.config.CwtTagType
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.config.tagType

class CwtBaseConfigPostProcessor : CwtConfigPostProcessor {
    override fun postProcess(config: CwtMemberConfig<*>): Boolean {
        applyTagOption(config)
        return true
    }

    private fun applyTagOption(config: CwtMemberConfig<*>) {
        // #123 mark tag type as predefined for config
        if (config is CwtValueConfig && config.optionData { flags }.tag) {
            config.tagType = CwtTagType.Predefined
        }
    }
}

class CwtInheritConfigPostProcessor : CwtConfigPostProcessor {
    override fun postProcess(config: CwtMemberConfig<*>): Boolean {
        applyInheritOption(config)
        return true
    }

    @Suppress("UNUSED_PARAMETER")
    private fun applyInheritOption(config: CwtMemberConfig<*>) {
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
}

class CwtInjectConfigPostProcessor: CwtConfigPostProcessor {
    override fun postProcess(config: CwtMemberConfig<*>): Boolean {
        applyInjectOption(config)
        return true
    }

    private fun applyInjectOption(config: CwtMemberConfig<*>) {
        // TODO 2.1.0
    }
}
