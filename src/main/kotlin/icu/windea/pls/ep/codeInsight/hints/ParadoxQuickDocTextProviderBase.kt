package icu.windea.pls.ep.codeInsight.hints

import com.intellij.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

sealed class ParadoxQuickDocTextProviderBase : ParadoxQuickDocTextProvider {
    abstract class ScriptedVariable : ParadoxQuickDocTextProviderBase() {
        override fun getQuickDocText(element: PsiElement): String? {
            if (!ParadoxPsiMatcher.isScriptedVariable(element)) return null
            val name = element.name
            if (name.isNullOrEmpty()) return null
            if (name.isParameterized()) return null
            return doGetQuickDocText(element)
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

        abstract fun doGetQuickDocText(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String?
    }

    abstract class ComplexEnumValue : ParadoxQuickDocTextProviderBase() {
        override fun getQuickDocText(element: PsiElement): String? {
            if (!ParadoxPsiMatcher.isComplexEnumValueElement(element)) return null
            val name = element.name
            if (name.isEmpty()) return null
            if (name.isParameterized()) return null
            return doGetQuickDocText(element)
        }

        abstract fun doGetQuickDocText(element: ParadoxComplexEnumValueElement): String?
    }

    abstract class DynamicValue : ParadoxQuickDocTextProviderBase() {
        override fun getQuickDocText(element: PsiElement): String? {
            if (!ParadoxPsiMatcher.isDynamicValueElement(element)) return null
            val name = element.name
            if (name.isEmpty()) return null
            if (name.isParameterized()) return null
            return doGetQuickDocText(element)
        }

        abstract fun doGetQuickDocText(element: ParadoxDynamicValueElement): String?
    }

    abstract class Parameter : ParadoxQuickDocTextProviderBase() {
        override fun getQuickDocText(element: PsiElement): String? {
            if (!ParadoxPsiMatcher.isParameterElement(element)) return null
            val name = element.name
            if (name.isEmpty()) return null
            // if (name.isParameterized()) return null // unnecessary
            return doGetQuickDocText(element)
        }

        abstract fun doGetQuickDocText(element: ParadoxParameterElement): String?
    }
}
