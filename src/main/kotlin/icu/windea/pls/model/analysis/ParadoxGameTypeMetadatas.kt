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
    override val gameEntries: Set<String> get() = gameMainEntries + gameExtraEntries
    override val modEntries: Set<String> get() = modMainEntries + modExtraEntries

    override val gameEntriesWithPaths: Map<String, List<String>> = gameEntries.ifEmpty { setOf("") }.toEntryMap()
    override val modEntriesWithPaths: Map<String, List<String>> = modEntries.ifEmpty { setOf("") }.toEntryMap()

    private fun Set<String>.toEntryMap() = sortedDescending().associateWith { it.splitEntry() }.toMap().optimized()

    private fun String.splitEntry() = if (isEmpty()) emptyList() else split('/')
}

data class ParadoxFallbackGameTypeMetadata(
    override val gameType: ParadoxGameType
) : ParadoxGameTypeMetadata {
    override val gameMainEntries: Set<String> get() = emptySet()
    override val gameExtraEntries: Set<String> get() = emptySet()
    override val modMainEntries: Set<String> get() = emptySet()
    override val modExtraEntries: Set<String> get() = emptySet()
    override val gameEntries: Set<String> get() = emptySet()
    override val modEntries: Set<String> get() = emptySet()
    override val gameEntriesWithPaths: Map<String, List<String>> get() = emptyMap()
    override val modEntriesWithPaths: Map<String, List<String>> get() = emptyMap()
    override val executablePaths: Set<String> get() = emptySet()
}
