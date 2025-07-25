package icu.windea.pls.model.constants

object CwtConfigTextPatternSets {
    val dynamicValueReference = listOf(
        CwtConfigTextPatterns.value,
        CwtConfigTextPatterns.valueSet,
        CwtConfigTextPatterns.dynamicValue,
    )
    val singleAliasReference = listOf(
        CwtConfigTextPatterns.singleAliasRight,
    )
    val aliasReference = listOf(
        CwtConfigTextPatterns.aliasName,
        CwtConfigTextPatterns.aliasMatchLeft,
        CwtConfigTextPatterns.aliasKeysField,
    )
}
