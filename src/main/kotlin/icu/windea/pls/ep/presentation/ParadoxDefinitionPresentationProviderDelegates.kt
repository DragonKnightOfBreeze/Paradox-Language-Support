package icu.windea.pls.ep.presentation

import icu.windea.pls.lang.expression.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

object ParadoxDefinitionPresentationProviderDelegates {
    class FromDefinitionType<T : ParadoxDefinitionPresentation>(
        override val type: Class<T>,
        definitionType: String
    ) : ParadoxDefinitionPresentationProviderBase<T>() {
        private val typeExpression = ParadoxDefinitionTypeExpression.resolve(definitionType)

        override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
            return typeExpression.matches(definitionInfo)
        }
    }

    inline fun <reified T : ParadoxDefinitionPresentation> create(definitionType: String): ParadoxDefinitionPresentationProvider<T> {
        return FromDefinitionType(T::class.java, definitionType)
    }
}
