package icu.windea.pls.model.analysis

import icu.windea.pls.core.optimized
import icu.windea.pls.model.ParadoxGameType

data class ParadoxJsonBasedGameTypeMetadata(
    override val gameType: ParadoxGameType,
    override val gameMainEntries: Set<String> = emptySet(),
    override val gameExtraEntries: Set<String> = emptySet(),
    override val modMainEntries: Set<String> = emptySet(),
    override val modExtraEntries: Set<String> = emptySet(),
    override val executablePaths: Set<String> = emptySet(),
) : ParadoxGameTypeMetadata {
    override val gameEntryMap: Map<String, Set<String>> = (gameMainEntries.ifEmpty { setOf("") } + gameExtraEntries).toEntryMap()
    override val modEntryMap: Map<String, Set<String>> = (modMainEntries.ifEmpty { setOf("") } + modExtraEntries).toEntryMap()

    private fun Set<String>.toEntryMap() = sortedDescending().associateWith { it.splitEntry() }.toMap().optimized()

    private fun String.splitEntry() = if (isEmpty()) emptySet() else split('/').toSet()
}

data class ParadoxFallbackGameTypeMetadata(
    override val gameType: ParadoxGameType
) : ParadoxGameTypeMetadata {
    override val gameMainEntries: Set<String> get() = emptySet()
    override val gameExtraEntries: Set<String> get() = emptySet()
    override val gameEntryMap: Map<String, Set<String>> get() = emptyMap()
    override val modMainEntries: Set<String> get() = emptySet()
    override val modExtraEntries: Set<String> get() = emptySet()
    override val modEntryMap: Map<String, Set<String>> get() = emptyMap()
    override val executablePaths: Set<String> get() = emptySet()
}
