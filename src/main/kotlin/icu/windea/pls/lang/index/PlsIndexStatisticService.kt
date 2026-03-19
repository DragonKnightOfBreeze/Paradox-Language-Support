package icu.windea.pls.lang.index

import icu.windea.pls.PlsFacade
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.ParadoxIndexInfoType
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Suppress("unused")
object PlsIndexStatisticService {
    var recordIndexStats = PlsFacade.Capacities.recordIndexStats()

    private val configSymbolCounters = ConcurrentHashMap<ParadoxGameType, AtomicLong>()
    private val complexEnumValueCounters = ConcurrentHashMap<ParadoxGameType, AtomicLong>()
    private val definitionCounters = ConcurrentHashMap<ParadoxGameType, AtomicLong>()
    private val definitionInjectionCounters = ConcurrentHashMap<ParadoxGameType, AtomicLong>()
    private val mergedCounters = ConcurrentHashMap<ParadoxGameType, ConcurrentHashMap<ParadoxIndexInfoType<*>, AtomicLong>>()

    fun collectResult(): PlsIndexStatisticResult {
        return PlsIndexStatisticResult(
            configSymbolCounters.mapValues { (_, v) -> v.get() },
            complexEnumValueCounters.mapValues { (_, v) -> v.get() },
            definitionCounters.mapValues { (_, v) -> v.get() },
            definitionInjectionCounters.mapValues { (_, v) -> v.get() },
            mergedCounters.mapValues { (_, v) -> v.mapValues { (_, v1) -> v1.get() } },
        )
    }

    fun recordConfigSymbol(gameType: ParadoxGameType) {
        if (!recordIndexStats) return
        val counter = configSymbolCounters.getOrPut(gameType) { AtomicLong() }
        counter.incrementAndGet()
    }

    fun recordComplexEnumValue(gameType: ParadoxGameType) {
        if (!recordIndexStats) return
        val counter = complexEnumValueCounters.computeIfAbsent(gameType) { AtomicLong() }
        counter.incrementAndGet()
    }

    fun recordDefinition(gameType: ParadoxGameType) {
        if (!recordIndexStats) return
        val counter = definitionCounters.computeIfAbsent(gameType) { AtomicLong() }
        counter.incrementAndGet()
    }

    fun recordDefinitionInjection(gameType: ParadoxGameType) {
        if (!recordIndexStats) return
        val counter = definitionInjectionCounters.computeIfAbsent(gameType) { AtomicLong() }
        counter.incrementAndGet()
    }

    fun recordMerged(gameType: ParadoxGameType, indexInfoType: ParadoxIndexInfoType<*>) {
        if (!recordIndexStats) return
        val counter = mergedCounters.computeIfAbsent(gameType) { ConcurrentHashMap() }.computeIfAbsent(indexInfoType) { AtomicLong() }
        counter.incrementAndGet()
    }

    fun cleanUp() {
        configSymbolCounters.clear()
        complexEnumValueCounters.clear()
        definitionCounters.clear()
        definitionInjectionCounters.clear()
        mergedCounters.clear()
    }
}
