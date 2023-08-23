package icu.windea.pls.lang.cwt

import com.intellij.openapi.diagnostic.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.model.*
import java.lang.invoke.*
import java.util.*

/**
 * Cwt规则的解析器。
 */
object CwtConfigResolver {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
    
    fun resolve(file: CwtFile, info: CwtConfigGroupInfo): CwtFileConfig {
        val rootBlock = file.block
        val properties = mutableListOf<CwtPropertyConfig>()
        val values = mutableListOf<CwtValueConfig>()
        val fileConfig = CwtFileConfig(file.createPointer(), info, properties, values, file.name)
        rootBlock?.processChild {
            when {
                it is CwtProperty -> resolveProperty(it, file, fileConfig)?.addTo(properties).let { true }
                it is CwtValue -> resolveValue(it, file, fileConfig).addTo(values).let { true }
                else -> true
            }
        }
        return fileConfig
    }
    
    private fun resolveProperty(propertyElement: CwtProperty, file: CwtFile, fileConfig: CwtFileConfig): CwtPropertyConfig? {
        val valueElement = propertyElement.propertyValue
        if(valueElement == null) {
            logger.error("Incorrect CWT config in ${fileConfig.name}\n${propertyElement.text}")
            return null
        }
        val pointer = propertyElement.createPointer(file)
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
        
        val config = CwtPropertyConfig.resolve(pointer, fileConfig.info, key, value, valueType.id, separatorType.id, configs, options, documentation)
        fileConfig.info.acceptConfigExpression(config.keyExpression, config)
        fileConfig.info.acceptConfigExpression(config.valueExpression, config)
        configs?.forEach { it.parentConfig = config }
        return config
    }
    
    private fun resolveValue(valueElement: CwtValue, file: CwtFile, fileConfig: CwtFileConfig): CwtValueConfig {
        val pointer = valueElement.createPointer(file)
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
        
        val config = CwtValueConfig.resolve(pointer, fileConfig.info, value, valueType.id, configs, options, documentation)
        fileConfig.info.acceptConfigExpression(config.valueExpression, config)
        configs?.forEach { it.parentConfig = config }
        return config
    }
    
    private fun resolveOption(optionElement: CwtOption, file: CwtFile, fileConfig: CwtFileConfig): CwtOptionConfig? {
        val optionValueElement = optionElement.optionValue
        if(optionValueElement == null) {
            logger.error("Incorrect CWT config in ${fileConfig.name}\n${optionElement.text}")
            return null
        }
        val key = optionElement.name.intern() //intern to optimize memory
        val value = optionValueElement.value.intern() //intern to optimize memory
        val valueType: CwtType
        val separatorType = optionElement.separatorType
        var options: List<CwtOptionMemberConfig<*>>? = null
        
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
                            if(options == null) options = mutableListOf()
                            options!!.asMutable().add(resolved)
                        }
                        it is CwtValue -> {
                            val resolved = resolveOptionValue(it, file, fileConfig)
                            if(options == null) options = mutableListOf()
                            options!!.asMutable().add(resolved)
                        }
                    }
                }
                if(options == null) options = emptyList()
            }
            else -> {
                valueType = CwtType.Unknown
            }
        }
        
        return CwtOptionConfig.resolve(emptyPointer(), fileConfig.info, key, value, valueType.id, separatorType.id, options)
    }
    
    private fun resolveOptionValue(optionValueElement: CwtValue, file: CwtFile, fileConfig: CwtFileConfig): CwtOptionValueConfig {
        val value = optionValueElement.value.intern() //intern to optimize memory
        val valueType: CwtType
        var options: List<CwtOptionMemberConfig<*>>? = null
        
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
                            if(options == null) options = mutableListOf()
                            options!!.asMutable().add(resolved)
                        }
                        it is CwtValue -> {
                            val resolved = resolveOptionValue(it, file, fileConfig)
                            if(options == null) options = mutableListOf()
                            options!!.asMutable().add(resolved)
                        }
                    }
                }
                if(options == null) options = emptyList()
            }
            else -> {
                valueType = CwtType.Unknown
            }
        }
        
        return CwtOptionValueConfig.resolve(emptyPointer(), fileConfig.info, value, valueType.id, options)
    }
}
