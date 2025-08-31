package icu.windea.pls.ep.presentation

import icu.windea.pls.lang.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

object ParadoxDefinitionPresentationProviderDelegates {
    class FromDefinitionType<T : ParadoxDefinitionPresentationData>(
        override val type: Class<T>,
        definitionType: String
    ) : ParadoxDefinitionPresentationProviderBase<T>() {
        private val typeExpression = ParadoxDefinitionTypeExpression.resolve(definitionType)

        override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
            return typeExpression.matches(definitionInfo)
        }
    }

    inline fun <reified T : ParadoxDefinitionPresentationData> create(definitionType: String): ParadoxDefinitionPresentationProvider<T> {
        return FromDefinitionType(T::class.java, definitionType)
    }
}
