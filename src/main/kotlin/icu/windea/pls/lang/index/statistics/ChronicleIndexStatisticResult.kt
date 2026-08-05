package icu.windea.pls.lang.index.statistics

import icu.windea.pls.lang.index.ParadoxMergedIndexType
import icu.windea.pls.lang.index.constraints.ParadoxDefinitionIndexConstraint
import icu.windea.pls.model.ParadoxGameType

@Suppress("unused")
data class ChronicleIndexStatisticResult(
    val configSymbol: Map<ParadoxGameType, Long>,
    val complexEnumValue: Map<ParadoxGameType, Long>,
    val definition: Map<ParadoxGameType, Long>,
    val definitionConstrained: Map<ParadoxGameType, Map<ParadoxDefinitionIndexConstraint, Long>>,
    val definitionInjection: Map<ParadoxGameType, Long>,
    val merged: Map<ParadoxGameType, Map<ParadoxMergedIndexType<*>, Long>>,
) {
    val configSymbolTotal: Long = configSymbol.values.sum()
    val complexEnumValueTotal: Long = complexEnumValue.values.sum()
    val definitionTotal: Long = definition.values.sum()
    val definitionConstrainedTotal: Map<ParadoxDefinitionIndexConstraint, Long> = buildMap {
        definitionConstrained.forEach { (_, v) -> v.forEach { (k1, v1) -> merge(k1, v1) { a, b -> a + b } } }
    }
    val definitionInjectionTotal: Long = definitionInjection.values.sum()
    val mergedTotal: Map<ParadoxMergedIndexType<*>, Long> = buildMap {
        merged.forEach { (_, v) -> v.forEach { (k1, v1) -> merge(k1, v1) { a, b -> a + b } } }
    }
}
