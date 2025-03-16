package icu.windea.pls.script.editor

import com.intellij.lang.annotation.*
import com.intellij.lang.annotation.HighlightSeverity.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys as Keys

/**
 * 脚本文件的注解器。
 *
 * * 提供定义的特殊颜色高亮。（基于CWT规则）
 * * 提供定义成员的特殊颜色高亮。（基于CWT规则）
 * * 提供特殊标签的特殊颜色高亮。（基于扩展的CWT规则）
 */
class ParadoxScriptAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        checkSyntax(element, holder)

        annotateParameterValue(element, holder)

        when (element) {
            is ParadoxScriptFile -> annotateFile(element, holder)
            is ParadoxScriptProperty -> annotateProperty(element, holder)
            is ParadoxScriptExpressionElement -> annotateExpressionElement(element, holder)
        }
    }

    private fun checkSyntax(element: PsiElement, holder: AnnotationHolder) {
        //不允许紧邻的字面量
        if (element.isLiteral() && element.prevSibling.isLiteral()) {
            holder.newAnnotation(ERROR, PlsBundle.message("neighboring.literal.not.supported"))
                .withFix(InsertStringFix(PlsBundle.message("neighboring.literal.not.supported.fix"), " ", element.startOffset))
                .create()
        }
        //检测是否缺失一侧的双引号
        if (element.isQuoteAware()) {
            val text = element.text
            val isLeftQuoted = text.isLeftQuoted()
            val isRightQuoted = text.isRightQuoted()
            if (!isLeftQuoted && isRightQuoted) {
                holder.newAnnotation(ERROR, PlsBundle.message("missing.opening.quote")).create()
            } else if (isLeftQuoted && !isRightQuoted) {
                holder.newAnnotation(ERROR, PlsBundle.message("missing.closing.quote")).create()
            }
        }
    }

    private fun PsiElement?.isLiteral() = this is ParadoxScriptExpressionElement

    private fun PsiElement?.isQuoteAware() = this is ParadoxScriptStringExpressionElement

    private fun annotateParameterValue(element: PsiElement, holder: AnnotationHolder) {
        val elementType = element.elementType
        if (elementType != ParadoxScriptElementTypes.PARAMETER_VALUE_TOKEN) return
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
        val definitionInfo = file.definitionInfo
        if (definitionInfo != null) annotateDefinition(file, holder, definitionInfo)
    }

    private fun annotateProperty(element: ParadoxScriptProperty, holder: AnnotationHolder) {
        val definitionInfo = element.definitionInfo
        if (definitionInfo != null) annotateDefinition(element, holder, definitionInfo)
    }

    private fun annotateDefinition(element: ParadoxScriptDefinitionElement, holder: AnnotationHolder, definitionInfo: ParadoxDefinitionInfo) {
        if (element is ParadoxScriptProperty) {
            holder.newSilentAnnotation(INFORMATION).range(element.propertyKey).textAttributes(Keys.DEFINITION_KEY).create()
        }
        val nameField = definitionInfo.typeConfig.nameField
        if (nameField != null) {
            //如果存在，高亮定义名对应的字符串（可能还有其他高亮）
            val propertyElement = element.findProperty(nameField) //不处理内联的情况
            val nameElement = propertyElement?.propertyValue<ParadoxScriptString>()
            if (nameElement != null) {
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

    private fun annotateExpressionElement(element: ParadoxScriptExpressionElement, holder: AnnotationHolder) {
        //#131
        if (!element.isResolvableExpression()) return

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
            //高亮特殊标签
            if (element is ParadoxScriptStringExpressionElement && config is CwtValueConfig && config.isTagConfig) {
                holder.newSilentAnnotation(INFORMATION).range(element).textAttributes(Keys.TAG_KEY).create()
                return
            }

            //如果不是字符串，除非是定义引用，否则不作高亮
            if (element !is ParadoxScriptStringExpressionElement && config.expression.type != CwtDataTypes.Definition) {
                return
            }
            //高亮脚本表达式
            annotateExpression(element, holder, config)
        }
    }

    private fun annotateComplexEnumValue(element: ParadoxScriptExpressionElement, holder: AnnotationHolder) {
        //高亮复杂枚举值声明对应的表达式
        holder.newSilentAnnotation(INFORMATION).range(element)
            .textAttributes(Keys.COMPLEX_ENUM_VALUE_KEY)
            .create()
    }

    private fun annotateExpression(element: ParadoxScriptExpressionElement, holder: AnnotationHolder, config: CwtMemberConfig<*>) {
        ParadoxExpressionManager.annotateExpression(element, null, config, holder)
    }
}
