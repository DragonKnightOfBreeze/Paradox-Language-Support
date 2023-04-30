package icu.windea.pls.script.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import com.intellij.util.text.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.inspections.inference.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.highlighter.ParadoxScriptAttributesKeys as Keys

/**
 * 脚本文件的注解器。
 *
 * * 提供定义的特殊颜色高亮。（基于CWT规则）
 * * 提供定义成员的特殊颜色高亮。（基于CWT规则）
 * * 提供特殊标签的特殊颜色高亮。（基于扩展的CWT规则）
 */
class ParadoxScriptAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when(element) {
            is ParadoxScriptFile -> annotateFile(element, holder)
            is ParadoxScriptProperty -> annotateProperty(element, holder)
            is ParadoxScriptStringExpressionElement -> annotateExpressionElement(element, holder)
            is ParadoxScriptInt -> annotateExpressionElement(element, holder)
        }
    }
    
    private fun annotateFile(file: ParadoxScriptFile, holder: AnnotationHolder) {
        annotateInlineScriptFile(file, holder)
    }
    
    private fun annotateInlineScriptFile(file: ParadoxScriptFile, holder: AnnotationHolder) {
        if(!getSettings().inference.inlineScriptLocation) return
        val expression = ParadoxInlineScriptHandler.getInlineScriptExpression(file)
        if(expression != null) {
            val usageInfo = ParadoxInlineScriptHandler.getInlineScriptUsageInfo(file)
            if(usageInfo != null && !usageInfo.hasConflict) {
                val message = PlsBundle.message("script.annotator.inlineScript", expression)
                holder.newAnnotation(INFORMATION, message).fileLevel().withFix(GotoInlineScriptUsagesIntention()).create()
            }
        }
    }
    
    private fun annotateProperty(element: ParadoxScriptProperty, holder: AnnotationHolder) {
        val definitionInfo = element.definitionInfo
        if(definitionInfo != null) return annotateDefinition(element, holder, definitionInfo)
    }
    
    private fun annotateDefinition(element: ParadoxScriptProperty, holder: AnnotationHolder, definitionInfo: ParadoxDefinitionInfo) {
        holder.newSilentAnnotation(INFORMATION).range(element.propertyKey).textAttributes(Keys.DEFINITION_KEY).create()
        val nameField = definitionInfo.typeConfig.nameField
        if(nameField != null) {
            //如果存在，高亮定义名对应的字符串（可能还有其他高亮）
            val propertyElement = element.findProperty(nameField) //不处理内联的情况
            val nameElement = propertyElement?.propertyValue<ParadoxScriptString>()
            if(nameElement != null) {
                val nameString = definitionInfo.name.escapeXml().orAnonymous()
                val typesString = definitionInfo.typesText
                //这里不能使用PSI链接
                val tooltip = PlsBundle.message("script.annotator.definitionName", nameString, typesString)
                holder.newSilentAnnotation(INFORMATION).range(nameElement)
                    .tooltip(tooltip)
                    .textAttributes(Keys.DEFINITION_NAME_KEY)
                    .create()
            }
        }
    }
    
    private fun annotateComplexEnumValue(element: ParadoxScriptExpressionElement, holder: AnnotationHolder) {
        //高亮复杂枚举值声明对应的表达式
        holder.newSilentAnnotation(INFORMATION).range(element)
            .textAttributes(Keys.COMPLEX_ENUM_VALUE_KEY)
            .create()
    }
    
    private fun annotateExpressionElement(element: ParadoxScriptExpressionElement, holder: AnnotationHolder) {
        //检查是否缺失引号
        if(element is ParadoxScriptStringExpressionElement) {
            checkLiteralElement(element, holder)
        }
        
        //高亮复杂枚举值声明
        if(element is ParadoxScriptStringExpressionElement) {
            val complexEnumValueInfo = element.complexEnumValueInfo
            if(complexEnumValueInfo != null) {
                annotateComplexEnumValue(element, holder)
                return
            }
        }
        
        val isKey = element is ParadoxScriptPropertyKey
        val config = ParadoxConfigHandler.getConfigs(element, !isKey, isKey).firstOrNull()
        if(config != null) {
            //高亮特殊标签
            if(config is CwtValueConfig && config.isTagConfig) {
                holder.newSilentAnnotation(INFORMATION).range(element).textAttributes(Keys.TAG_KEY).create()
                return
            }
            //如果不是字符串，除非是定义引用，否则不作高亮
            if(element !is ParadoxScriptStringExpressionElement && config.expression.type != CwtDataType.Definition) {
                return
            }
            //缓存参数范围
            if(element is ParadoxScriptStringExpressionElement) {
                val elementText = element.text
                if(elementText.surroundsWith('$', '$')) return //整个是参数的情况，不需要进行高亮
                if(elementText.contains('$')) setParameterRanges(element) //缓存参数文本范围
            }
            annotateExpression(element, null, holder, config)
        }
    }
    
    private fun checkLiteralElement(element: PsiElement, holder: AnnotationHolder) {
        val text = element.text
        if(text.isLeftQuoted() && !text.isRightQuoted()) {
            //missing closing quote
            holder.newAnnotation(ERROR, PlsBundle.message("syntax.error.missing.closing.quote")).create()
        }
    }
    
    private fun annotateExpression(
        element: ParadoxScriptExpressionElement,
        rangeInElement: TextRange?,
        holder: AnnotationHolder,
        config: CwtConfig<*>
    ) {
        val configExpression = config.expression ?: return
        val text = element.text
        val expression = rangeInElement?.substring(text) ?: element.value
        
        ParadoxScriptExpressionSupport.annotate(element, rangeInElement, expression, holder, config)
        
        when {
            configExpression.type.isValueSetValueType() -> {
                //not key/value or quoted -> only value set value name, no scope info
                if(config !is CwtDataConfig<*> || expression.isLeftQuoted()) {
                    val valueSetName = config.expression?.value ?: return
                    val attributesKey = when(valueSetName) {
                        "variable" -> Keys.VARIABLE_KEY
                        else -> Keys.VALUE_SET_VALUE_KEY
                    }
                    doHighlightScriptExpression(element, element.textRange.unquote(text), attributesKey, holder)
                    return
                }
                val configGroup = config.info.configGroup
                val isKey = element is ParadoxScriptPropertyKey
                val textRange = TextRange.create(0, expression.length)
                val valueSetValueExpression = ParadoxValueSetValueExpression.resolve(expression, textRange, config, configGroup, isKey) ?: return
                annotateComplexExpression(element, valueSetValueExpression, holder, config)
            }
            configExpression.type.isScopeFieldType()-> {
                if(expression.isLeftQuoted()) return
                val configGroup = config.info.configGroup
                val isKey = element is ParadoxScriptPropertyKey
                val textRange = TextRange.create(0, expression.length)
                val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expression, textRange, configGroup, isKey) ?: return
                annotateComplexExpression(element, scopeFieldExpression, holder, config)
            }
            configExpression.type.isValueFieldType()-> {
                if(expression.isLeftQuoted()) return
                val configGroup = config.info.configGroup
                val isKey = element is ParadoxScriptPropertyKey
                val textRange = TextRange.create(0, expression.length)
                val valueFieldExpression = ParadoxValueFieldExpression.resolve(expression, textRange, configGroup, isKey) ?: return
                annotateComplexExpression(element, valueFieldExpression, holder, config)
            }
            configExpression.type.isVariableFieldType() -> {
                if(expression.isLeftQuoted()) return
                val configGroup = config.info.configGroup
                val isKey = element is ParadoxScriptPropertyKey
                val textRange = TextRange.create(0, expression.length)
                val variableFieldExpression = ParadoxVariableFieldExpression.resolve(expression, textRange, configGroup, isKey) ?: return
                annotateComplexExpression(element, variableFieldExpression, holder, config)
            }
            else -> return
        }
    }
    
    private fun annotateComplexExpression(element: ParadoxScriptExpressionElement, expression: ParadoxComplexExpression, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(element !is ParadoxScriptStringExpressionElement) return
        doAnnotateComplexExpression(element, expression, holder, config)
    }
    
    private fun doAnnotateComplexExpression(element: ParadoxScriptStringExpressionElement, expressionNode: ParadoxExpressionNode, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = expressionNode.getAttributesKey()
        if(attributesKey != null) {
            if(expressionNode is ParadoxTokenExpressionNode) {
                //override default highlight by highlighter (property key or string)
                holder.newSilentAnnotation(INFORMATION).textAttributes(HighlighterColors.TEXT).create()
            }
            val rangeToAnnotate = expressionNode.rangeInExpression.shiftRight(element.textRange.unquote(element.text).startOffset)
            doHighlightScriptExpression(element, rangeToAnnotate, attributesKey, holder)
        }
        val attributesKeyConfig = expressionNode.getAttributesKeyConfig(element)
        if(attributesKeyConfig != null) {
            val rangeInElement = expressionNode.rangeInExpression.shiftRight(if(element.text.isLeftQuoted()) 1 else 0)
            annotateExpression(element, rangeInElement, holder, attributesKeyConfig)
        }
        if(expressionNode.nodes.isNotEmpty()) {
            for(node in expressionNode.nodes) {
                doAnnotateComplexExpression(element, node, holder, config)
            }
        }
    }
    
    private fun doHighlightScriptExpression(element: ParadoxScriptExpressionElement, range: TextRange, attributesKey: TextAttributesKey, holder: AnnotationHolder) {
        if(element !is ParadoxScriptStringExpressionElement) {
            holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
            return
        }
        //进行特殊代码高亮时，可能需要跳过字符串表达式中的参数部分
        val parameterRanges = getParameterRanges(element)
        if(parameterRanges.isEmpty()) {
            holder.newSilentAnnotation(INFORMATION).range(range).textAttributes(attributesKey).create()
        } else {
            val finalRanges = TextRangeUtil.excludeRanges(range, parameterRanges)
            finalRanges.forEach { r ->
                if(!r.isEmpty) {
                    holder.newSilentAnnotation(INFORMATION).range(r).textAttributes(attributesKey).create()
                }
            }
        }
    }
    
    private fun setParameterRanges(element: ParadoxScriptStringExpressionElement) {
        var parameterRanges: SmartList<TextRange>? = null
        element.processChild { parameter ->
            if(parameter is ParadoxParameter) {
                if(parameterRanges == null) parameterRanges = SmartList()
                parameterRanges?.add(parameter.textRange)
            }
            true
        }
        element.putUserData(PlsKeys.parameterRangesKey, parameterRanges.orEmpty())
    }
    
    private fun getParameterRanges(element: ParadoxScriptStringExpressionElement): List<TextRange> {
        return element.getUserData(PlsKeys.parameterRangesKey).orEmpty()
    }
}
