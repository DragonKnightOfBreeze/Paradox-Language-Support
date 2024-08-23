package icu.windea.pls.ep.data

import icu.windea.pls.lang.expression.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

abstract class ParadoxDefinitionDataProviderBase<T: ParadoxDefinitionData>(
    val definitionType: String
): ParadoxDefinitionDataProvider<T>() {
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return ParadoxDefinitionTypeExpression.resolve(definitionType).matches(definitionInfo) 
    }
}
