package icu.windea.pls.model.constants

import icu.windea.pls.model.constants.CwtConfigTextPatterns as C

@Suppress("unused")
object CwtConfigTextPatternSets {
    val dynamicValueReference = arrayOf(C.value, C.valueSet, C.dynamicValue)
    val singleAliasReference = arrayOf(C.singleAliasRight)
    val aliasReference = arrayOf(C.aliasName, C.aliasMatchLeft, C.aliasKeysField)
}
