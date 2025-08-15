package icu.windea.pls.model.constants

import icu.windea.pls.model.constants.CwtConfigTextPatterns as C

object CwtConfigTextPatternSets {
    val dynamicValueReference = setOf(C.value, C.valueSet, C.dynamicValue)
    val singleAliasReference = setOf(C.singleAliasRight)
    val aliasReference = setOf(C.aliasName, C.aliasMatchLeft, C.aliasKeysField)
}
