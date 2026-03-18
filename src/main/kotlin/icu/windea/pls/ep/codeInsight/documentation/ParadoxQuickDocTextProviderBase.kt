package icu.windea.pls.ep.codeInsight.documentation

import com.intellij.psi.PsiElement
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxPsiMatcher
import icu.windea.pls.lang.psi.light.ParadoxComplexEnumValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

sealed class ParadoxQuickDocTextProviderBase : ParadoxQuickDocTextProvider {
    abstract class ScriptedVariable : ParadoxQuickDocTextProviderBase() {
        override fun getQuickDocText(element: PsiElement): String? {
            if (!ParadoxPsiMatcher.isScriptedVariable(element)) return null
            val name = element.name
            if (name.isNullOrEmpty()) return null
            if (name.isParameterized()) return null
            return doGetQuickDocText(element, name)
        }

        abstract fun doGetQuickDocText(element: ParadoxScriptScriptedVariable, name: String): String?
    }

    abstract class Definition : ParadoxQuickDocTextProviderBase() {
        override fun getQuickDocText(element: PsiElement): String? {
            if (!ParadoxPsiMatcher.isDefinition(element)) return null
            val definitionInfo = element.definitionInfo ?: return null
            val name = definitionInfo.name
            if (name.isEmpty()) return null
            if (name.isParameterized()) return null
            return doGetQuickDocText(element, definitionInfo)
        }

        abstract fun doGetQuickDocText(element: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String?
    }

    abstract class InlineScript : ParadoxQuickDocTextProviderBase() {
        override fun getQuickDocText(element: PsiElement): String? {
            if (!ParadoxPsiMatcher.isInlineScriptFile(element)) return null
            val expression = ParadoxInlineScriptManager.getInlineScriptExpression(element) ?: return null
            if (expression.isEmpty()) return null
            if (expression.isParameterized()) return null
            return doGetQuickDocText(element, expression)
        }

        abstract fun doGetQuickDocText(element: ParadoxScriptFile, expression: String): String?
    }

    abstract class ComplexEnumValue : ParadoxQuickDocTextProviderBase() {
        override fun getQuickDocText(element: PsiElement): String? {
            if (!ParadoxPsiMatcher.isComplexEnumValueElement(element)) return null
            val name = element.name
            if (name.isEmpty()) return null
            if (name.isParameterized()) return null
            return doGetQuickDocText(element)
        }

        abstract fun doGetQuickDocText(element: ParadoxComplexEnumValueLightElement): String?
    }

    abstract class DynamicValue : ParadoxQuickDocTextProviderBase() {
        override fun getQuickDocText(element: PsiElement): String? {
            if (!ParadoxPsiMatcher.isDynamicValueElement(element)) return null
            val name = element.name
            if (name.isEmpty()) return null
            if (name.isParameterized()) return null
            return doGetQuickDocText(element)
        }

        abstract fun doGetQuickDocText(element: ParadoxDynamicValueLightElement): String?
    }

    abstract class Parameter : ParadoxQuickDocTextProviderBase() {
        override fun getQuickDocText(element: PsiElement): String? {
            if (!ParadoxPsiMatcher.isParameterElement(element)) return null
            val name = element.name
            if (name.isEmpty()) return null
            // if (name.isParameterized()) return null // unnecessary
            return doGetQuickDocText(element)
        }

        abstract fun doGetQuickDocText(element: ParadoxParameterLightElement): String?
    }
}
