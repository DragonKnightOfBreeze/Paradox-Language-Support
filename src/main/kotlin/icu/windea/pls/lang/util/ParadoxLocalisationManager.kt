package icu.windea.pls.lang.util

import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.PsiModificationTracker
import icu.windea.pls.ChronicleCapabilities
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.annotations.Inferred
import icu.windea.pls.core.isEscapedCharAt
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getCachedValueOnDemand
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.resolve.ParadoxLocalisationService
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

object ParadoxLocalisationManager {
    object Keys : KeyRegistry() {
        val cachedRelatedScriptedVariables by registerKey<CachedValue<List<ParadoxScriptScriptedVariable>>>(this)
        val cachedRelatedDefinitions by registerKey<CachedValue<List<ParadoxDefinitionElement>>>(this)
        val cachedPresentableText by registerKey<CachedValue<String?>>(this)
    }

    @Inferred
    fun isRichLocalisationText(text: CharSequence, checkEscape: Boolean = true): Boolean {
        // For each localisation property value, if it does not contain special markers,
        // it's unnecessary to process lazy-parsing.

        // For each script string, if it can match data expression `scalar` or `localisation`, and contain special markers,
        // it's worth to apply language injection.

        for (i in 0 until text.length) {
            when (text[i]) {
                // accept left bracket & do not check escape (`[[` or `\[`)
                '[' -> return true
                // accept special markers involve rich text constructs & check escape if `checkEscape = true`
                '$', '§', '£', '#', '@' -> if (!(checkEscape && text.isEscapedCharAt(i))) return true
                // 3.0.2 accept special markers involve grammatical constructs & check escape if `checkEscape = true`
                '|', '&' -> if (!(checkEscape && text.isEscapedCharAt(i))) return true
            }
        }
        return false
    }

    @Inferred
    fun isSpecialLocalisation(element: ParadoxLocalisationProperty): Boolean {
        // There are some special localizations that cannot be used directly to render localisation text

        // NOTE 3.0.4 For localisations which text contain grammatical constructs, such as tags or context tags,
        //  although they are special in some meanings, since it's expected to still contain the normal string part (i.e., before `|||`),
        //  we can still (and just at this moment) render such part.

        val file = element.containingFile ?: return false
        val fileName = file.name
        if (fileName.startsWith("name_system_")) return true // e.g., `name_system_l_english.yml`
        return false
    }

    // region Related Items

    /**
     * 得到 [element] 对应的本地化的所有相关封装变量。
     *
     * @see ParadoxLocalisationService.resolveRelatedScriptedVariables
     */
    fun getRelatedScriptedVariables(element: ParadoxLocalisationProperty): List<ParadoxScriptScriptedVariable> {
        return getRelatedScriptedVariablesInternal(element)
    }

    private fun getRelatedScriptedVariablesInternal(element: ParadoxLocalisationProperty): List<ParadoxScriptScriptedVariable> {
        return getCachedValueOnDemand(element, Keys.cachedRelatedScriptedVariables, ChronicleCapabilities.Cache.relatedItems) {
            // NOTE 3.0.4 since PSI is directly cached here, invalidated on any PSI change atm
            val value = ParadoxLocalisationService.resolveRelatedScriptedVariables(element)
            CachedValueProvider.Result.create(value, element, PsiModificationTracker.MODIFICATION_COUNT)
        }
    }

    /**
     * 得到 [element] 对应的本地化的所有相关定义。
     *
     * @see ParadoxLocalisationService.resolveRelatedDefinitions
     */
    fun getRelatedDefinitions(element: ParadoxLocalisationProperty): List<ParadoxDefinitionElement> {
        return getRelatedDefinitionsInternal(element)
    }

    private fun getRelatedDefinitionsInternal(element: ParadoxLocalisationProperty): List<ParadoxDefinitionElement> {
        return getCachedValueOnDemand(element, Keys.cachedRelatedDefinitions, ChronicleCapabilities.Cache.relatedItems) {
            // NOTE 3.0.4 since PSI is directly cached here, invalidated on any PSI change atm
            val value = ParadoxLocalisationService.resolveRelatedDefinitions(element)
            CachedValueProvider.Result.create(value, element, PsiModificationTracker.MODIFICATION_COUNT)
        }
    }

    /**
     * 得到 [name] 对应的所有相关本地化。
     *
     * @see ParadoxLocalisationService.resolveRelatedLocalisationsFrom
     */
    fun getRelatedLocalisationsFrom(
        name: String?,
        contextElement: PsiElement,
        preferredLocale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig(),
        preferred: Boolean = false,
    ): List<ParadoxLocalisationProperty> {
        return ParadoxLocalisationService.resolveRelatedLocalisationsFrom(name, contextElement, preferredLocale, preferred)
    }

    // endregion

    // region Presentable Items

    /**
     * 得到 [element] 对应的本地化的展示文本。
     *
     * @see ParadoxLocalisationService.resolvePresentableText
     */
    fun getPresentableText(element: ParadoxLocalisationProperty?): String? {
        if (element == null) return null
        return getPresentableTextInternal(element)
    }

    /**
     * 得到 [elements] 对应的一组本地化的展示文本。
     *
     * @see ParadoxLocalisationService.resolvePresentableText
     */
    fun getPresentableText(elements: Collection<ParadoxLocalisationProperty>): List<String> {
        if (elements.isEmpty()) return emptyList()
        if (elements.size == 1) return getPresentableTextInternal(elements.single()).to.singletonListOrEmpty()
        return elements.mapNotNullTo(mutableSetOf()) { getPresentableTextInternal(it) }.toList()
    }

    private fun getPresentableTextInternal(element: ParadoxLocalisationProperty): String? {
        return getCachedValueOnDemand(element, Keys.cachedPresentableText, ChronicleCapabilities.Cache.presentableItems) {
            val value = ParadoxLocalisationService.resolvePresentableText(element)
            CachedValueProvider.Result.create(value, element)
        }
    }
}
