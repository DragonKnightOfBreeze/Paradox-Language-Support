package icu.windea.pls.config.util

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.psi.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.config.internal.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.internal.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.codeInsight.completion.*

object CwtConfigCompletionManager {
    fun initializeContext(parameters: CompletionParameters, context: ProcessingContext, contextElement: PsiElement): Boolean {
        context.parameters = parameters
        
        val configGroup = CwtConfigManager.getContainingConfigGroup(parameters.originalFile) ?: return false
        context.configGroup = configGroup
        
        val quoted = contextElement.text.isLeftQuoted()
        val rightQuoted = contextElement.text.isRightQuoted()
        val offsetInParent = parameters.offset - contextElement.startOffset
        val keyword = contextElement.getKeyword(offsetInParent)
        
        context.contextElement = contextElement
        context.offsetInParent = offsetInParent
        context.keyword = keyword
        context.quoted = quoted
        context.rightQuoted = rightQuoted
        
        context.completionIds = mutableSetOf<String>().synced()
        
        return true
    }
    
    fun addConfigCompletions(context: ProcessingContext, result: CompletionResultSet) {
        val contextElement = context.contextElement ?: return //typing key / value
        val configGroup = context.configGroup ?: return
        
        val containerElement = when {
            contextElement is CwtPropertyKey -> contextElement.parent?.parent
            contextElement is CwtString && contextElement.isPropertyValue() -> contextElement.parent
            contextElement is CwtString/* && contextElement.isBlockValue()*/ -> contextElement.parent
            else -> null
        }
        if(containerElement !is CwtBlockElement && containerElement !is CwtProperty) return
        val containerConfigPath = CwtConfigManager.getConfigPath(containerElement) ?: return
        
        val schema = configGroup.schemas.firstOrNull() ?: return
        var configsToMatch = mutableListOf<CwtMemberConfig<*>>()
        configsToMatch += schema.properties
        var isRoot = true
        containerConfigPath.forEach f1@{ path ->
            when {
                isRoot -> isRoot = false
                else -> configsToMatch = configsToMatch.flatMapTo(mutableListOf()) { it.configs.orEmpty() }
            }
            val nextConfigsToMatch = mutableListOf<CwtMemberConfig<*>>()
            configsToMatch.forEach f2@{ config ->
                when(config) {
                    is CwtPropertyConfig -> {
                        val schemaExpression = CwtSchemaExpression.resolve(config.key)
                        if(!matchesSchemaExpression(path, schemaExpression, schema)) return@f2
                        nextConfigsToMatch += config
                    }
                    is CwtValueConfig -> {
                        if(path != "-") return@f2
                        nextConfigsToMatch += config
                    }
                }
            }
            configsToMatch = nextConfigsToMatch
        }
        if(configsToMatch.isEmpty()) return
        
        val completeKey = contextElement is CwtPropertyKey || contextElement is CwtString && contextElement.isBlockValue()
        val completeKeyWithValue = contextElement is CwtString && contextElement.isBlockValue()
        val completeBlockValue = contextElement is CwtString && contextElement.isBlockValue()
        val completePropertyValue = contextElement is CwtString && contextElement.isPropertyValue()
        
        configsToMatch.forEach { config ->
            when(config) {
                is CwtPropertyConfig -> {
                    if(completeKey) {
                        val schemaExpression = CwtSchemaExpression.resolve(config.key)
                        completeBySchemaExpression(context, schemaExpression, schema, config) { lookupElement ->
                            result.addElement(lookupElement)
                            true
                        }
                    } else if(completePropertyValue) {
                        val schemaExpression = CwtSchemaExpression.resolve(config.value)
                        completeBySchemaExpression(context, schemaExpression, schema, config) { lookupElement ->
                            result.addElement(lookupElement)
                            true
                        }
                    }
                }
                is CwtValueConfig -> {
                    if(completeBlockValue) {
                        val schemaExpression = CwtSchemaExpression.resolve(config.value)
                        completeBySchemaExpression(context, schemaExpression, schema, config) { lookupElement ->
                            result.addElement(lookupElement)
                            true
                        }
                    }
                }
            }
        }
    }
    
    fun matchesSchemaExpression(value: String, schemaExpression: CwtSchemaExpression, schema: CwtSchemaConfig): Boolean {
        return when(schemaExpression) {
            is CwtSchemaExpression.Constant -> {
                schemaExpression.expressionString == value
            }
            is CwtSchemaExpression.Enum -> {
                schema.enums[schemaExpression.name]?.values?.any { it.stringValue == value } ?: false
            }
            is CwtSchemaExpression.Template -> {
                schemaExpression.pattern.matchesPattern(value)
            }
            is CwtSchemaExpression.Type -> {
                true //TODO 1.3.19+ 
            }
        }
    }
    
    fun completeBySchemaExpression(
        context: ProcessingContext,
        schemaExpression: CwtSchemaExpression,
        schema: CwtSchemaConfig,
        config: CwtMemberConfig<*>,
        processor: Processor<LookupElement>
    ): Boolean {
        val icon = when(config) {
            is CwtPropertyConfig -> PlsIcons.Nodes.Property
            is CwtValueConfig -> PlsIcons.Nodes.Value
        }
        val element = config.pointer
        val typeFile = element.containingFile
        val p = if(config is CwtValueConfig) "#" else ""
        return when(schemaExpression) {
            is CwtSchemaExpression.Constant -> {
                val v = schemaExpression.expressionString
                if(!shouldComplete(context, p + v)) return true
                val lookupElement = LookupElementBuilder.create(element, v)
                    .withIcon(icon)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                processor.process(lookupElement)
            }
            is CwtSchemaExpression.Enum -> {
                val tailText = " by ${schemaExpression}"
                schema.enums[schemaExpression.name]?.values?.forEach {
                    val v = it.stringValue ?: return@forEach
                    if(!shouldComplete(context, p + v)) return true
                    val lookupElement = LookupElementBuilder.create(element, v)
                        .withIcon(icon)
                        .withTypeText(typeFile?.name, typeFile?.icon, true)
                        .withTailText(tailText)
                    processor.process(lookupElement)
                }
                true
            }
            is CwtSchemaExpression.Template -> {
                true //TODO 1.3.18+
            }
            is CwtSchemaExpression.Type -> {
                true //TODO 1.3.19+
            }
        }
    }
    
    private fun shouldComplete(context: ProcessingContext, id: String): Boolean {
        return context.completionIds?.add(id) != false
    }
}
