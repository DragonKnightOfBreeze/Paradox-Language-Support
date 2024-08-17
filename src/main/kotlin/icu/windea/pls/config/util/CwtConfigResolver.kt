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

object CwtConfigResolver {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
    
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
        if(valueElement == null) {
            logger.warn("Incorrect CWT config in ${file.virtualFile.path}")
            return null
        }
        //1. ues EmptyPointer for default project to optimize memory
        //2. use CwtPropertyPointer to optimize performance and memory
        val pointer = when {
            configGroup.project.isDefault -> emptyPointer()
            else -> propertyElement.createPointer(file).let { CwtPropertyPointer(it) }
        }
        val key = propertyElement.name.intern() //intern to optimize memory
        val value: String = valueElement.value.intern() //intern to optimize memory
        val valueType: CwtType
        val separatorType = propertyElement.separatorType
        var configs: List<CwtMemberConfig<*>>? = null
        var documentationLines: LinkedList<String>? = null
        var html = false
        var options: LinkedList<CwtOptionMemberConfig<*>>? = null
        
        when {
            valueElement is CwtBoolean -> {
                valueType = CwtType.Boolean
            }
            valueElement is CwtInt -> {
                valueType = CwtType.Int
            }
            valueElement is CwtFloat -> {
                valueType = CwtType.Float
            }
            valueElement is CwtString -> {
                valueType = CwtType.String
            }
            valueElement is CwtBlock -> {
                valueType = CwtType.Block
                valueElement.forEachChild f@{
                    when {
                        it is CwtProperty -> {
                            val resolved = resolveProperty(it, file, fileConfig) ?: return@f
                            if(configs == null) configs = mutableListOf()
                            configs!!.asMutable().add(resolved)
                        }
                        it is CwtValue -> {
                            val resolved = resolveValue(it, file, fileConfig)
                            if(configs == null) configs = mutableListOf()
                            configs!!.asMutable().add(resolved)
                        }
                    }
                }
                if(configs == null) configs = emptyList()
            }
            else -> {
                valueType = CwtType.Unknown
            }
        }
        
        var current: PsiElement = propertyElement
        while(true) {
            current = current.prevSibling ?: break
            when {
                current is CwtDocumentationComment -> {
                    val documentationText = current.documentationText
                    if(documentationText != null) {
                        if(documentationLines == null) documentationLines = LinkedList()
                        val docText = documentationText.text.trimStart('#').trim() //这里接受HTML
                        documentationLines.addFirst(docText)
                    }
                }
                current is CwtOptionComment -> {
                    val option = current.option
                    if(option != null) {
                        if(option.name == "format" && option.value == "html") html = true
                        if(options == null) options = LinkedList()
                        val resolved = resolveOption(option, file, fileConfig) ?: continue
                        options.addFirst(resolved)
                    } else {
                        val optionValue = current.value ?: continue
                        if(options == null) options = LinkedList()
                        val resolved = resolveOptionValue(optionValue, file, fileConfig)
                        options.addFirst(resolved)
                    }
                }
                current is PsiWhiteSpace || current is PsiComment -> continue
                else -> break
            }
        }
        val documentation = getDocumentation(documentationLines, html)
        
