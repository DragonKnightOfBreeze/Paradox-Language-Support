package icu.windea.pls.lang.codeInsight.type

import com.intellij.psi.PsiElement
import com.intellij.psi.util.parents
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.ParadoxCsvPsiService
import icu.windea.pls.lang.ParadoxLanguage
import icu.windea.pls.lang.complexEnumValueInfo
import icu.windea.pls.lang.defineInfo
import icu.windea.pls.lang.definitionCandidateInfo
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.overrides.ParadoxOverrideService
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.psi.isDefinitionName
import icu.windea.pls.lang.psi.resolveLocalisation
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxCsvManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.model.type.ParadoxType
import icu.windea.pls.model.type.ParadoxTypeResolver
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptInlineMathNumber
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptValue

object ParadoxTypeManager {
    fun isTypedElement(element: PsiElement): Boolean {
        if (element.language !is ParadoxLanguage) return false
        return when (element) {
            is ParadoxExpressionElement -> true
            is ParadoxScriptScriptedVariable -> true
            is ParadoxScriptedVariableReference -> true
            is ParadoxScriptInlineMathNumber -> true
            is ParadoxLocalisationProperty -> true
            is ParadoxParameter -> true
            is ParadoxConditionParameter -> true
            is ParadoxLocalisationParameter -> true
            else -> false
        }
    }

    fun findTypedElements(element: PsiElement): List<PsiElement> {
        if (element.language !is ParadoxLanguage) return emptyList()
        val typedElement = element.parents(withSelf = true).find { isTypedElement(it) }
        return typedElement.to.singletonListOrEmpty()
    }

    /**
     * 依次尝试导航到：
     * - 定义的类型规则
     * - 定义名对应的定义的类型规则
     * - 对应的枚举规则
     * - 对应的复杂枚举规则
     * - 对应的预定义的动态值规则
     */
    fun findTypeDeclarations(element: PsiElement): List<PsiElement> {
        // 注意这里的 element 是解析引用后得到的 [element] 元素，因此无法定位到定义成员对应的规则声明
        when {
            element is ParadoxScriptProperty -> {
                val definitionInfo = element.definitionInfo
                if (definitionInfo != null) {
                    if (definitionInfo.types.size == 1) {
                        return definitionInfo.typeConfig.pointer.element.to.singletonListOrEmpty()
                    } else {
                        // 这里的 element 可能是 null，以防万一，需要处理是 null 的情况
                        return buildList {
                            definitionInfo.typeConfig.pointer.element?.let { add(it) }
                            definitionInfo.subtypeConfigs.forEach { subtypeConfig ->
                                subtypeConfig.pointer.element?.let { add(it) }
                            }
                        }
                    }
                }
            }
            element is ParadoxScriptExpressionElement -> {
                if (element is ParadoxScriptPropertyKey) {
                    return findTypeDeclarations(element.parent)
                } else if (element is ParadoxScriptValue && element.isDefinitionName()) {
                    val definition = selectScope { element.parentDefinition() }
                    if (definition is ParadoxScriptProperty) return findTypeDeclarations(definition)
                }

                val complexEnumValueInfo = element.complexEnumValueInfo
                if (complexEnumValueInfo != null) {
                    val resolved = complexEnumValueInfo.config.pointer.element
                    return resolved.to.singletonListOrEmpty()
                }
            }
            element is ParadoxCsvExpressionElement -> {
                val complexEnumValueInfo = element.complexEnumValueInfo
                if (complexEnumValueInfo != null) {
                    val resolved = complexEnumValueInfo.config.pointer.element
                    return resolved.to.singletonListOrEmpty()
                }
            }
        }
        return emptyList()
    }

    /**
     * 类型 - 如果 [element] 表示一个表达式、封装变量、封装变量引用、内联数学数字等则可用。
     */
    fun getType(element: PsiElement): ParadoxType? {
        return ParadoxTypeResolver.resolveType(element)
    }

