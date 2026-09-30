package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.core.annotations.Inferred
import icu.windea.pls.core.isEscapedCharAt
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.resolve.ParadoxLocalisationService
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

object ParadoxLocalisationManager {
    object Keys : KeyRegistry() {
        val cachedPresentableName by registerKey<CachedValue<String>>(Keys)
    }

    fun getPresentableText(element: ParadoxLocalisationProperty): String? {
        // from cache
        return getPresentableTextFromCache(element)
    }

    private fun getPresentableTextFromCache(element: ParadoxLocalisationProperty): String? {
        // invalidate on element modification
        return CachedValuesManager.getCachedValue(element, Keys.cachedPresentableName) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                val value = ParadoxLocalisationService.resolvePresentableText(element)
                value.withDependencyItems(element)
            }
        }
    }

    fun getRelatedScriptedVariables(element: ParadoxLocalisationProperty): List<ParadoxScriptScriptedVariable> {
        return ParadoxLocalisationService.resolveRelatedScriptedVariables(element)
    }

    fun getRelatedDefinitions(element: ParadoxLocalisationProperty): List<ParadoxDefinitionElement> {
        return ParadoxLocalisationService.resolveRelatedDefinitions(element)
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
}