        val config = CwtPropertyConfig.resolve(pointer, configGroup, key, value, valueType.id, separatorType.id, configs, options, documentation)
            .let { applyInheritOptions(it) }
        CwtConfigCollector.processConfigWithConfigExpression(config, config.keyExpression)
        CwtConfigCollector.processConfigWithConfigExpression(config, config.valueExpression)
        configs?.forEach { it.parentConfig = config }
        return config
    }
    
    private fun resolveValue(valueElement: CwtValue, file: CwtFile, fileConfig: CwtFileConfig): CwtValueConfig {
        val configGroup = fileConfig.configGroup
        //1. ues EmptyPointer for default project to optimize memory
        val pointer = when {
            configGroup.project.isDefault -> emptyPointer()
            else -> valueElement.createPointer(file)
        }
        val value: String = valueElement.value.intern() //intern to optimize memory
        val valueType: CwtType
        var configs: List<CwtMemberConfig<*>>? = null
        var documentationLines: LinkedList<String>? = null
        var html = false
        var options: LinkedList<CwtOptionMemberConfig<*>>? = null
        
        when {
            valueElement is CwtBoolean -> {
                valueType = CwtType.Boolean
            }
            valueElement is CwtInt -> {
                valueType = CwtType.Int
            }
            valueElement is CwtFloat -> {
                valueType = CwtType.Float
            }
            valueElement is CwtString -> {
                valueType = CwtType.String
            }
            valueElement is CwtBlock -> {
                valueType = CwtType.Block
                valueElement.forEachChild f@{
                    when {
                        it is CwtProperty -> {
                            val resolved = resolveProperty(it, file, fileConfig) ?: return@f
                            if(configs == null) configs = mutableListOf()
                            configs!!.asMutable().add(resolved)
                        }
                        it is CwtValue -> {
                            val resolved = resolveValue(it, file, fileConfig)
                            if(configs == null) configs = mutableListOf()
                            configs!!.asMutable().add(resolved)
                        }
                    }
                }
                if(configs == null) configs = emptyList()
            }
            else -> {
                valueType = CwtType.Unknown
            }
        }
        
        var current: PsiElement = valueElement
        while(true) {
            current = current.prevSibling ?: break
            when {
                current is CwtDocumentationComment -> {
                    val documentationText = current.documentationText
                    if(documentationText != null) {
                        if(documentationLines == null) documentationLines = LinkedList()
                        val docText = documentationText.text.trimStart('#').trim() //这里接受HTML
                        documentationLines.addFirst(docText)
                    }
                }
                current is CwtOptionComment -> {
                    val option = current.option
                    if(option != null) {
                        if(option.name == "format" && option.value == "html") html = true
                        if(options == null) options = LinkedList()
                        val resolved = resolveOption(option, file, fileConfig) ?: continue
                        options.addFirst(resolved)
                    } else {
                        val optionValue = current.value ?: continue
                        if(options == null) options = LinkedList()
                        val resolved = resolveOptionValue(optionValue, file, fileConfig)
                        options.addFirst(resolved)
                    }
                }
                current is PsiWhiteSpace || current is PsiComment -> continue
                else -> break
            }
        }
        val documentation = getDocumentation(documentationLines, html)
        
        val config = CwtValueConfig.resolve(pointer, configGroup, value, valueType.id, configs, options, documentation)
            .let { applyInheritOptions(it) }
        CwtConfigCollector.processConfigWithConfigExpression(config, config.valueExpression)
        configs?.forEach { it.parentConfig = config }
        return config
    }
    
    private fun resolveOption(optionElement: CwtOption, file: CwtFile, fileConfig: CwtFileConfig): CwtOptionConfig? {
        val optionValueElement = optionElement.optionValue
        if(optionValueElement == null) {
            logger.warn("Incorrect CWT config in ${file.virtualFile.path}")
            return null
        }
        val key = optionElement.name.intern() //intern to optimize memory
        val value = optionValueElement.value.intern() //intern to optimize memory
        val valueType: CwtType
        val separatorType = optionElement.separatorType
        var optionConfigs: List<CwtOptionMemberConfig<*>>? = null
        
        when {
            optionValueElement is CwtBoolean -> {
                valueType = CwtType.Boolean
            }
            optionValueElement is CwtInt -> {
                valueType = CwtType.Int
            }
            optionValueElement is CwtFloat -> {
                valueType = CwtType.Float
            }
            optionValueElement is CwtString -> {
                valueType = CwtType.String
            }
            optionValueElement is CwtBlock -> {
                valueType = CwtType.Block
                optionValueElement.forEachChild f@{
                    when {
                        it is CwtOption -> {
                            val resolved = resolveOption(it, file, fileConfig) ?: return@f
                            if(optionConfigs == null) optionConfigs = mutableListOf()
                            optionConfigs!!.asMutable().add(resolved)
                        }
                        it is CwtValue -> {
                            val resolved = resolveOptionValue(it, file, fileConfig)
                            if(optionConfigs == null) optionConfigs = mutableListOf()
                            optionConfigs!!.asMutable().add(resolved)
                        }
                    }
                }
                if(optionConfigs == null) optionConfigs = emptyList()
            }
            else -> {
                valueType = CwtType.Unknown
            }
        }
        
        return CwtOptionConfig.resolve(key, value, valueType, separatorType, optionConfigs)
    }
    
    private fun resolveOptionValue(optionValueElement: CwtValue, file: CwtFile, fileConfig: CwtFileConfig): CwtOptionValueConfig {
        val value = optionValueElement.value.intern() //intern to optimize memory
        val valueType: CwtType
        var optionConfigs: List<CwtOptionMemberConfig<*>>? = null
        
        when {
            optionValueElement is CwtBoolean -> {
                valueType = CwtType.Boolean
            }
            optionValueElement is CwtInt -> {
                valueType = CwtType.Int
            }
            optionValueElement is CwtFloat -> {
                valueType = CwtType.Float
            }
            optionValueElement is CwtString -> {
                valueType = CwtType.String
            }
            optionValueElement is CwtBlock -> {
                valueType = CwtType.Block
                optionValueElement.forEachChild f@{
                    when {
                        it is CwtOption -> {
                            val resolved = resolveOption(it, file, fileConfig) ?: return@f
                            if(optionConfigs == null) optionConfigs = mutableListOf()
                            optionConfigs!!.asMutable().add(resolved)
                        }
                        it is CwtValue -> {
                            val resolved = resolveOptionValue(it, file, fileConfig)
                            if(optionConfigs == null) optionConfigs = mutableListOf()
                            optionConfigs!!.asMutable().add(resolved)
                        }
                    }
                }
                if(optionConfigs == null) optionConfigs = emptyList()
            }
            else -> {
                valueType = CwtType.Unknown
            }
        }
        
        return CwtOptionValueConfig.resolve(value, valueType, optionConfigs)
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
