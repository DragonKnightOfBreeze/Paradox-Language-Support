package icu.windea.pls.lang.util

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextRenderer
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptPsiUtil
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 用于处理封装变量。
 */
object ParadoxScriptedVariableManager {
    object Keys : KeyRegistry() {
        val localScriptedVariable by createKey<CachedValue<List<SmartPsiElementPointer<ParadoxScriptScriptedVariable>>>>(Keys)
    }

    @Suppress("unused")
    fun getLocalScriptedVariables(file: ParadoxScriptFile): List<SmartPsiElementPointer<ParadoxScriptScriptedVariable>> {
        return CachedValuesManager.getCachedValue(file, Keys.localScriptedVariable) {
            val value = doGetLocalScriptedVariables(file)
            value.withDependencyItems(file)
        }
    }

    private fun doGetLocalScriptedVariables(file: ParadoxScriptFile): List<SmartPsiElementPointer<ParadoxScriptScriptedVariable>> {
        val result = mutableListOf<SmartPsiElementPointer<ParadoxScriptScriptedVariable>>()
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptScriptedVariable) {
                    result.add(element.createPointer(file))
                }
                if (!ParadoxScriptPsiUtil.isMemberContextElement(element)) return // optimize
                super.visitElement(element)
            }
        })
        return result
    }

    fun getNameLocalisation(name: String, contextElement: PsiElement, locale: CwtLocaleConfig): ParadoxLocalisationProperty? {
        val selector = selector(contextElement.project, contextElement).localisation().contextSensitive().preferLocale(locale)
        return ParadoxLocalisationSearch.search(name, selector).find()
    }

    fun getLocalizedName(element: ParadoxScriptScriptedVariable): String? {
        val name = element.name?.orNull() ?: return null
        val nameLocalisation = getNameLocalisation(name, element, ParadoxLocaleManager.getPreferredLocaleConfig()) ?: return null
        return ParadoxLocalisationTextRenderer().render(nameLocalisation).orNull()
    }
}
