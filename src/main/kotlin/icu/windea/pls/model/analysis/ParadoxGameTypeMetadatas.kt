package icu.windea.pls.model.analysis

import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.paths.ParadoxPath

private val fallbackMainEntries = setOf("")
private val fallbackEntryPaths = setOf(ParadoxPath.resolveEmpty())

private fun Set<String>.toEntryPaths() = sortedDescending().mapTo(mutableSetOf()) { ParadoxPath.resolve(it) }

data class ParadoxDefaultGameTypeMetadata(
    override val gameType: ParadoxGameType,
    override val gameMainEntries: Set<String> = emptySet(),
    override val gameExtraEntries: Set<String> = emptySet(),
    override val modMainEntries: Set<String> = emptySet(),
    override val modExtraEntries: Set<String> = emptySet(),
    override val executablePaths: Set<String> = emptySet(),
) : ParadoxGameTypeMetadata {
    override val gameEntries: Set<String> = gameMainEntries + gameExtraEntries
    override val modEntries: Set<String> = modMainEntries + modExtraEntries

    override val gameEntryPaths: Set<ParadoxPath> = gameEntries.toEntryPaths()
    override val modEntryPaths: Set<ParadoxPath> = modEntries.toEntryPaths()
}

data class ParadoxFallbackGameTypeMetadata(
    override val gameType: ParadoxGameType
) : ParadoxGameTypeMetadata {
    override val gameMainEntries: Set<String> get() = fallbackMainEntries
    override val gameExtraEntries: Set<String> get() = emptySet()
    override val modMainEntries: Set<String> get() = fallbackMainEntries
    override val modExtraEntries: Set<String> get() = emptySet()
    override val gameEntries: Set<String> get() = fallbackMainEntries
    override val modEntries: Set<String> get() = fallbackMainEntries
    override val gameEntryPaths: Set<ParadoxPath> get() = fallbackEntryPaths
    override val modEntryPaths: Set<ParadoxPath> get() = fallbackEntryPaths
    override val executablePaths: Set<String> get() = emptySet()
}
