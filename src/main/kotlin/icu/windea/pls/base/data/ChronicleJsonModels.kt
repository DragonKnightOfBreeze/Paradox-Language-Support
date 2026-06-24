package icu.windea.pls.base.data

import icu.windea.pls.model.ParadoxGameType

data class ParadoxGameTypeMetadataJson(
    val gameType: ParadoxGameType,
    val gameMainEntries: Set<String> = singleEmptyStringSet,
    val gameExtraEntries: Set<String> = emptySet(),
    val modMainEntries: Set<String> = singleEmptyStringSet,
    val modExtraEntries: Set<String> = emptySet(),
    val executablePaths: Set<String> = emptySet(),
)

data class CwtConfigGroupDataJson(
    val gameType: ParadoxGameType,
    val aliasNamesSupportScope: Set<String> = emptySet(),
    val typesSupportScope: Set<String> = emptySet(),
    val typesIndirectSupportScope: Set<String> = emptySet(),
    val typesSkipCheckSystemScope: Set<String> = emptySet(),
    val typesSupportParameters: Set<String> = emptySet(),
    val typesSupportScopeContextInference: Set<String> = emptySet(),
)

private val singleEmptyStringSet = setOf("")
