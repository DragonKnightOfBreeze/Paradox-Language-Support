package icu.windea.pls.ep.presentation

import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

abstract class ParadoxDefinitionPresentationProviderBase<T: ParadoxDefinitionPresentation>() : ParadoxDefinitionPresentationProvider<T> {
    private val cachedPresentationKey: Key<CachedValue<T>> by lazy { createKey("cached.presentation.by.${javaClass.name}") }

    override fun getPresentation(definition: ParadoxScriptDefinitionElement): T? {
        return CachedValuesManager.getCachedValue(definition, cachedPresentationKey) {
            val value = doGetPresentation(definition)
            value.withDependencyItems(
                definition,
                ParadoxModificationTrackers.FileTracker,
            )
        }
    }

    private fun doGetPresentation(definition: ParadoxScriptDefinitionElement): T? {
        return type.getConstructor(ParadoxScriptDefinitionElement::class.java).newInstance(definition)
    }
}
