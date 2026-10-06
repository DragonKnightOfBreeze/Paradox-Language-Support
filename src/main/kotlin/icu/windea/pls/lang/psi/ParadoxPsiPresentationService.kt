package icu.windea.pls.lang.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.lang.defineNamespaceInfo
import icu.windea.pls.lang.defineVariableInfo
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.lang.psi.light.ParadoxDefinitionSnippetLightElement
import icu.windea.pls.lang.psi.light.ParadoxLocalisationSnippetLightElement
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.resolve.ParadoxInlineScriptService
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Suppress("unused", "UselessCallOnNotNull")
object ParadoxPsiPresentationService {
    /**
     * 得到定义引用片段的名字。如果匹配语义而无法获取，则返回空字符串。
     *
     * 从解析得到的 [ParadoxDefinitionSnippetLightElement] 获取。
     */
    @OptIn(ExperimentalContracts::class)
    fun getNameForDefinitionSnippet(element: PsiElement?): String? {
        contract {
            returnsNotNull() implies (element is ParadoxDefinitionSnippetLightElement)
        }
        if (element !is ParadoxDefinitionSnippetLightElement) return null
        return element.name.orEmpty()
    }

    /**
     * 得到本地化引用片段的名字。如果匹配语义而无法获取，则返回空字符串。
     *
     * 从解析得到的 [ParadoxLocalisationSnippetLightElement] 获取。
     */
    @OptIn(ExperimentalContracts::class)
    fun getNameForLocalisationSnippet(element: PsiElement?): String? {
        contract {
            returnsNotNull() implies (element is ParadoxLocalisationSnippetLightElement)
        }
        if (element !is ParadoxLocalisationSnippetLightElement) return null
        return element.name.orEmpty()
    }

    /**
     * 得到修正的名字。如果匹配语义而无法获取，则返回空字符串。
     *
     * 从解析得到的 [ParadoxModifierLightElement] 获取。
     */
    @OptIn(ExperimentalContracts::class)
    fun getNameForModifier(element: PsiElement?): String? {
        contract {
            returnsNotNull() implies (element is ParadoxModifierLightElement)
        }
        if (element !is ParadoxModifierLightElement) return null
        return element.name.orEmpty()
    }

    /**
     * 得到封装变量的名字。如果匹配语义而无法获取，则返回空字符串。
     */
    @OptIn(ExperimentalContracts::class)
    fun getNameForScriptedVariable(element: PsiElement?): String? {
        contract {
            returnsNotNull() implies (element is ParadoxScriptScriptedVariable)
        }
        if (element !is ParadoxScriptScriptedVariable) return null
        return element.name.orEmpty()
    }

    /**
     * 得到封装变量的展示名字。如果匹配语义而无法获取，则返回空字符串。
     */
    @OptIn(ExperimentalContracts::class)
    fun getPresentableNameForScriptedVariable(element: PsiElement?): String? {
        contract {
            returnsNotNull() implies (element is ParadoxScriptScriptedVariable)
        }
        if (element !is ParadoxScriptScriptedVariable) return null
        return ParadoxScriptedVariableManager.getPresentableNames(element, preferred = true).firstOrNull().orEmpty()
    }

    /**
     * 得到定义的名字。如果匹配语义而无法获取，则返回空字符串。
     */
    @OptIn(ExperimentalContracts::class)
    fun getNameForDefinition(element: PsiElement?): String? {
        contract {
            returnsNotNull() implies (element is ParadoxDefinitionElement)
        }
        if (element !is ParadoxDefinitionElement) return null
        val definitionInfo = element.definitionInfo
        if (definitionInfo == null) return null
        return definitionInfo.name.orEmpty()
    }

    /**
     * 得到定义的展示名字。如果匹配语义而无法获取，则返回空字符串。
     */
    @OptIn(ExperimentalContracts::class)
    fun getPresentableNameForDefinition(element: PsiElement?): String? {
        contract {
            returnsNotNull() implies (element is ParadoxDefinitionElement)
        }
        if (element !is ParadoxDefinitionElement) return null
        val definitionInfo = element.definitionInfo
        if (definitionInfo == null) return null
        return ParadoxDefinitionManager.getPresentableName(element).orEmpty()
    }

    /**
     * 得到本地化的名字。如果匹配语义而无法获取，则返回空字符串。
     */
    @OptIn(ExperimentalContracts::class)
    fun getNameForLocalisation(element: PsiElement?): String? {
        contract {
            returnsNotNull() implies (element is ParadoxLocalisationProperty)
        }
        if (element !is ParadoxLocalisationProperty) return null
        return element.name.orEmpty()
    }

    /**
     * 得到定值命名空间的名字。如果匹配语义而无法获取，则返回空字符串。
     */
    @OptIn(ExperimentalContracts::class)
    fun getNameForDefineNamespace(element: PsiElement?): String? {
        contract {
            returnsNotNull() implies (element is ParadoxScriptProperty)
        }
        if (element !is ParadoxScriptProperty) return null
        val defineNamespaceInfo = element.defineNamespaceInfo
        if (defineNamespaceInfo == null) return null
        return defineNamespaceInfo.namespace.orEmpty()
    }

    /**
     * 得到定值变量的名字。如果匹配语义而无法获取，则返回空字符串。
     */
    @OptIn(ExperimentalContracts::class)
    fun getNameForDefineVariable(element: PsiElement?): String? {
        contract {
            returnsNotNull() implies (element is ParadoxScriptProperty)
        }
        if (element !is ParadoxScriptProperty) return null
        val defineVariableInfo = element.defineVariableInfo
        if (defineVariableInfo == null) return null
        return defineVariableInfo.variable.orEmpty()
    }

    /**
     * 得到定值变量的表达式。如果匹配语义而无法获取，则返回空字符串。
     */
    @OptIn(ExperimentalContracts::class)
    fun getExpressionForDefineVariable(element: PsiElement?): String? {
        contract {
            returnsNotNull() implies (element is ParadoxScriptProperty)
        }
        if (element !is ParadoxScriptProperty) return null
        val defineVariableInfo = element.defineVariableInfo
        if (defineVariableInfo == null) return null
        return defineVariableInfo.expression.orEmpty()
    }

    /**
     * 得到内联脚本用法的表达式。如果匹配语义而无法获取，则返回空字符串。
     *
     * 从内联脚本用法对应的 [ParadoxScriptProperty] 获取。
     */
    @OptIn(ExperimentalContracts::class)
    fun getExpressionForInlineScriptUsage(element: PsiElement?): String? {
        contract {
            returnsNotNull() implies (element is ParadoxScriptProperty)
        }
        if (element !is ParadoxScriptProperty) return null
        if (!ParadoxPsiMatchService.isInlineScriptUsage(element)) return null
        return ParadoxInlineScriptService.getInlineScriptExpressionFromUsageElement(element, resolve = true).orEmpty()
    }

    /**
     * 得到定义注入用法的表达式。如果匹配语义而无法获取，则返回空字符串。
     *
     * 从定义注入用法对应的 [ParadoxScriptProperty] 获取。
     */
    @OptIn(ExperimentalContracts::class)
    fun getExpressionForDefinitionInjectionUsage(element: PsiElement?): String? {
        contract {
            returnsNotNull() implies (element is ParadoxScriptProperty)
        }
        if (element !is ParadoxScriptProperty) return null
        val definitionInjectionInfo = element.definitionInjectionInfo
        if (definitionInjectionInfo == null) return null
        return definitionInjectionInfo.expression.orEmpty()
    }
}
