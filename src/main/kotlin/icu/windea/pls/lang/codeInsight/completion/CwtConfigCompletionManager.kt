package icu.windea.pls.lang.codeInsight.completion

import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.config.internal.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.internal.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.codeStyle.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

object CwtConfigCompletionManager {
    //region Predefined Lookup Elements
    val yesLookupElement = LookupElementBuilder.create("yes").bold()
        .withPriority(CwtConfigCompletionPriorities.keyword)
        .withCompletionId()
    
    val noLookupElement = LookupElementBuilder.create("no").bold()
        .withPriority(CwtConfigCompletionPriorities.keyword)
        .withCompletionId()
    
    val blockLookupElement = LookupElementBuilder.create("")
        .withPresentableText("{...}")
        .withInsertHandler { c, _ ->
            val editor = c.editor
            val customSettings = CodeStyle.getCustomSettings(c.file, CwtCodeStyleSettings::class.java)
            val spaceWithinBraces = customSettings.SPACE_WITHIN_BRACES
            val text = if(spaceWithinBraces) "{  }" else "{}"
            val length = if(spaceWithinBraces) text.length - 2 else text.length - 1
            EditorModificationUtil.insertStringAtCaret(editor, text, false, true, length)
        }
        .withPriority(CwtConfigCompletionPriorities.keyword)
        .withCompletionId()
    //endregion
    
    //region Core Methods
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
        val schema = configGroup.schemas.firstOrNull() ?: return
        val contextConfigs = CwtConfigManager.getContextConfigs(contextElement, containerElement, schema)
        if(contextConfigs.isEmpty()) return
        
        val isKey = contextElement is CwtPropertyKey || contextElement is CwtString && contextElement.isBlockValue()
        val isBlockValue = contextElement is CwtString && contextElement.isBlockValue()
        val isPropertyValue = contextElement is CwtString && contextElement.isPropertyValue()
        
        val contextConfigsGroup = contextConfigs.groupBy { config ->
            when(config) {
                is CwtPropertyConfig -> config.key
                is CwtValueConfig -> config.value
            }
        }
        contextConfigsGroup.forEach f1@{ (_, configs) ->
            val filteredConfigs = mutableListOf<CwtMemberConfig<*>>()
            configs.find { it is CwtValueConfig }?.also { filteredConfigs += it }
            configs.find { it is CwtPropertyConfig && it.valueType != CwtType.Block }?.also { filteredConfigs += it }
            configs.find { it is CwtPropertyConfig && it.valueType == CwtType.Block }?.also { filteredConfigs += it }
            
            filteredConfigs.forEach f2@{ config ->
                val isBlock = config.valueType == CwtType.Block
                when(config) {
                    is CwtPropertyConfig -> {
                        if(isKey) {
                            val schemaExpression = CwtSchemaExpression.resolve(config.key)
                            completeBySchemaExpression(schemaExpression, schema, config) {
                                val lookupElement = it.forConfig(context, config, schemaExpression)
                                result.addElement(lookupElement, context)
                                true
                            }
                        } else if(isPropertyValue) {
                            if(isBlock) {
                                result.addElement(blockLookupElement, context)
                                return@f2
                            }
                            val schemaExpression = CwtSchemaExpression.resolve(config.value)
                            completeBySchemaExpression(schemaExpression, schema, config) {
                                val lookupElement = it.forConfig(context, config, schemaExpression)
                                result.addElement(lookupElement, context)
                                true
                            }
                        }
                    }
                    is CwtValueConfig -> {
                        if(isBlockValue) {
                            if(isBlock) {
                                result.addElement(blockLookupElement, context)
                                return@f2
                            }
                            val schemaExpression = CwtSchemaExpression.resolve(config.value)
                            completeBySchemaExpression(schemaExpression, schema, config) {
                                val lookupElement = it.forConfig(context, config, schemaExpression)
                                result.addElement(lookupElement, context)
                                true
                            }
                        }
                    }
                }
            }
        }
    }
    
    fun completeBySchemaExpression(
        schemaExpression: CwtSchemaExpression,
        schema: CwtSchemaConfig,
        config: CwtMemberConfig<*>,
        processor: Processor<LookupElementBuilder>
    ): Boolean {
        val icon = when(config) {
            is CwtPropertyConfig -> PlsIcons.Nodes.Property
            is CwtValueConfig -> PlsIcons.Nodes.Value
        }
        return when(schemaExpression) {
            is CwtSchemaExpression.Constant -> {
                val element = config.pointer
                val typeFile = element.containingFile
                val v = schemaExpression.expressionString
                val lookupElement = LookupElementBuilder.create(element, v)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withIcon(icon)
                    .withPriority(CwtConfigCompletionPriorities.constant)
                processor.process(lookupElement)
            }
            is CwtSchemaExpression.Enum -> {
                val enumName = schemaExpression.name
                val tailText = " by ${schemaExpression}"
                schema.enums[enumName]?.values?.forEach {
                    val element = it.pointer
                    val typeFile = element.containingFile
                    val v = it.stringValue ?: return@forEach
                    val lookupElement = LookupElementBuilder.create(element, v)
                        .withTypeText(typeFile?.name, typeFile?.icon, true)
                        .withIcon(icon)
                        .withPriority(CwtConfigCompletionPriorities.enumValue)
                        .withPatchableTailText(tailText)
                    processor.process(lookupElement)
                }
                true
            }
            is CwtSchemaExpression.Template -> {
                val tailText = " (template)"
                val element = config.pointer
                val typeFile = element.containingFile
                val v = schemaExpression.expressionString
                val lookupElement = LookupElementBuilder.create(element, v)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withIcon(icon)
                    .withPatchableTailText(tailText)
                processor.process(lookupElement)
            }
            is CwtSchemaExpression.Type -> {
                val typeName = schemaExpression.name
                if(typeName == "bool" || typeName == "scalar" || typeName == "any") {
                    processor.process(yesLookupElement)
                    processor.process(noLookupElement)
                }
                if(typeName == "any") {
                    processor.process(blockLookupElement)
                }
                //TODO 1.3.19+
                true
            }
        }
    }
    //endregion
}
