package icu.windea.pls.script.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.CwtConfigHandler.getParameterRanges
import icu.windea.pls.lang.config.*
import icu.windea.pls.model.*
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
        val elementType = element.elementType
        if(elementType == ParadoxScriptElementTypes.SNIPPET_TOKEN) return annotateSnippetToken(element, holder)
        
        when(element) {
            is ParadoxScriptFile -> annotateFile(element, holder)
            is ParadoxScriptProperty -> annotateProperty(element, holder)
            is ParadoxScriptStringExpressionElement -> annotateExpressionElement(element, holder)
            is ParadoxScriptInt -> annotateExpressionElement(element, holder)
        }
    }
    
    private fun annotateSnippetToken(element: PsiElement, holder: AnnotationHolder) {
        val templateElement = element.parent?.parent ?: return
        val attributesKey = when {
            element.text.startsWith("@") -> Keys.SCRIPTED_VARIABLE_KEY
            templateElement is ParadoxScriptPropertyKey -> Keys.PROPERTY_KEY_KEY
            templateElement is ParadoxScriptString -> Keys.STRING_KEY
            templateElement is ParadoxScriptScriptedVariableName -> Keys.SCRIPTED_VARIABLE_KEY
            templateElement is ParadoxScriptScriptedVariableReference -> Keys.SCRIPTED_VARIABLE_KEY
            else -> return
        }
        holder.newSilentAnnotation(INFORMATION).range(element)
            .textAttributes(attributesKey)
            .create()
    }
    
    private fun annotateFile(file: ParadoxScriptFile, holder: AnnotationHolder) {
        annotateInlineScriptFile(file, holder)
        val definitionInfo = file.definitionInfo
        if(definitionInfo != null) annotateDefinition(file, holder, definitionInfo)
    }
    
    private fun annotateInlineScriptFile(file: ParadoxScriptFile, holder: AnnotationHolder) {
        if(!getSettings().inference.inlineScriptConfig) return
        val inlineScriptExpression = ParadoxInlineScriptHandler.getInlineScriptExpression(file) ?: return
        val configContext = CwtConfigHandler.getConfigContext(file) ?: return
        if(configContext.inlineScriptHasConflict == true) return
        if(configContext.inlineScriptHasRecursion == true) return
        val message = PlsBundle.message("script.annotator.inlineScript", inlineScriptExpression)
        holder.newAnnotation(INFORMATION, message).fileLevel().withFix(GotoInlineScriptUsagesIntention()).create()
    }
    
    private fun annotateProperty(element: ParadoxScriptProperty, holder: AnnotationHolder) {
        val definitionInfo = element.definitionInfo
        if(definitionInfo != null) annotateDefinition(element, holder, definitionInfo)
    }
    
    private fun annotateDefinition(element: ParadoxScriptDefinitionElement, holder: AnnotationHolder, definitionInfo: ParadoxDefinitionInfo) {
        if(element is ParadoxScriptProperty) {
            holder.newSilentAnnotation(INFORMATION).range(element.propertyKey).textAttributes(Keys.DEFINITION_KEY).create()
        }
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
        val config = CwtConfigHandler.getConfigs(element, orDefault = isKey).firstOrNull()
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
                if(elementText.contains('$')) {
                    val parameterRanges = getParameterRanges(element)
                    //缓存参数文本范围
                    element.putUserData(PlsKeys.parameterRanges, parameterRanges)
                    //如果参数直接作为整个脚本表达式，不需要进行额外的高亮
                    if(parameterRanges.singleOrNull()?.length == elementText.unquote().length) {
                        return
                    }
                }
            }
            CwtConfigHandler.annotateScriptExpression(element, null, config, holder)
        }
    }
    
    private fun checkLiteralElement(element: PsiElement, holder: AnnotationHolder) {
        val text = element.text
        if(text.isLeftQuoted() && !text.isRightQuoted()) {
            //missing closing quote
            holder.newAnnotation(ERROR, PlsBundle.message("syntax.error.missing.closing.quote")).create()
        }
    }
}
