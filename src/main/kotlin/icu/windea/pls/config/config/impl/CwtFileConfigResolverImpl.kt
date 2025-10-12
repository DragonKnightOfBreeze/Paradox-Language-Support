package icu.windea.pls.config.config.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.diagnostic.trace
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtOptionValueConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigCollector
import icu.windea.pls.config.util.CwtConfigResolverUtil.withLocationPrefix
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.processChild
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.cwt.psi.CwtOptionComment
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtPropertyPointer
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.lang.codeInsight.type
import icu.windea.pls.model.CwtType

internal class CwtFileConfigResolverImpl : CwtFileConfig.Resolver {
    private val logger = thisLogger()

    override fun resolve(file: CwtFile, filePath: String, configGroup: CwtConfigGroup): CwtFileConfig {
        val rootBlock = file.block
        val properties = mutableListOf<CwtPropertyConfig>()
        val values = mutableListOf<CwtValueConfig>()
        val fileConfig = CwtFileConfigImpl(file.createPointer(), configGroup, file.name, filePath, properties, values)
        rootBlock?.processChild { e ->
            when {
                e is CwtProperty -> resolveProperty(e, file, filePath, configGroup)?.also { properties.add(it) }
                e is CwtValue -> resolveValue(e, file, filePath, configGroup).also { values.add(it) }
            }
            true
        }
        val memberSize = properties.size + values.size
        logger.debug { "Resolved file config ($memberSize member configs).".withLocationPrefix() }
        return fileConfig
    }

    private fun resolveProperty(propertyElement: CwtProperty, file: CwtFile, filePath: String, configGroup: CwtConfigGroup): CwtPropertyConfig? {
        // 1. use EmptyPointer for default project to optimize memory
        // 2. use CwtPropertyPointer to optimize performance and memory

        val valueElement = propertyElement.propertyValue
        if (valueElement == null) {
            logger.warn("Missing property value.".withLocationPrefix(propertyElement))
            return null
        }
        val pointer = when {
            configGroup.project.isDefault -> emptyPointer()
            else -> CwtPropertyPointer(propertyElement.createPointer(file))
        }
        val key = propertyElement.name
        val value: String = valueElement.value
        val valueType = valueElement.type
        val separatorType = propertyElement.separatorType
        val configs = doGetConfigs(valueElement, file, filePath, configGroup)
        val optionConfigs = doGetOptionConfigs(propertyElement, file, filePath, configGroup)
        val config = CwtPropertyConfig.resolve(pointer, configGroup, key, value, valueType, separatorType, configs, optionConfigs)
        CwtConfigCollector.postHandleConfig(config)
        CwtConfigCollector.processConfigWithConfigExpression(config, config.keyExpression)
        CwtConfigCollector.processConfigWithConfigExpression(config, config.valueExpression)
        configs?.forEach { it.parentConfig = config }
        logger.trace { "Resolved property config (key: ${config.key}, value: ${config.value}).".withLocationPrefix(propertyElement) }
        return config
    }

    private fun resolveValue(valueElement: CwtValue, file: CwtFile, filePath: String, configGroup: CwtConfigGroup): CwtValueConfig {
        // 1. use EmptyPointer for default project to optimize memory

        val pointer = when {
            configGroup.project.isDefault -> emptyPointer()
            else -> valueElement.createPointer(file)
        }
        val value: String = valueElement.value
        val valueType = valueElement.type
        val configs = doGetConfigs(valueElement, file, filePath, configGroup)
        val optionConfigs = doGetOptionConfigs(valueElement, file, filePath, configGroup)
        val config = CwtValueConfig.resolve(pointer, configGroup, value, valueType, configs, optionConfigs)
        CwtConfigCollector.postHandleConfig(config)
        CwtConfigCollector.processConfigWithConfigExpression(config, config.valueExpression)
        configs?.forEach { it.parentConfig = config }
        logger.trace { "Resolved value config (value: ${config.value}).".withLocationPrefix(valueElement) }
        return config
    }

