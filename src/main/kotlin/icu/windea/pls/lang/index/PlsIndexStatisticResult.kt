package icu.windea.pls.lang.index

import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.ParadoxIndexInfoType

@Suppress("unused")
data class PlsIndexStatisticResult(
    val configSymbol: Map<ParadoxGameType, Long>,
    val complexEnumValue: Map<ParadoxGameType, Long>,
    val definition: Map<ParadoxGameType, Long>,
    val definitionInjection: Map<ParadoxGameType, Long>,
    val merged: Map<ParadoxGameType, Map<ParadoxIndexInfoType<*>, Long>>,
) {
    val configSymbolTotal: Long = configSymbol.values.sum()
    val complexEnumValueTotal: Long = complexEnumValue.values.sum()
    val definitionTotal: Long = definition.values.sum()
    val definitionInjectionTotal: Long = definitionInjection.values.sum()
    val mergedTotal: Map<ParadoxIndexInfoType<*>, Long> = buildMap { merged.forEach { (_, v) -> v.forEach { (k1, v1) -> merge(k1, v1) { a, b -> a + b } } } }
}
