package icu.windea.pls.ep.config

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.config.CwtTagType
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.config.tagType
import icu.windea.pls.config.util.CwtConfigResolverManager

class CwtBaseConfigPostProcessor : CwtConfigPostProcessor {
    override fun postProcess(config: CwtMemberConfig<*>) {
        // #123 mark tag type as predefined for config
        applyTagOption(config)
    }

    private fun applyTagOption(config: CwtMemberConfig<*>) {
        if (config is CwtValueConfig && config.optionData { flags }.tag) {
            config.tagType = CwtTagType.Predefined
        }
    }
}

class CwtInheritConfigPostProcessor : CwtConfigPostProcessor {
    override fun postProcess(config: CwtMemberConfig<*>) {
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

class CwtInjectConfigPostProcessor : CwtConfigPostProcessor {
    override fun supports(config: CwtMemberConfig<*>): Boolean {
        val pathExpression = config.optionData { inject } ?: return false
        val pathList = pathExpression.split('@', limit = 2)
        if (pathList.size != 2) {
            invalidPathExpression(pathExpression, config)
            return false
        }
        return true
    }

    override fun deferred(config: CwtMemberConfig<*>): Boolean {
        return true
    }

    override fun postProcess(config: CwtMemberConfig<*>) {
        val pathExpression = config.optionData { inject } ?: return
        val configsToInject = CwtConfigResolverManager.findConfigsByPathExpression(config.configGroup, pathExpression)
        if (configsToInject == null) {
            invalidPathExpression(pathExpression, config)
            return
        }
        if (configsToInject.isEmpty()) {
            noMatched(pathExpression, config)
        }
        // TODO 2.1.0
    }

    private fun invalidPathExpression(pathExpression: String, config: CwtMemberConfig<*>) {
        thisLogger().warn("Invalid path expression (path expression: ${pathExpression}, config: ${config})")
    }

    private fun noMatched(pathExpression: String, config: CwtMemberConfig<*>) {
        thisLogger().warn("No matched configs to inject found (path expression: ${pathExpression}, config: ${config})")
    }
}