    private fun doGetConfigs(valueElement: CwtValue, file: CwtFile, filePath: String, configGroup: CwtConfigGroup): List<CwtMemberConfig<*>>? {
        if (valueElement !is CwtBlock) return null
        val configs = mutableListOf<CwtMemberConfig<*>>()
        valueElement.forEachChild f@{ e ->
            when {
                e is CwtProperty -> {
                    val resolved = resolveProperty(e, file, filePath, configGroup) ?: return@f
                    configs += resolved
                }
                e is CwtValue -> {
                    val resolved = resolveValue(e, file, filePath, configGroup)
                    configs += resolved
                }
            }
        }
        return configs.optimized() // optimized to optimize memory
    }

    private fun doGetOptionConfigs(element: PsiElement, file: CwtFile, filePath: String, configGroup: CwtConfigGroup): List<CwtOptionMemberConfig<*>>? {
        var optionConfigs: MutableList<CwtOptionMemberConfig<*>>? = null
        var current: PsiElement = element
        while (true) {
            current = current.prevSibling ?: break
            when {
                current is CwtOptionComment -> {
                    val option = current.option
                    if (option != null) {
                        if (optionConfigs == null) optionConfigs = mutableListOf()
                        val resolved = resolveOption(option, file, filePath, configGroup) ?: continue
                        optionConfigs.add(0, resolved)
                    } else {
                        val optionValue = current.optionValue ?: continue
                        if (optionConfigs == null) optionConfigs = mutableListOf()
                        val resolved = resolveOptionValue(optionValue, file, filePath, configGroup)
                        optionConfigs.add(0, resolved)
                    }
                }
                current is PsiWhiteSpace || current is PsiComment -> continue
                else -> break
            }
        }
        return optionConfigs?.optimized() // optimized to optimize memory
    }

    private fun resolveOption(optionElement: CwtOption, file: CwtFile, filePath: String, configGroup: CwtConfigGroup): CwtOptionConfig? {
        val optionValueElement = optionElement.optionValue
        if (optionValueElement == null) {
            logger.warn("Missing option value.".withLocationPrefix(optionElement))
            return null
        }
        val key = optionElement.name
        val value = optionValueElement.value
        val valueType: CwtType = optionValueElement.type
        val separatorType = optionElement.separatorType
        val optionConfigs = doGetOptionConfigsInOption(optionValueElement, file, filePath, configGroup)
        return CwtOptionConfig.resolve(key, value, valueType, separatorType, optionConfigs)
    }

    private fun resolveOptionValue(optionValueElement: CwtValue, file: CwtFile, filePath: String, configGroup: CwtConfigGroup): CwtOptionValueConfig {
        val value = optionValueElement.value
        val valueType = optionValueElement.type
        val optionConfigs = doGetOptionConfigsInOption(optionValueElement, file, filePath, configGroup)
        return CwtOptionValueConfig.resolve(value, valueType, optionConfigs)
    }

    private fun doGetOptionConfigsInOption(optionValueElement: CwtValue, file: CwtFile, filePath: String, configGroup: CwtConfigGroup): List<CwtOptionMemberConfig<*>>? {
        if (optionValueElement !is CwtBlock) return null
        val optionConfigs = mutableListOf<CwtOptionMemberConfig<*>>()
        optionValueElement.forEachChild f@{ e ->
            when {
                e is CwtOption -> {
                    val resolved = resolveOption(e, file, filePath, configGroup) ?: return@f
                    optionConfigs += resolved
                }
                e is CwtValue -> {
                    val resolved = resolveOptionValue(e, file, filePath, configGroup)
                    optionConfigs += resolved
                }
            }
        }
        return optionConfigs.optimized() // optimized to optimize memory
    }
}

private class CwtFileConfigImpl(
    override val pointer: SmartPsiElementPointer<CwtFile>,
    override val configGroup: CwtConfigGroup,
    override val name: String,
    override val path: String,
    override val properties: List<CwtPropertyConfig>,
    override val values: List<CwtValueConfig>,
) : UserDataHolderBase(), CwtFileConfig {
    override fun toString() = "CwtFileConfigImpl(name='$name', path='$path')"
}
