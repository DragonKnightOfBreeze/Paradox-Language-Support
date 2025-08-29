package icu.windea.pls.lang.editor

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.lang.complexEnumValueInfo
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.findProperty
import icu.windea.pls.script.psi.isResolvableExpression
import icu.windea.pls.script.psi.propertyValue
import icu.windea.pls.script.psi.tagType

class ParadoxScriptAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is ParadoxScriptFile -> annotateFile(element, holder)
            is ParadoxScriptProperty -> annotateProperty(element, holder)
            is ParadoxScriptExpressionElement -> annotateExpressionElement(element, holder)
        }
    }

    private fun annotateFile(file: ParadoxScriptFile, holder: AnnotationHolder) {
        val definitionInfo = file.definitionInfo
        if (definitionInfo != null) annotateDefinition(file, holder, definitionInfo)
    }

    private fun annotateProperty(element: ParadoxScriptProperty, holder: AnnotationHolder) {
        val definitionInfo = element.definitionInfo
        if (definitionInfo != null) annotateDefinition(element, holder, definitionInfo)
    }

    private fun annotateDefinition(element: ParadoxScriptDefinitionElement, holder: AnnotationHolder, definitionInfo: ParadoxDefinitionInfo) {
        if (element is ParadoxScriptProperty) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element.propertyKey).textAttributes(ParadoxScriptAttributesKeys.DEFINITION_KEY).create()
        }
        val nameField = definitionInfo.typeConfig.nameField
        if (nameField != null) {
            //如果存在，高亮定义名对应的字符串（可能还有其他高亮）
            val propertyElement = element.findProperty(nameField) //不处理内联的情况
            val nameElement = propertyElement?.propertyValue<ParadoxScriptString>()
            if (nameElement != null) {
                val nameString = definitionInfo.name.escapeXml().or.anonymous()
                val typesString = definitionInfo.typesText
                //这里不能使用PSI链接
                val tooltip = "<pre>(definition name) <b>$nameString</b>: $typesString</pre>"
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(nameElement).tooltip(tooltip).textAttributes(ParadoxScriptAttributesKeys.DEFINITION_NAME_KEY).create()
            }
        }
    }

    private fun annotateExpressionElement(element: ParadoxScriptExpressionElement, holder: AnnotationHolder) {
        //#131
        if (!element.isResolvableExpression()) return

        //高亮特殊标签
        run {
            if (element !is ParadoxScriptString) return@run
            val tagType = element.tagType()
            if (tagType == null) return@run
            annotateTag(element, holder)
            return
        }

        //高亮复杂枚举值声明
        run {
            if (element !is ParadoxScriptStringExpressionElement) return@run
            if (element.complexEnumValueInfo == null) return@run
            annotateComplexEnumValue(element, holder)
            return
        }

        val isKey = element is ParadoxScriptPropertyKey
        val config = ParadoxExpressionManager.getConfigs(element, orDefault = isKey).firstOrNull()
        if (config != null) {
            //如果不是字符串，除非是定义引用，否则不作高亮
            if (element !is ParadoxScriptStringExpressionElement && config.configExpression.type != CwtDataTypes.Definition) return

            //高亮脚本表达式
            annotateExpression(element, holder, config)
            return
        }
    }

    private fun annotateTag(element: ParadoxScriptString, holder: AnnotationHolder) {
        //目前不在这里显示标签类型，而是在快速文档中
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element).textAttributes(ParadoxScriptAttributesKeys.TAG_KEY).create()
    }

    private fun annotateComplexEnumValue(element: ParadoxScriptExpressionElement, holder: AnnotationHolder) {
        //高亮复杂枚举值声明对应的表达式
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element)
            .textAttributes(ParadoxScriptAttributesKeys.COMPLEX_ENUM_VALUE_KEY)
            .create()
    }

    private fun annotateExpression(element: ParadoxScriptExpressionElement, holder: AnnotationHolder, config: CwtMemberConfig<*>) {
        ParadoxExpressionManager.annotateScriptExpression(element, null, holder, config)
    }
}
