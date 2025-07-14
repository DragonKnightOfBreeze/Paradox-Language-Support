package icu.windea.pls.config.util

import com.intellij.openapi.diagnostic.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.codeInsight.*
import icu.windea.pls.model.*

private val logger = logger<CwtConfigResolver>()

object CwtConfigResolver {
    fun resolve(file: CwtFile, configGroup: CwtConfigGroup): CwtFileConfig {
        val rootBlock = file.block
        val properties = mutableListOf<CwtPropertyConfig>()
        val values = mutableListOf<CwtValueConfig>()
        val fileConfig = CwtFileConfig(file.createPointer(), configGroup, properties, values, file.name)
        rootBlock?.processChild { e ->
            when {
                e is CwtProperty -> resolveProperty(e, file, fileConfig)?.also { properties.add(it) }
                e is CwtValue -> resolveValue(e, file, fileConfig).also { values.add(it) }
            }
            true
        }
        return fileConfig
    }

    private fun resolveProperty(propertyElement: CwtProperty, file: CwtFile, fileConfig: CwtFileConfig): CwtPropertyConfig? {
        val configGroup = fileConfig.configGroup
        val valueElement = propertyElement.propertyValue
        if (valueElement == null) {
            logger.warn("Incorrect CWT config in ${file.virtualFile.path}")
            return null
        }

        //1. use EmptyPointer for default project to optimize memory
        //2. use CwtPropertyPointer to optimize performance and memory
        val pointer = when {
            configGroup.project.isDefault -> emptyPointer()
            else -> propertyElement.createPointer(file).let { CwtPropertyPointer(it) }
        }
        val key = propertyElement.name
        val value: String = valueElement.value
        val valueType = valueElement.type
        val separatorType = propertyElement.separatorType
        val configs = doGetConfigs(valueElement, file, fileConfig)
        val optionConfigs = doGetOptionConfigs(propertyElement, file, fileConfig)
        val config = CwtPropertyConfig.resolve(pointer, configGroup, key, value, valueType, separatorType, configs, optionConfigs)
        CwtConfigCollector.postHandleConfig(config)
        CwtConfigCollector.processConfigWithConfigExpression(config, config.keyExpression)
        CwtConfigCollector.processConfigWithConfigExpression(config, config.valueExpression)
        configs?.forEach { it.parentConfig = config }
        return config
    }

    private fun resolveValue(valueElement: CwtValue, file: CwtFile, fileConfig: CwtFileConfig): CwtValueConfig {
        val configGroup = fileConfig.configGroup

        //1. use EmptyPointer for default project to optimize memory
        val pointer = when {
            configGroup.project.isDefault -> emptyPointer()
            else -> valueElement.createPointer(file)
        }
        val value: String = valueElement.value
        val valueType = valueElement.type
        val configs = doGetConfigs(valueElement, file, fileConfig)
        val optionConfigs = doGetOptionConfigs(valueElement, file, fileConfig)
        val config = CwtValueConfig.resolve(pointer, configGroup, value, valueType, configs, optionConfigs)
        CwtConfigCollector.postHandleConfig(config)
        CwtConfigCollector.processConfigWithConfigExpression(config, config.valueExpression)
        configs?.forEach { it.parentConfig = config }
        return config
    }

    private fun doGetConfigs(valueElement: CwtValue, file: CwtFile, fileConfig: CwtFileConfig): List<CwtMemberConfig<*>>? {
        if (valueElement !is CwtBlock) return null
        val configs = mutableListOf<CwtMemberConfig<*>>()
        valueElement.forEachChild f@{ e ->
            when {
                e is CwtProperty -> {
                    val resolved = resolveProperty(e, file, fileConfig) ?: return@f
                    configs += resolved
                }
                e is CwtValue -> {
                    val resolved = resolveValue(e, file, fileConfig)
                    configs += resolved
                }
            }
        }
        return configs.optimized() //optimized to optimize memory
    }

    private fun doGetOptionConfigs(element: PsiElement, file: CwtFile, fileConfig: CwtFileConfig): List<CwtOptionMemberConfig<*>>? {
        var optionConfigs: MutableList<CwtOptionMemberConfig<*>>? = null
        var current: PsiElement = element
        while (true) {
            current = current.prevSibling ?: break
            when {
                current is CwtOptionComment -> {
                    val option = current.option
                    if (option != null) {
                        if (optionConfigs == null) optionConfigs = mutableListOf()
                        val resolved = resolveOption(option, file, fileConfig) ?: continue
                        optionConfigs.add(0, resolved)
                    } else {
                        val optionValue = current.optionValue ?: continue
                        if (optionConfigs == null) optionConfigs = mutableListOf()
                        val resolved = resolveOptionValue(optionValue, file, fileConfig)
                        optionConfigs.add(0, resolved)
                    }
                }
                current is PsiWhiteSpace || current is PsiComment -> continue
                else -> break
            }
        }
        return optionConfigs?.optimized() //optimized to optimize memory
    }

    private fun resolveOption(optionElement: CwtOption, file: CwtFile, fileConfig: CwtFileConfig): CwtOptionConfig? {
        val optionValueElement = optionElement.optionValue
        if (optionValueElement == null) {
            logger.warn("Incorrect CWT config in ${file.virtualFile.path}:\n${optionElement.text}")
            return null
        }

        val key = optionElement.name
        val value = optionValueElement.value
        val valueType: CwtType = optionValueElement.type
        val separatorType = optionElement.separatorType
        val optionConfigs = doGetOptionConfigsInOption(optionValueElement, file, fileConfig)
        return CwtOptionConfig.resolve(key, value, valueType, separatorType, optionConfigs)
    }

    private fun resolveOptionValue(optionValueElement: CwtValue, file: CwtFile, fileConfig: CwtFileConfig): CwtOptionValueConfig {
        val value = optionValueElement.value
        val valueType = optionValueElement.type
        val optionConfigs = doGetOptionConfigsInOption(optionValueElement, file, fileConfig)
        return CwtOptionValueConfig.resolve(value, valueType, optionConfigs)
    }

    private fun doGetOptionConfigsInOption(optionValueElement: CwtValue, file: CwtFile, fileConfig: CwtFileConfig): List<CwtOptionMemberConfig<*>>? {
        if (optionValueElement !is CwtBlock) return null
        val optionConfigs = mutableListOf<CwtOptionMemberConfig<*>>()
        optionValueElement.forEachChild f@{ e ->
            when {
                e is CwtOption -> {
                    val resolved = resolveOption(e, file, fileConfig) ?: return@f
                    optionConfigs += resolved
                }
                e is CwtValue -> {
                    val resolved = resolveOptionValue(e, file, fileConfig)
                    optionConfigs += resolved
                }
            }
        }
        return optionConfigs.optimized() //optimized to optimize memory
    }
}
