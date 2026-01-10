package icu.windea.pls.ep.config

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.config.tagType
import icu.windea.pls.config.util.CwtConfigResolverManager
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.model.ParadoxTagType

class CwtBaseConfigPostProcessor : CwtConfigPostProcessor {
    override fun postProcess(config: CwtMemberConfig<*>) {
        // #123 mark tag type as predefined for config
        applyTagOption(config)
    }

    private fun applyTagOption(config: CwtMemberConfig<*>) {
        if (config is CwtValueConfig && config.optionData { flags }.tag) {
            config.tagType = ParadoxTagType.Predefined
        }
    }
}

class CwtInjectConfigPostProcessor : CwtConfigPostProcessor {
    private val logger = thisLogger()

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

        // avoid (shallow) recursion injection: if configs to inject also contain inject option, ignore directly
        if (configsToInject.any { it.optionData { inject } != null }) {
            recursive(pathExpression, config)
            return
        }

        val targetConfig = config
        val originalConfigs = targetConfig.configs ?: return
        val injectedConfigs = CwtConfigManipulator.createListForDeepCopy()
        configsToInject.forEach { configToInject ->
            injectedConfigs += deepCopyForInjection(configToInject, targetConfig)
        }
        if (injectedConfigs.isEmpty()) {
            noMatched(pathExpression, config)
            return
        }

        val newConfigs = CwtConfigManipulator.createListForDeepCopy()
        newConfigs += originalConfigs
        newConfigs += injectedConfigs

        val updated = updateChildConfigs(targetConfig, newConfigs)
        if (!updated) {
            logger.warn("Config injection ignored because config cannot carry child configs (config: ${config})")
            return
        }
        logger.info("Applied config injection (path expression: ${pathExpression}, config: ${config})")
    }

    private fun invalidPathExpression(pathExpression: String, config: CwtMemberConfig<*>) {
        logger.warn("Invalid path expression (path expression: ${pathExpression}, config: ${config})")
    }

    private fun noMatched(pathExpression: String, config: CwtMemberConfig<*>) {
        logger.warn("No matched configs to inject found (path expression: ${pathExpression}, config: ${config})")
    }

    private fun recursive(pathExpression: String, config: CwtMemberConfig<*>) {
        logger.warn("Config injection ignored due to recursion (path expression: ${pathExpression}, config: ${config})")
    }

    private fun updateChildConfigs(targetConfig: CwtMemberConfig<*>, configs: List<CwtMemberConfig<*>>): Boolean {
        val updated = CwtMemberConfig.withConfigs(targetConfig, configs)
        if (updated) CwtMemberConfig.postOptimize(targetConfig)
        return updated
    }

    private fun deepCopyForInjection(configToInject: CwtMemberConfig<*>, parentConfig: CwtMemberConfig<*>): CwtMemberConfig<*> {
        val sourceConfigs = configToInject.configs
        if (sourceConfigs == null) {
            return CwtMemberConfig.delegated(configToInject, null).also { it.parentConfig = parentConfig }
        }
        val copiedChildConfigs = CwtConfigManipulator.createListForDeepCopy(sourceConfigs)
            ?: return CwtMemberConfig.delegated(configToInject, null).also { it.parentConfig = parentConfig }
        val delegatedConfig = CwtMemberConfig.delegated(configToInject, copiedChildConfigs).also { it.parentConfig = parentConfig }
        copiedChildConfigs += CwtConfigManipulator.deepCopyConfigs(configToInject, delegatedConfig).orEmpty()
        CwtMemberConfig.postOptimize(delegatedConfig)
        return delegatedConfig
    }
}
