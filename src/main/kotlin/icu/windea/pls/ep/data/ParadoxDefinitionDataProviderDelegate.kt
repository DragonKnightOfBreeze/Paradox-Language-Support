package icu.windea.pls.ep.data

import icu.windea.pls.lang.expression.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

object ParadoxDefinitionDataProviderDelegates {
    class FromDefinitionType<T : ParadoxDefinitionData>(
        override val dataType: Class<T>,
        definitionType: String
    ) : ParadoxDefinitionDataProviderBase<T>() {
        private val typeExpression = ParadoxDefinitionTypeExpression.resolve(definitionType)

        override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
            return typeExpression.matches(definitionInfo)
        }
    }

    inline fun <reified T : ParadoxDefinitionData> create(definitionType: String): ParadoxDefinitionDataProvider<T> {
        return FromDefinitionType(T::class.java, definitionType)
    }
}

