package icu.windea.pls.ep.config

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.config.CwtTagType
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.config.tagType
import icu.windea.pls.config.util.CwtConfigResolverManager
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.core.withRecursionGuard

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

        withRecursionGuard {
            withRecursionCheck("cwt-inject@${pathExpression}@${config.pointer}") a@{
                val targetConfig = config
                val originalConfigs = targetConfig.configs ?: return@a null
                val injectedConfigs = CwtConfigManipulator.createListForDeepCopy()
                configsToInject.forEach { configToInject ->
                    injectedConfigs += deepCopyForInjection(configToInject, targetConfig)
                }
                if (injectedConfigs.isEmpty()) {
                    noMatched(pathExpression, config)
                    return@a null
                }

                val newConfigs = CwtConfigManipulator.createListForDeepCopy()
                newConfigs += originalConfigs
                newConfigs += injectedConfigs

                val updated = updateChildConfigs(targetConfig, newConfigs)
                if (!updated) {
                    val delegatedTargetConfig = CwtMemberConfig.delegated(targetConfig, newConfigs)
                    delegatedTargetConfig.parentConfig = targetConfig.parentConfig
                    CwtMemberConfig.postOptimize(delegatedTargetConfig)
                    val replaced = replaceConfig(targetConfig, delegatedTargetConfig)
                    if (!replaced) {
                        logger.warn("Config injection ignored because config cannot be replaced (config: ${config})")
                        return@a null
                    }
                }
                logger.info("Applied config injection (path expression: ${pathExpression}, config: ${config})")
                null
            }
        } ?: run {
            recursive(pathExpression, config)
        }
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
        val expectedSize = configs.size
        return when (targetConfig) {
            is CwtPropertyConfig -> {
                CwtPropertyConfig.withConfigs(targetConfig, configs)
                CwtPropertyConfig.postOptimize(targetConfig)
                true
            }
            is CwtValueConfig -> {
                CwtValueConfig.withConfigs(targetConfig, configs)
                CwtValueConfig.postOptimize(targetConfig)
                targetConfig.configs?.size == expectedSize
            }
        }
    }

    private fun replaceConfig(oldConfig: CwtMemberConfig<*>, newConfig: CwtMemberConfig<*>): Boolean {
        val parentConfig = oldConfig.parentConfig
        if (parentConfig != null) {
            val parentConfigs = parentConfig.configs ?: return false
            val index = parentConfigs.indexOf(oldConfig)
            if (index == -1) return false
            val newParentConfigs = CwtConfigManipulator.createListForDeepCopy().also {
                it += parentConfigs
                it[index] = newConfig
            }
            return updateChildConfigs(parentConfig, newParentConfigs)
        }

        val elementFile = oldConfig.pointer.element?.containingFile
        val fileConfigs = CwtConfigResolverManager.getFileConfigs(oldConfig.configGroup)
        val fileConfig = fileConfigs.values.firstOrNull { it.pointer.element == elementFile } ?: return false
        val rootConfigs = fileConfig.configs
        val index = rootConfigs.indexOf(oldConfig)
        if (index == -1) return false

        val newRootConfigs = CwtConfigManipulator.createListForDeepCopy().also {
            it += rootConfigs
            it[index] = newConfig
        }
        CwtFileConfig.withConfigs(fileConfig, newRootConfigs)
        return true
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
