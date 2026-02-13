package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.core.annotations.Inferred
import icu.windea.pls.core.isEscapedCharAt
import icu.windea.pls.core.runReadActionSmartly
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.resolve.ParadoxLocalisationService
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

object ParadoxLocalisationManager {
    object Keys : KeyRegistry() {
        val cachedLocalizedName by registerKey<CachedValue<String>>(Keys)
    }

    fun getLocalizedText(element: ParadoxLocalisationProperty): String? {
        // from cache (invalidate on element modification)
        return CachedValuesManager.getCachedValue(element, Keys.cachedLocalizedName) {
            ProgressManager.checkCanceled()
            val value = runReadActionSmartly { ParadoxLocalisationService.resolveLocalizedText(element) }
            value.withDependencyItems(element)
        }
    }

    fun getRelatedScriptedVariables(element: ParadoxLocalisationProperty): List<ParadoxScriptScriptedVariable> {
        return ParadoxLocalisationService.resolveRelatedScriptedVariables(element)
    }

    fun getRelatedDefinitions(element: ParadoxLocalisationProperty): List<ParadoxDefinitionElement> {
        return ParadoxLocalisationService.resolveRelatedDefinitions(element)
    }

    @Inferred
    fun isSpecialLocalisation(element: ParadoxLocalisationProperty): Boolean {
        // 存在一些特殊的本地化，不能直接用来渲染文本
        val file = element.containingFile ?: return false
        val fileName = file.name
        if (fileName.startsWith("name_system_")) return true // e.g., name_system_l_english.yml
        return false
    }

    @Inferred
    fun isRichText(text: String): Boolean {
        for ((i, c) in text.withIndex()) {
            // accept left bracket & do not check escape (`[[`)
            if (c == '[') return true
            // accept special markers && check escape
            if (c in "$£§#" && !text.isEscapedCharAt(i)) return true
        }
        return false
    }
}
