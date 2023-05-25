package icu.windea.pls.config

import com.intellij.openapi.diagnostic.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.model.*
import java.lang.invoke.*
import java.util.*

/**
 * Cwt规则的解析器。
 */
object CwtConfigResolver {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
    
    fun resolve(file: CwtFile, info: CwtConfigGroupInfo): CwtFileConfig {
        val rootBlock = file.block
        val properties = SmartList<CwtPropertyConfig>()
        val values = SmartList<CwtValueConfig>()
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
    
    private fun resolveProperty(property: CwtProperty, file: CwtFile, fileConfig: CwtFileConfig): CwtPropertyConfig? {
        val pointer = property.createPointer(file)
        val key = property.name
        val propertyValue = property.propertyValue
        if(propertyValue == null) {
            logger.error("Incorrect cwt config in ${fileConfig.name}\n${property.text}")
            return null
        }
        var booleanValue: Boolean? = null
        var intValue: Int? = null
        var floatValue: Float? = null
        var stringValue: String? = null
        var configs: List<CwtDataConfig<*>>? = null
        var documentationLines: LinkedList<String>? = null
        var html = false
        var options: LinkedList<CwtOptionConfig>? = null
        var optionValues: LinkedList<CwtOptionValueConfig>? = null
        val separatorType = property.separatorType
        when {
            propertyValue is CwtBoolean -> booleanValue = propertyValue.booleanValue
            propertyValue is CwtInt -> intValue = propertyValue.intValue
            propertyValue is CwtFloat -> floatValue = propertyValue.floatValue
            propertyValue is CwtString -> stringValue = propertyValue.stringValue
            propertyValue is CwtBlock -> when {
                propertyValue.isEmpty -> {
                    configs = emptyList()
                }
                else -> {
                    configs = SmartList()
                    propertyValue.processChild {
                        when {
                            it is CwtProperty -> resolveProperty(it, file, fileConfig)?.addTo(configs).let { true }
                            it is CwtValue -> resolveValue(it, file, fileConfig).addTo(configs).let { true }
                            else -> true
                        }
                    }
                }
            }
        }
        
        var current: PsiElement = property
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
                        when {
                            option.name == "format" && option.value == "html" -> html = true
                        }
                        if(options == null) options = LinkedList()
                        val resolvedOption = resolveOption(option, file, fileConfig)
                        if(resolvedOption != null) options.addFirst(resolvedOption)
                    } else {
                        val optionValue = current.value
                        if(optionValue != null) {
                            if(optionValues == null) optionValues = LinkedList()
                            val resolvedOptionValue = resolveOptionValue(optionValue, file, fileConfig)
                            optionValues.addFirst(resolvedOptionValue)
                        }
                    }
                }
                current is PsiWhiteSpace || current is PsiComment -> continue
                else -> break
            }
        }
        val documentation = getDocumentation(documentationLines, html)
        
        val config = CwtPropertyConfig(
            pointer, fileConfig.info, key, propertyValue.value,
            booleanValue, intValue, floatValue, stringValue, configs,
            documentation, options, optionValues, separatorType
        )
        fileConfig.info.acceptConfigExpression(config.keyExpression, config)
        fileConfig.info.acceptConfigExpression(config.valueExpression, config)
        configs?.forEach { it.parent = config }
        return config
    }
    
    private fun resolveValue(value: CwtValue, file: CwtFile, fileConfig: CwtFileConfig): CwtValueConfig {
        val pointer = value.createPointer(file)
        var booleanValue: Boolean? = null
        var intValue: Int? = null
        var floatValue: Float? = null
        var stringValue: String? = null
        var configs: List<CwtDataConfig<*>>? = null
        var documentationLines: LinkedList<String>? = null
        var html = false
        var options: LinkedList<CwtOptionConfig>? = null
        var optionValues: LinkedList<CwtOptionValueConfig>? = null
        
        when {
            value is CwtBoolean -> booleanValue = value.booleanValue
            value is CwtInt -> intValue = value.intValue
            value is CwtFloat -> floatValue = value.floatValue
            value is CwtString -> stringValue = value.stringValue
            value is CwtBlock -> when {
                value.isEmpty -> {
                    configs = emptyList()
                }
                else -> {
                    configs = SmartList()
                    value.processChild {
                        when {
                            it is CwtProperty -> resolveProperty(it, file, fileConfig)?.addTo(configs).let { true }
                            it is CwtValue -> resolveValue(it, file, fileConfig).addTo(configs).let { true }
                            else -> true
                        }
                    }
                }
            }
        }
        
        var current: PsiElement = value
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
                        when {
                            option.name == "format" && option.value == "html" -> html = true
                        }
                        if(options == null) options = LinkedList()
                        val resolvedOption = resolveOption(option, file, fileConfig)
                        if(resolvedOption != null) options.addFirst(resolvedOption)
                    } else {
                        val optionValue = current.value
                        if(optionValue != null) {
                            if(optionValues == null) optionValues = LinkedList()
                            val resolvedOptionValue = resolveOptionValue(optionValue, file, fileConfig)
                            optionValues.addFirst(resolvedOptionValue)
                        }
                    }
                }
                current is PsiWhiteSpace || current is PsiComment -> continue
                else -> break
            }
        }
        val documentation = getDocumentation(documentationLines, html)
        
        val config = CwtValueConfig(
            pointer, fileConfig.info, value.value,
            booleanValue, intValue, floatValue, stringValue, configs,
            documentation, options, optionValues
        )
        fileConfig.info.acceptConfigExpression(config.valueExpression, config)
        configs?.forEach { it.parent = config }
        return config
    }
    
    private fun resolveOption(option: CwtOption, file: CwtFile, fileConfig: CwtFileConfig): CwtOptionConfig? {
        val optionValue = option.optionValue ?: return null
        val key = option.name
        val value = optionValue.value
        val valueType: CwtType
        val separatorType = option.separatorType
        var options: List<CwtOptionConfig>? = null
        var optionValues: List<CwtOptionValueConfig>? = null
        when {
            optionValue is CwtBoolean -> {
                valueType = CwtType.Boolean
            }
            optionValue is CwtInt -> {
                valueType = CwtType.Int
            }
            optionValue is CwtFloat -> {
                valueType = CwtType.Float
            }
            optionValue is CwtString -> {
                valueType = CwtType.String
            }
            optionValue is CwtBlock -> {
                valueType = CwtType.Block
                optionValue.forEachChild f@{
                    when {
                        it is CwtOption -> {
                            val resolved = resolveOption(it, file, fileConfig)
                            if(resolved == null) return@f
                            if(options == null) options = SmartList()
                            options!!.asMutable().add(resolved)
                        }
                        it is CwtValue -> {
                            val resolved = resolveOptionValue(it, file, fileConfig)
                            if(optionValues == null) optionValues = SmartList()
                            optionValues!!.asMutable().add(resolved)
                        }
                    }
                }
                if(options == null) options = emptyList()
                if(optionValues == null) optionValues = emptyList()
            }
            else -> {
                valueType = CwtType.Unknown
            }
        }
        return CwtOptionConfig(emptyPointer(), fileConfig.info, key, value, valueType, separatorType, options, optionValues)
    }
    
    private fun resolveOptionValue(optionValue: CwtValue, file: CwtFile, fileConfig: CwtFileConfig): CwtOptionValueConfig {
        val value = optionValue.value
        val valueType: CwtType
        var options: List<CwtOptionConfig>? = null
        var optionValues: List<CwtOptionValueConfig>? = null
        when {
            optionValue is CwtBoolean -> {
                valueType = CwtType.Boolean
            }
            optionValue is CwtInt -> {
                valueType = CwtType.Int
            }
            optionValue is CwtFloat -> {
                valueType = CwtType.Float
            }
            optionValue is CwtString -> {
                valueType = CwtType.String
            }
            optionValue is CwtBlock -> {
                valueType = CwtType.Block
                optionValue.forEachChild f@{
                    when {
                        it is CwtOption -> {
                            val resolved = resolveOption(it, file, fileConfig)
                            if(resolved == null) return@f
                            if(options == null) options = SmartList()
                            options!!.asMutable().add(resolved)
                        }
                        it is CwtValue -> {
                            val resolved = resolveOptionValue(it, file, fileConfig)
                            if(optionValues == null) optionValues = SmartList()
                            optionValues!!.asMutable().add(resolved)
                        }
                    }
                }
                if(options == null) options = emptyList()
                if(optionValues == null) optionValues = emptyList()
            }
            else -> {
                valueType = CwtType.Unknown
            }
        }
        return CwtOptionValueConfig(emptyPointer(), fileConfig.info, value, valueType, options, optionValues)
    }
}
