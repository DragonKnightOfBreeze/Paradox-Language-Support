package icu.windea.pls.ep.presentation

import com.intellij.openapi.util.Key
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

abstract class ParadoxDefinitionPresentationProviderBase<T: ParadoxDefinitionPresentationData>() : ParadoxDefinitionPresentationProvider<T> {
    private val cachedPresentationKey: Key<CachedValue<T>> by lazy { createKey("cached.presentation.by.${javaClass.name}") }

    override fun getPresentationData(definition: ParadoxScriptDefinitionElement): T? {
        return doGetPresentationDataFromCache(definition)
    }

    private fun doGetPresentationDataFromCache(definition: ParadoxScriptDefinitionElement): T? {
        return CachedValuesManager.getCachedValue(definition, cachedPresentationKey) {
            val value = doGetPresentationData(definition)
            value.withDependencyItems(
                definition,
                ParadoxModificationTrackers.FileTracker,
            )
        }
    }

    private fun doGetPresentationData(definition: ParadoxScriptDefinitionElement): T? {
        return type.getConstructor(ParadoxScriptDefinitionElement::class.java).newInstance(definition)
    }
}
