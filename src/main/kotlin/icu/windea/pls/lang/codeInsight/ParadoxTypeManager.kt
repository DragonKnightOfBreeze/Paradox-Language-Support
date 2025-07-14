package icu.windea.pls.lang.codeInsight

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*

object ParadoxTypeManager {
    fun isTypedElement(element: PsiElement): Boolean {
        if (element.language !is ParadoxBaseLanguage) return false
        return when {
            element is ParadoxExpressionElement -> true
            element is ParadoxScriptedVariableReference -> true
            element is ParadoxParameter -> true
            element is ParadoxConditionParameter -> true
            element is ParadoxScriptScriptedVariable -> true
            element is ParadoxScriptInlineMathNumber -> true
            else -> false
        }
    }

    fun findTypedElements(elementAt: PsiElement): List<PsiElement> {
        if (elementAt.language !is ParadoxBaseLanguage) return emptyList()
        val element = elementAt.parents(withSelf = true).find { isTypedElement(it) }
        if (element == null) return emptyList()
        return listOf(element)
    }

    /**
     * 基本类型 - 基于PSI的类型
     */
    fun getType(element: PsiElement): ParadoxType {
        return when (element) {
            is ParadoxScriptPropertyKey -> ParadoxTypeResolver.resolve(element.value)
            is ParadoxScriptBoolean -> ParadoxType.Boolean
            is ParadoxScriptInt -> ParadoxType.Int
            is ParadoxScriptFloat -> ParadoxType.Float
            is ParadoxScriptString -> ParadoxType.String
            is ParadoxScriptColor -> ParadoxType.Color
            is ParadoxScriptBlock -> ParadoxType.Block
            is ParadoxScriptInlineMath -> ParadoxType.InlineMath
            is ParadoxScriptedVariableReference -> {
                element.reference?.resolve()?.let { getType(it) } ?: ParadoxType.Unknown
            }
            is ParadoxParameter -> ParadoxType.Parameter
            is ParadoxConditionParameter -> ParadoxType.Parameter
            is ParadoxLocalisationCommandText -> {
                if (element.isCommandExpression()) return ParadoxType.CommandExpression
                ParadoxType.Unknown
            }
            is ParadoxLocalisationConceptName -> {
                if (element.isDatabaseObjectExpression()) return ParadoxType.DatabaseObjectExpression
                ParadoxType.Unknown
            }
            is ParadoxScriptScriptedVariable -> {
                element.scriptedVariableValue?.let { getType(it) } ?: ParadoxType.Unknown
            }
            is ParadoxScriptInlineMathNumber -> ParadoxTypeResolver.resolve(element.text)
            else -> ParadoxType.Unknown
        }
    }

    /**
     * 表达式 - 如果PSI表示一个表达式则可用
     */
    fun getExpression(element: PsiElement): String? {
        if (!isTypedElement(element)) return null
        return when (element) {
            is ParadoxScriptBlock -> PlsStringConstants.blockFolder
            is ParadoxScriptInlineMath -> PlsStringConstants.inlineMathFolder
            else -> element.text
        }
    }

    /**
     * 规则表达式 - 如果存在对应的CWT规则表达式则可用
     */
    fun getConfigExpression(element: PsiElement): String? {
        if (!isTypedElement(element)) return null
        val config = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return null
        return when (element) {
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

    /**
     * 定义类型 - 如果PSI是[ParadoxScriptPropertyKey]则可用
     */
    fun getDefinitionType(element: PsiElement): String? {
        if (element !is ParadoxScriptPropertyKey) return null
        val definition = element.parent.castOrNull<ParadoxScriptProperty>() ?: return null
        val definitionInfo = definition.definitionInfo ?: return null
        return definitionInfo.typesText
    }

    /**
     * 作用域上下文信息 - 如果存在则可用
     */
    fun getScopeContext(element: PsiElement): ParadoxScopeContext? {
        val memberElement = when {
            element is ParadoxScriptPropertyKey -> element.parent as? ParadoxScriptProperty
            element is ParadoxScriptValue -> element
            else -> null
        }
        if (memberElement == null) return null
        if (!ParadoxScopeManager.isScopeContextSupported(memberElement, indirect = true)) return null
        val scopeContext = ParadoxScopeManager.getSwitchedScopeContext(memberElement) ?: return null
        return scopeContext
    }

    /**
     * 依次尝试导航到：
     * * 定义的CWT类型规则
     * * 定义名对应的定义的CWT类型规则
     * * 对应的CWT枚举规则
     * * 对应的CWT复杂枚举规则
     * * 对应的预定义的CWT动态值规则
     */
    fun findTypeDeclarations(element: PsiElement): List<PsiElement> {
        //注意这里的element是解析引用后得到的PSI元素，因此无法定位到定义成员对应的规则声明
        when {
            element is ParadoxScriptProperty -> {
                val definitionInfo = element.definitionInfo
                if (definitionInfo != null) {
                    if (definitionInfo.types.size == 1) {
                        return definitionInfo.typeConfig.pointer.element.toSingletonListOrEmpty()
                    } else {
                        //这里的element可能是null，以防万一，处理是null的情况
                        return buildList {
                            definitionInfo.typeConfig.pointer.element?.let { add(it) }
                            definitionInfo.subtypeConfigs.forEach { subtypeConfig ->
                                subtypeConfig.pointer.element?.let { add(it) }
                            }
                        }
                    }
                }
                return emptyList()
            }
            element is ParadoxScriptExpressionElement -> {
                if (element is ParadoxScriptPropertyKey) {
                    return findTypeDeclarations(element.parent)
                } else if (element is ParadoxScriptValue && element.isDefinitionName()) {
                    val definition = element.findParentDefinition()
                    if (definition is ParadoxScriptProperty) return findTypeDeclarations(definition)
                }

                if (element is ParadoxScriptStringExpressionElement) {
                    val complexEnumValueInfo = element.complexEnumValueInfo
                    if (complexEnumValueInfo != null) {
                        val gameType = complexEnumValueInfo.gameType
                        val configGroup = PlsFacade.getConfigGroup(element.project, gameType)
                        val enumName = complexEnumValueInfo.enumName
                        val config = configGroup.complexEnums[enumName] ?: return emptyList() //unexpected
                        val resolved = config.pointer.element ?: return emptyList()
                        return resolved.toSingletonList()
                    }
                }
            }
        }
        return emptyList()
    }
}
