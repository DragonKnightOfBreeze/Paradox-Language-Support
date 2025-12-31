package icu.windea.pls.lang.editor

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.startOffset
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.lang.complexEnumValueInfo
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxPsiMatcher
import icu.windea.pls.lang.psi.findProperty
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.tagType
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isResolvableExpression
import icu.windea.pls.script.psi.propertyValue

class ParadoxScriptAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is ParadoxScriptFile -> annotateFile(element, holder)
            is ParadoxScriptProperty -> annotateProperty(element, holder)
            is ParadoxScriptExpressionElement -> annotateExpressionElement(element, holder)
        }
    }

    private fun annotateFile(file: ParadoxScriptFile, holder: AnnotationHolder) {
        // 高亮定义声明
        val definitionInfo = file.definitionInfo
        if (definitionInfo != null) annotateDefinition(file, holder, definitionInfo)
    }

    private fun annotateProperty(element: ParadoxScriptProperty, holder: AnnotationHolder) {
        val gameType = selectGameType(element) ?: return
        // 高亮内联脚本用法 - `inline_script = ...` 中的 `inline_script`
        if (annotateInlineScriptUsage(element, holder, gameType)) return
        // 高亮定义注入表达式 - `inject:some_definition = {...}` 中的 `inject:some_definition`（以及使用其他合法前缀的情况）
        if (annotateDefinitionInjectionExpression(element, holder, gameType)) return

        // 高亮定义声明
        val definitionInfo = element.definitionInfo
        if (definitionInfo != null) annotateDefinition(element, holder, definitionInfo)
    }

    private fun annotateExpressionElement(element: ParadoxScriptExpressionElement, holder: AnnotationHolder) {
        // #131
        if (!element.isResolvableExpression()) return

        // 高亮特殊标签
        if (annotateTag(element, holder)) return
        // 高亮复杂枚举值声明
        if (annotateComplexEnumValue(element, holder)) return

        val isKey = element is ParadoxScriptPropertyKey
        val config = ParadoxExpressionManager.getConfigs(element, orDefault = isKey).firstOrNull()
        if (config != null) {
            // 如果不是字符串，除非是定义引用，否则不作高亮
            if (element !is ParadoxScriptStringExpressionElement && config.configExpression.type != CwtDataTypes.Definition) return

            // 高亮脚本表达式
            annotateExpression(element, holder, config)
            return
        }
    }

    private fun annotateDefinition(element: ParadoxScriptDefinitionElement, holder: AnnotationHolder, definitionInfo: ParadoxDefinitionInfo) {
        if (element is ParadoxScriptProperty) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.propertyKey).textAttributes(ParadoxScriptAttributesKeys.DEFINITION_KEY).create()
        }
        val nameField = definitionInfo.typeConfig.nameField
        if (nameField != null) {
            // 如果存在，高亮定义名对应的字符串（可能还有其他高亮）
            val propertyElement = element.findProperty(nameField) // 不处理内联的情况
            val nameElement = propertyElement?.propertyValue<ParadoxScriptString>()
            if (nameElement != null) {
                val nameString = definitionInfo.name.escapeXml().or.anonymous()
                val typesString = definitionInfo.typesText
                // 这里不能使用PSI链接
                val tooltip = "<pre>(definition name) <b>$nameString</b>: $typesString</pre>"
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(nameElement).tooltip(tooltip).textAttributes(ParadoxScriptAttributesKeys.DEFINITION_NAME_KEY).create()
            }
        }
    }

    private fun annotateExpression(element: ParadoxScriptExpressionElement, holder: AnnotationHolder, config: CwtMemberConfig<*>) {
        ParadoxExpressionManager.annotateScriptExpression(element, null, holder, config)
    }

    private fun annotateComplexEnumValue(element: ParadoxScriptExpressionElement, holder: AnnotationHolder): Boolean {
        if (element !is ParadoxScriptStringExpressionElement) return false
        if (element.complexEnumValueInfo == null) return false
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element)
            .textAttributes(ParadoxScriptAttributesKeys.COMPLEX_ENUM_VALUE_KEY)
            .create()
        return true
    }

    private fun annotateTag(element: ParadoxScriptExpressionElement, holder: AnnotationHolder): Boolean {
        // 目前不在这里显示标签类型，而是在快速文档中
        if (element !is ParadoxScriptString) return false
        if (element.tagType == null) return false
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element).textAttributes(ParadoxScriptAttributesKeys.TAG_KEY).create()
        return true
    }

    private fun annotateInlineScriptUsage(element: ParadoxScriptProperty, holder: AnnotationHolder, gameType: ParadoxGameType): Boolean {
        if (!ParadoxPsiMatcher.isInlineScriptUsage(element, gameType)) return false
        val name = element.name
        val offset = element.startOffset + ParadoxExpressionManager.getExpressionOffset(element.propertyKey)
        val r1 = TextRange.from(offset, name.length)
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(r1).textAttributes(ParadoxScriptAttributesKeys.INLINE_KEY).create()
        return true
    }

    private fun annotateDefinitionInjectionExpression(element: ParadoxScriptProperty, holder: AnnotationHolder, gameType: ParadoxGameType): Boolean {
        if (!ParadoxPsiMatcher.isDefinitionInjection(element, gameType)) return false
        val name = element.name
        if (name.isParameterized()) return false // 忽略带参数的情况
        val mode = ParadoxDefinitionInjectionManager.getModeFromExpression(name)
        if (mode.isEmpty()) return false
        val target = ParadoxDefinitionInjectionManager.getTargetFromExpression(name)
        val offset = element.startOffset + ParadoxExpressionManager.getExpressionOffset(element.propertyKey)
        val r1 = TextRange.from(offset, mode.length)
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(r1).textAttributes(ParadoxScriptAttributesKeys.MACRO_KEY).create()
        val r2 = TextRange.from(offset + mode.length, 1)
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(r2).textAttributes(ParadoxScriptAttributesKeys.MARKER_KEY).create()
        if (target.isEmpty()) return true
        val r3 = TextRange.from(offset + mode.length + 1, target.length)
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(r3).textAttributes(ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY).create()
        return true
    }
}
