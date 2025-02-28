package icu.windea.pls.config.util

import com.intellij.openapi.diagnostic.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*
import java.lang.invoke.*
import java.util.*

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
        val key = propertyElement.name.intern() //intern to optimize memory
        val value: String = valueElement.value.intern() //intern to optimize memory
        val valueType = valueElement.type
        val separatorType = propertyElement.separatorType
        val configs = doGetConfigs(valueElement, file, fileConfig)
        val (optionConfigs, documentation) = doGetOptionConfigsAndDocumentation(propertyElement, file, fileConfig)
        val config = CwtPropertyConfig.resolve(pointer, configGroup, key, value, valueType, separatorType, configs, optionConfigs, documentation)
            .let { applyInheritOptions(it) }
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
        val value: String = valueElement.value.intern() //intern to optimize memory
        val valueType = valueElement.type
        val configs = doGetConfigs(valueElement, file, fileConfig)
        val (optionConfigs, documentation) = doGetOptionConfigsAndDocumentation(valueElement, file, fileConfig)
        val config = CwtValueConfig.resolve(pointer, configGroup, value, valueType, configs, optionConfigs, documentation)
            .let { applyInheritOptions(it) }
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

    private fun doGetOptionConfigsAndDocumentation(element: PsiElement, file: CwtFile, fileConfig: CwtFileConfig): Tuple2<List<CwtOptionMemberConfig<*>>?, String?> {
        var optionConfigs: MutableList<CwtOptionMemberConfig<*>>? = null
        var documentationLines: MutableList<String>? = null
        var html = false

        var current: PsiElement = element
        while (true) {
            current = current.prevSibling ?: break
            when {
                current is CwtOptionComment -> {
                    val option = current.option
                    if (option != null) {
                        if (option.name == "format" && option.value == "html") html = true
                        if (optionConfigs == null) optionConfigs = mutableListOf()
                        val resolved = resolveOption(option, file, fileConfig) ?: continue
                        optionConfigs.add(0, resolved)
                    } else {
                        val optionValue = current.value ?: continue
                        if (optionConfigs == null) optionConfigs = mutableListOf()
                        val resolved = resolveOptionValue(optionValue, file, fileConfig)
                        optionConfigs.add(0, resolved)
                    }
                }
                current is CwtDocumentationComment -> {
                    val documentationText = current.documentationText
                    if (documentationText != null) {
                        if (documentationLines == null) documentationLines = LinkedList()
                        val docText = documentationText.text.trimStart('#').trim() //这里接受HTML
                        documentationLines.add(0, docText)
                    }
                }
                current is PsiWhiteSpace || current is PsiComment -> continue
                else -> break
            }
        }
        val documentation = getDocumentation(documentationLines, html)
        return optionConfigs?.optimized() to documentation //optimized to optimize memory
    }

    private fun resolveOption(optionElement: CwtOption, file: CwtFile, fileConfig: CwtFileConfig): CwtOptionConfig? {
        val optionValueElement = optionElement.optionValue
        if (optionValueElement == null) {
            logger.warn("Incorrect CWT config in ${file.virtualFile.path}:\n${optionElement.text}")
            return null
        }

        val key = optionElement.name.intern() //intern to optimize memory
        val value = optionValueElement.value.intern() //intern to optimize memory
        val valueType: CwtType = optionValueElement.type
        val separatorType = optionElement.separatorType
        val optionConfigs = doGetOptionConfigs(optionValueElement, file, fileConfig)
        return CwtOptionConfig.resolve(key, value, valueType, separatorType, optionConfigs)
    }

    private fun resolveOptionValue(optionValueElement: CwtValue, file: CwtFile, fileConfig: CwtFileConfig): CwtOptionValueConfig {
        val value = optionValueElement.value.intern() //intern to optimize memory
        val valueType = optionValueElement.type
        val optionConfigs = doGetOptionConfigs(optionValueElement, file, fileConfig)
        return CwtOptionValueConfig.resolve(value, valueType, optionConfigs)
    }

    private fun doGetOptionConfigs(optionValueElement: CwtValue, file: CwtFile, fileConfig: CwtFileConfig): List<CwtOptionMemberConfig<*>>? {
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

    private fun <T : CwtMemberConfig<*>> applyInheritOptions(config: T): T {
        //TODO 1.3.18+
        //val configGroup = config.configGroup
        //
        //var inheritConfigsValue: String? = null
        //var inheritOptionsValue: String? = null
        //var inheritDocValue: String? = null
        //
        //val oldOptions = mutableListOf<CwtOptionMemberConfig<*>>()
        //config.options?.forEach { o ->
        //    when(o){
        //        is CwtOptionConfig -> when(o.key) {
        //            "inherit_configs" -> o.stringValue?.let { inheritConfigsValue = it }
        //            "inherit_options" -> o.stringValue?.let { inheritOptionsValue = it }
        //            "inherit_doc" -> o.stringValue?.let { inheritDocValue = it }
        //            else -> oldOptions += o
        //        }
        //        is CwtOptionValueConfig -> oldOptions += o
        //    }
        //}
        //
        //if(inheritConfigsValue == null && inheritOptionsValue == null && inheritDocValue == null) return config
        //
        //var newConfigs: List<CwtMemberConfig<*>>? = null
        //var newOptions: List<CwtOptionMemberConfig<*>>? = null
        //var newDocumentation: String? = null
        //
        //inheritDocValue?.let { pathExpression ->
        //    CwtConfigManager.getConfigByPathExpression(configGroup, pathExpression)?.let { newConfig ->
        //        newDocumentation = newConfig.documentation
        //    }
        //}
        //
        //if(newConfigs == null && newOptions == null && newDocumentation == null) return config

        return config
    }
}
