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