    /**
     * 名字 - 如果 [element] 表示一个封装变量、封装变量引用、定义、定值变量、本地化等则可用。
     */
    fun getName(element: PsiElement): String? {
        return when (element) {
            is ParadoxScriptScriptedVariable -> element.name
            is ParadoxScriptScriptedVariableReference -> element.name
            is ParadoxScriptProperty -> {
                element.definitionInfo?.let { return it.name }
                element.defineInfo?.let { return it.expression }
                null
            }
            is ParadoxScriptPropertyKey -> {
                val propertyElement = element.parent ?: return null
                getName(propertyElement)
            }
            is ParadoxLocalisationProperty -> element.name
            is ParadoxParameter -> element.name
            is ParadoxConditionParameter -> element.name
            is ParadoxLocalisationParameter -> element.name
            else -> null
        }
    }

    /**
     * 定义类型 - 如果 [element] 表示一个定义候选（定义、定义注入、定义模板）则可用。
     */
    fun getDefinitionType(element: PsiElement): String? {
        return when (element) {
            is ParadoxScriptProperty -> {
                // #252 兼容定义注入
                element.definitionCandidateInfo?.let { return it.typeText }
                null
            }
            is ParadoxScriptPropertyKey -> {
                val propertyElement = element.parent ?: return null
                getDefinitionType(propertyElement)
            }
            else -> null
        }
    }

    /**
     * 本地化类型 - 如果 [element] 表示一个本地化属性或本地化参数则可用。
     */
    fun getLocalisationType(element: PsiElement): ParadoxLocalisationType? {
        return when (element) {
            is ParadoxLocalisationProperty -> element.type
            is ParadoxLocalisationParameter -> element.resolveLocalisation()?.type
            else -> null
        }
    }

    /**
     * 表达式 - 如果 [element] 表示一个表达式则可用。
     */
    fun getExpression(element: PsiElement): String? {
        return when (element) {
            is ParadoxExpressionElement -> element.expression
            else -> null
        }
    }

    /**
     * 规则表达式 - 如果存在对应的规则表达式则可用。
     */
    fun getConfigExpression(element: PsiElement): String? {
        return when (element) {
            is ParadoxScriptExpressionElement -> {
                val config = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return null
                when (element) {
                    is ParadoxScriptPropertyKey -> {
                        if (config !is CwtPropertyConfig) return null
                        config.key
                    }
                    is ParadoxScriptValue -> {
                        if (config !is CwtValueConfig) return null
                        config.value
                    }
                    else -> null
                }
            }
            is ParadoxCsvExpressionElement -> {
                if (element !is ParadoxCsvColumn) return null
                val columnConfig = ParadoxCsvManager.getColumnConfig(element) ?: return null
                when {
                    ParadoxCsvPsiService.isHeaderColumn(element) -> columnConfig.key
                    else -> {
                        if (!ParadoxCsvManager.isMatchedColumnConfig(element, columnConfig)) return null // require matched
                        columnConfig.value
                    }
                }
            }
            else -> null
        }
    }

    /**
     * 覆盖方式 - 仅限（全局）封装变量、（作为脚本属性的）定义、定值变量、本地化。
     */
    fun getOverrideStrategy(element: PsiElement): ParadoxOverrideStrategy? {
        val targetElement = when {
            element is ParadoxScriptScriptedVariable -> element
            element is ParadoxScriptPropertyKey -> element.parent
            element is ParadoxLocalisationProperty -> element
            else -> null
        }
        if (targetElement == null) return null
        return ParadoxOverrideService.getOverrideStrategy(targetElement)
    }

    /**
     * 作用域上下文信息 - 如果存在则可用。
     */
    fun getScopeContext(element: PsiElement): ParadoxScopeContext? {
        val memberElement = when {
            element is ParadoxScriptPropertyKey -> element.parent as? ParadoxScriptProperty
            element is ParadoxScriptValue -> element
            else -> null
        }
        if (memberElement == null) return null
        if (!ParadoxScopeManager.isScopeContextSupported(memberElement, indirect = true)) return null
        val scopeContext = ParadoxScopeManager.getScopeContext(memberElement) ?: return null
        return scopeContext
    }
}
