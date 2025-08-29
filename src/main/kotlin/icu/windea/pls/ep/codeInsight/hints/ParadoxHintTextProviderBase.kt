package icu.windea.pls.ep.codeInsight.hints

import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtLocaleConfig
import icu.windea.pls.lang.PlsKeys
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.util.ParadoxPsiMatcher
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementFactory
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

sealed class ParadoxHintTextProviderBase : ParadoxHintTextProvider {
    protected fun createHintLocalisation(hintText: String, element: PsiElement): ParadoxLocalisationProperty {
        val hintLocalisation = ParadoxLocalisationElementFactory.createProperty(element.project, "hint", hintText)
        //it's necessary to inject fileInfo here (so that gameType can be got later)
        hintLocalisation.containingFile.virtualFile.putUserData(PlsKeys.injectedFileInfo, element.fileInfo)
        return hintLocalisation
    }

    abstract class ScriptedVariable : ParadoxHintTextProviderBase() {
        override fun getHintText(element: PsiElement, locale: CwtLocaleConfig?): String? {
            if (!ParadoxPsiMatcher.isScriptedVariable(element)) return null
            val name = element.name
            if (name.isNullOrEmpty()) return null
            if (name.isParameterized()) return null
            return doGetHintText(element, name, locale)
        }

        override fun getHintLocalisation(element: PsiElement, locale: CwtLocaleConfig?): ParadoxLocalisationProperty? {
            if (!ParadoxPsiMatcher.isScriptedVariable(element)) return null
            val name = element.name
            if (name.isNullOrEmpty()) return null
            if (name.isParameterized()) return null
            return doGetHintLocalisation(element, name, locale)
        }

        abstract fun doGetHintText(element: ParadoxScriptScriptedVariable, name: String, locale: CwtLocaleConfig?): String?

        abstract fun doGetHintLocalisation(element: ParadoxScriptScriptedVariable, name: String, locale: CwtLocaleConfig?): ParadoxLocalisationProperty?
    }

    abstract class Definition : ParadoxHintTextProviderBase() {
        override fun getHintText(element: PsiElement, locale: CwtLocaleConfig?): String? {
            if (!ParadoxPsiMatcher.isDefinition(element)) return null
            val definitionInfo = element.definitionInfo ?: return null
            val name = definitionInfo.name
            if (name.isEmpty()) return null
            if (name.isParameterized()) return null
            return doGetHintText(element, definitionInfo, locale)
        }

        override fun getHintLocalisation(element: PsiElement, locale: CwtLocaleConfig?): ParadoxLocalisationProperty? {
            if (!ParadoxPsiMatcher.isDefinition(element)) return null
            val definitionInfo = element.definitionInfo ?: return null
            val name = definitionInfo.name
            if (name.isEmpty()) return null
            if (name.isParameterized()) return null
            if (name.isParameterized()) return null
            return doGetHintLocalisation(element, definitionInfo, locale)
        }

        abstract fun doGetHintText(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, locale: CwtLocaleConfig?): String?

        abstract fun doGetHintLocalisation(element: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, locale: CwtLocaleConfig?): ParadoxLocalisationProperty?
    }

    abstract class ComplexEnumValue : ParadoxHintTextProviderBase() {
        override fun getHintText(element: PsiElement, locale: CwtLocaleConfig?): String? {
            if (!ParadoxPsiMatcher.isComplexEnumValueElement(element)) return null
            val name = element.name
            if (name.isEmpty()) return null
            if (name.isParameterized()) return null
            return doGetHintText(element, locale)
        }

        override fun getHintLocalisation(element: PsiElement, locale: CwtLocaleConfig?): ParadoxLocalisationProperty? {
            if (!ParadoxPsiMatcher.isComplexEnumValueElement(element)) return null
            val name = element.name
            if (name.isEmpty()) return null
            if (name.isParameterized()) return null
            return doGetHintLocalisation(element, locale)
        }

        abstract fun doGetHintText(element: ParadoxComplexEnumValueElement, locale: CwtLocaleConfig?): String?

        abstract  fun doGetHintLocalisation(element: ParadoxComplexEnumValueElement, locale: CwtLocaleConfig?): ParadoxLocalisationProperty?
    }

    abstract class DynamicValue : ParadoxHintTextProviderBase() {
        override fun getHintText(element: PsiElement, locale: CwtLocaleConfig?): String? {
            if (!ParadoxPsiMatcher.isDynamicValueElement(element)) return null
            val name = element.name
            if (name.isEmpty()) return null
            if (name.isParameterized()) return null
            return doGetHintText(element, locale)
        }


        override fun getHintLocalisation(element: PsiElement, locale: CwtLocaleConfig?): ParadoxLocalisationProperty? {
            if (!ParadoxPsiMatcher.isDynamicValueElement(element)) return null
            val name = element.name
            if (name.isEmpty()) return null
            if (name.isParameterized()) return null
            return doGetHintLocalisation(element, locale)
        }

        abstract fun doGetHintText(element: ParadoxDynamicValueElement, locale: CwtLocaleConfig?): String?

        abstract fun doGetHintLocalisation(element: ParadoxDynamicValueElement, locale: CwtLocaleConfig?): ParadoxLocalisationProperty?
    }
}
