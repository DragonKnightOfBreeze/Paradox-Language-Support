package icu.windea.pls.lang.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.core.orNull
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.complexEnumValueInfo
import icu.windea.pls.lang.defineInfo
import icu.windea.pls.lang.defineNamespaceInfo
import icu.windea.pls.lang.defineVariableInfo
import icu.windea.pls.lang.definitionCandidateInfo
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.lang.psi.light.ParadoxComplexEnumValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxDefinitionSnippetLightElement
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxLocalisationParameterLightElement
import icu.windea.pls.lang.psi.light.ParadoxLocalisationSnippetLightElement
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.ParadoxScriptedVariableType
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * 用于按照类型或语义匹配输入的 PSI。
 */
@Suppress("unused")
object ParadoxPsiMatchService {
    /**
     * 是否是匹配语义的 [ParadoxDefinitionSnippetLightElement]。如果名字为空或无法获取，则认为不匹配。
     */
    @OptIn(ExperimentalContracts::class)
    fun isDefinitionSnippetElement(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxDefinitionSnippetLightElement)
        }
        if (element !is ParadoxDefinitionSnippetLightElement) return false
        if (element.name.orNull() == null) return false
        return true
    }

    /**
     * 是否是匹配语义的 [ParadoxLocalisationSnippetLightElement]。如果名字为空或无法获取，则认为不匹配。
     */
    @OptIn(ExperimentalContracts::class)
    fun isLocalisationSnippetElement(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxLocalisationSnippetLightElement)
        }
        if (element !is ParadoxLocalisationSnippetLightElement) return false
        if (element.name.orNull() == null) return false
        return true
    }

    /**
     * 是否是匹配语义的 [ParadoxComplexEnumValueLightElement]。如果名字为空或无法获取，则认为不匹配。
     */
    @OptIn(ExperimentalContracts::class)
    fun isComplexEnumValueElement(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxComplexEnumValueLightElement)
        }
        if (element !is ParadoxComplexEnumValueLightElement) return false
        if (element.name.orNull() == null) return false
        return true
    }

    /**
     * 是否是匹配语义的 [ParadoxDynamicValueLightElement]。如果名字为空或无法获取，则认为不匹配。
     */
    @OptIn(ExperimentalContracts::class)
    fun isDynamicValueElement(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxDynamicValueLightElement)
        }
        if (element !is ParadoxDynamicValueLightElement) return false
        if (element.name.orNull() == null) return false
        return true
    }

    /**
     * 是否是匹配语义的 [ParadoxParameterLightElement]。如果名字为空或无法获取，则认为不匹配。
     */
    @OptIn(ExperimentalContracts::class)
    fun isParameterElement(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxParameterLightElement)
        }
        if (element !is ParadoxParameterLightElement) return false
        if (element.name.orNull() == null) return false
        return true
    }

    /**
     * 是否是匹配语义的 [ParadoxLocalisationParameterLightElement]。如果名字为空或无法获取，则认为不匹配。
     */
    @OptIn(ExperimentalContracts::class)
    fun isLocalisationParameterElement(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxLocalisationParameterLightElement)
        }
        if (element !is ParadoxLocalisationParameterLightElement) return false
        if (element.name.orNull() == null) return false
        return true
    }

    /**
     * 是否是匹配语义的 [ParadoxModifierLightElement]。如果名字为空或无法获取，则认为不匹配。
     */
    @OptIn(ExperimentalContracts::class)
    fun isModifierElement(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxModifierLightElement)
        }
        if (element !is ParadoxModifierLightElement) return false
        if (element.name.orNull() == null) return false
        return true
    }

    /**
     * 是否是匹配语义的模组描述符文件。
     */
    @OptIn(ExperimentalContracts::class)
    fun isModDescriptorFile(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptFile)
        }
        if (element !is ParadoxScriptFile) return false
        if (!element.name.endsWith(".mod", true)) return false
        return true
    }

    /**
     * 是否是匹配语义的的封装变量。如果名字为空或无法获取，则认为不匹配。如果指定了 [type]，则也匹配类型。
     */
    @OptIn(ExperimentalContracts::class)
    fun isScriptedVariable(element: PsiElement?, type: ParadoxScriptedVariableType? = null): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptScriptedVariable)
        }
        if (element !is ParadoxScriptScriptedVariable) return false
        if (element.name?.orNull() == null) return false
        if (type != null) return type == element.type
        return true
    }

    /**
     * 是否是匹配语义的定义候选。
     */
    @OptIn(ExperimentalContracts::class)
    fun isDefinitionCandidate(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxDefinitionElement)
        }
        if (element !is ParadoxDefinitionElement) return false
        if (element.definitionCandidateInfo == null) return false
        return true
    }

    /**
     * 是否是匹配语义的定义。兼容匿名定义。
     */
    @OptIn(ExperimentalContracts::class)
    fun isDefinition(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxDefinitionElement)
        }
        if (element !is ParadoxDefinitionElement) return false
        if (element.definitionInfo == null) return false
        return true
    }

    /**
     * 是否是匹配语义的定义注入。
     */
    @OptIn(ExperimentalContracts::class)
    fun isDefinitionInjection(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptProperty)
        }
        if (element !is ParadoxScriptProperty) return false
        if (element.definitionInjectionInfo == null) return false
        return true
    }

    /**
     * 是否是匹配语义的的本地化。如果名字为空或无法获取，则认为不匹配。如果指定了 [type]，则也匹配类型。
     */
    @OptIn(ExperimentalContracts::class)
    fun isLocalisation(element: PsiElement?, type: ParadoxLocalisationType? = null): Boolean {
        contract {
            returns(true) implies (element is ParadoxLocalisationProperty)
        }
        if (element !is ParadoxLocalisationProperty) return false
        if (element.name.orNull() == null) return false
        if (type != null) return type == element.type
        return true
    }

    /**
     * 是否是匹配语义的复杂枚举值。
     */
    @OptIn(ExperimentalContracts::class)
    fun isComplexEnumValue(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxExpressionElement)
        }
        if (element !is ParadoxExpressionElement) return false
        if (element.complexEnumValueInfo == null) return false
        return true
    }

    /**
     * 是否是匹配语义的定值变量或定值命名空间。
     */
    @OptIn(ExperimentalContracts::class)
    fun isDefine(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptProperty)
        }
        if (element !is ParadoxScriptProperty) return false
        if (element.defineInfo == null) return false
        return true
    }

    /**
     * 是否是匹配语义的定值命名空间。
     */
    @OptIn(ExperimentalContracts::class)
    fun isDefineNamespace(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptProperty)
        }
        if (element !is ParadoxScriptProperty) return false
        if (element.defineNamespaceInfo == null) return false
        return true
    }

    /**
     * 是否是匹配语义的定值变量。
     */
    @OptIn(ExperimentalContracts::class)
    fun isDefineVariable(element: PsiElement?): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptProperty)
        }
        if (element !is ParadoxScriptProperty) return false
        if (element.defineVariableInfo == null) return false
        return true
    }

    /**
     * 是否是匹配语义的内联脚本用法。
     */
    @OptIn(ExperimentalContracts::class)
    fun isInlineScriptUsage(element: PsiElement?, context: Any? = element): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptProperty)
        }
        if (element !is ParadoxScriptProperty) return false
        if (!ParadoxInlineScriptManager.isMatched(element.name, context)) return false
        if (!ParadoxInlineScriptManager.isAvailable(element)) return false
        return true
    }

    /**
     * 是否是匹配语义的定义注入用法。
     */
    @OptIn(ExperimentalContracts::class)
    fun isDefinitionInjectionUsage(element: PsiElement?, context: Any? = element): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptProperty)
        }
        if (element !is ParadoxScriptProperty) return false
        if (!ParadoxDefinitionInjectionManager.isMatched(element.name, context)) return false
        if (!ParadoxDefinitionInjectionManager.isAvailable(element)) return false
        return true
    }

    /**
     * 是否是匹配语义的定义（scripted trigger/scripted effect/等等）调用。
     */
    @OptIn(ExperimentalContracts::class)
    fun isDefinitionCall(element: PsiElement?, referenceElement: PsiElement): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptProperty)
        }
        if (element !is ParadoxScriptProperty) return false
        if (referenceElement !is ParadoxScriptPropertyKey) return false
        val name = element.definitionInfo?.name?.orNull() ?: return false
        if (name != referenceElement.text.unquote()) return false
        return true
    }

    /**
     * 是否是匹配语义的内联脚本文件。
     */
    @OptIn(ExperimentalContracts::class)
    fun isInlineScriptFile(element: PsiElement?, context: Any? = element): Boolean {
        contract {
            returns(true) implies (element is ParadoxScriptFile)
        }
        if (element !is ParadoxScriptFile) return false
        if (!ParadoxInlineScriptManager.isInlineScriptFile(element)) return false
        return true
    }
}
