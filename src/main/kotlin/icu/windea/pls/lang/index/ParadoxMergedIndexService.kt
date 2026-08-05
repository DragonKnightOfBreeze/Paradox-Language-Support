package icu.windea.pls.lang.index

import com.google.common.collect.ImmutableSet
import com.intellij.psi.PsiElement
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.ep.index.ParadoxMergedIndexOptimizer
import icu.windea.pls.ep.index.ParadoxMergedIndexSupport
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.ParadoxDefinitionCandidateInfo
import icu.windea.pls.model.index.ParadoxIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

@Optimized
object ParadoxMergedIndexService {
    fun getAvailableTypes(file: ParadoxScriptFile, optimizers: List<ParadoxMergedIndexOptimizer>): Set<ParadoxMergedIndexType<*>> {
        val builder = ImmutableSet.builder<ParadoxMergedIndexType<*>>()
        optimizers.forEachFast { optimizer -> builder.addAll(optimizer.getAvailableTypes(file)) }
        return builder.build()
    }

    fun getAvailableTypes(file: ParadoxLocalisationFile, optimizers: List<ParadoxMergedIndexOptimizer>): Set<ParadoxMergedIndexType<*>> {
        val builder = ImmutableSet.builder<ParadoxMergedIndexType<*>>()
        optimizers.forEachFast { optimizer -> builder.addAll(optimizer.getAvailableTypes(file)) }
        return builder.build()
    }

    fun getAvailableTypes(file: ParadoxCsvFile, optimizers: List<ParadoxMergedIndexOptimizer>): Set<ParadoxMergedIndexType<*>> {
        val builder = ImmutableSet.builder<ParadoxMergedIndexType<*>>()
        optimizers.forEachFast { optimizer -> builder.addAll(optimizer.getAvailableTypes(file)) }
        return builder.build()
    }

    fun getAvailableTypes(definitionCandidateInfo: ParadoxDefinitionCandidateInfo, optimizers: List<ParadoxMergedIndexOptimizer>): Set<ParadoxMergedIndexType<*>> {
        val builder = ImmutableSet.builder<ParadoxMergedIndexType<*>>()
        optimizers.forEachFast { optimizer -> builder.addAll(optimizer.getAvailableTypes(definitionCandidateInfo)) }
        return builder.build()
    }

    fun buildData(element: PsiElement, context: ParadoxMergedIndexScriptContext, supports: List<ParadoxMergedIndexSupport<*>>) {
        supports.forEachFast { support -> support.buildData(element, context) }
    }

    fun buildDataForExpression(element: ParadoxScriptStringExpressionElement, context: ParadoxMergedIndexScriptContext, supports: List<ParadoxMergedIndexSupport<*>>) {
        supports.forEachFast { support -> support.buildDataForExpression(element, context) }
    }

    fun buildDataForExpression(element: ParadoxLocalisationExpressionElement, context: ParadoxMergedIndexLocalisationContext, supports: List<ParadoxMergedIndexSupport<*>>) {
        supports.forEachFast { support -> support.buildDataForExpression(element, context) }
    }

    fun buildDataForExpression(element: ParadoxCsvExpressionElement, context: ParadoxMergedIndexCsvContext, supports: List<ParadoxMergedIndexSupport<*>>) {
        supports.forEachFast { support -> support.buildDataForExpression(element, context) }
    }

    fun compressData(fileData: MutableMap<String, List<ParadoxIndexInfo>>, supports: List<ParadoxMergedIndexSupport<*>>) {
        if (fileData.isEmpty()) return
        if (supports.isEmpty()) return
        for (key in fileData.keys) {
            val oldValue = fileData.getValue(key)
            if (oldValue.size <= 1) continue
            val support = getSupportOrUnsupported(key, supports)
            val newValue = support.compressData(oldValue)
            fileData[key] = newValue
        }
    }

    fun getSupportOrUnsupported(key: String, supports: List<ParadoxMergedIndexSupport<*>>): ParadoxMergedIndexSupport<ParadoxIndexInfo> {
        return supports.findFast { support -> support.type.key == key }?.castOrNull() ?: throw UnsupportedOperationException()
    }

    fun getSupportOrUnsupported(type: Class<ParadoxIndexInfo>, supports: List<ParadoxMergedIndexSupport<*>>): ParadoxMergedIndexSupport<ParadoxIndexInfo> {
        return supports.findFast { support -> support.type.type == type }?.castOrNull() ?: throw UnsupportedOperationException()
    }
}
