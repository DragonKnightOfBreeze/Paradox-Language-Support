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
    override val gameEntriesWithPaths: Map<String, List<String>> = (gameMainEntries.ifEmpty { listOf("") } + gameExtraEntries).toEntryMap()
    override val modEntriesWithPaths: Map<String, List<String>> = (modMainEntries.ifEmpty { listOf("") } + modExtraEntries).toEntryMap()

    private fun List<String>.toEntryMap() = sortedDescending().associateWith { it.splitEntry() }.toMap().optimized()

    private fun String.splitEntry() = if (isEmpty()) emptyList() else split('/')
}

data class ParadoxFallbackGameTypeMetadata(
    override val gameType: ParadoxGameType
) : ParadoxGameTypeMetadata {
    override val gameMainEntries: Set<String> get() = emptySet()
    override val gameExtraEntries: Set<String> get() = emptySet()
    override val modMainEntries: Set<String> get() = emptySet()
    override val modExtraEntries: Set<String> get() = emptySet()
    override val gameEntriesWithPaths: Map<String, List<String>> get() = emptyMap()
    override val modEntriesWithPaths: Map<String, List<String>> get() = emptyMap()
    override val executablePaths: Set<String> get() = emptySet()
}
