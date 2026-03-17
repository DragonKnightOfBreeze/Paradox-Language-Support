package icu.windea.pls.ep.index

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.core.optimizer.forAccess
import icu.windea.pls.core.readOrReadFrom
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.withState
import icu.windea.pls.core.writeByte
import icu.windea.pls.core.writeOrWriteFrom
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.index.ParadoxIndexInfoType
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxLocalisationParameterLightElement
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.model.index.ParadoxDynamicValueIndexInfo
import icu.windea.pls.model.index.ParadoxIndexInfo
import icu.windea.pls.model.index.ParadoxLocalisationParameterIndexInfo
import icu.windea.pls.model.index.ParadoxParameterIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import java.io.DataInput
import java.io.DataOutput

class ParadoxDynamicValueMergedIndexSupport : ParadoxMergedIndexSupport<ParadoxDynamicValueIndexInfo> {
    private val constraint = ParadoxResolveConstraint.DynamicValue
    private val compressComparator = compareBy<ParadoxDynamicValueIndexInfo>({ it.dynamicValueType }, { it.name })

    override val id = ParadoxIndexInfoType.DynamicValue.id

    override val type = ParadoxDynamicValueIndexInfo::class.java

    override fun buildDataForExpression(element: ParadoxScriptStringExpressionElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>, definitionInfo: ParadoxDefinitionInfo) {
        if (!constraint.canResolveReference(element)) return
        val references = ParadoxExpressionManager.getExpressionReferences(element) // use expression references only to optimize performance
        for (reference in references) {
            if (!constraint.canResolve(reference)) continue
            buildDataFromReference(reference, fileData)
        }
    }

    override fun buildDataForExpression(element: ParadoxLocalisationExpressionElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        if (!constraint.canResolveReference(element)) return
        val references = ParadoxExpressionManager.getExpressionReferences(element) // use expression references only to optimize performance
        for (reference in references) {
            if (!constraint.canResolve(reference)) continue
            buildDataFromReference(reference, fileData)
        }
    }

    private fun buildDataFromReference(reference: PsiReference, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        val resolved = withState(PlsStates.resolveForMergedIndex) { reference.resolve() }
        if (resolved !is ParadoxDynamicValueLightElement) return
        for (dynamicValueType in resolved.dynamicValueTypes) {
            val info = ParadoxDynamicValueIndexInfo(resolved.name, dynamicValueType, resolved.readWriteAccess, resolved.gameType)
            addToFileData(info, fileData)
        }
    }

    override fun compressData(value: List<ParadoxDynamicValueIndexInfo>): List<ParadoxDynamicValueIndexInfo> {
        return value.sortedWith(compressComparator).distinct()
    }

    override fun saveData(storage: DataOutput, info: ParadoxDynamicValueIndexInfo, previousInfo: ParadoxDynamicValueIndexInfo?, gameType: ParadoxGameType) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.dynamicValueType }, { storage.writeUTFFast(it) })
        storage.writeByte(info.readWriteAccess.optimized(OptimizerRegistry.forAccess()))
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxDynamicValueIndexInfo?, gameType: ParadoxGameType): ParadoxDynamicValueIndexInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        val dynamicValueType = storage.readOrReadFrom(previousInfo, { it.dynamicValueType }, { storage.readUTFFast() })
        val readWriteAccess = storage.readByte().deoptimized(OptimizerRegistry.forAccess())
        return ParadoxDynamicValueIndexInfo(name, dynamicValueType, readWriteAccess, gameType)
    }
}

class ParadoxParameterMergedIndexSupport : ParadoxMergedIndexSupport<ParadoxParameterIndexInfo> {
    private val constraint = ParadoxResolveConstraint.Parameter
    private val compressComparator = compareBy<ParadoxParameterIndexInfo>({ it.contextKey }, { it.name })

    override val id = ParadoxIndexInfoType.Parameter.id

    override val type = ParadoxParameterIndexInfo::class.java

    override fun buildData(element: PsiElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        if (element is ParadoxScriptExpressionElement) return // skip expression elements first
        if (!constraint.canResolveReference(element)) return
        val references = element.references
        for (reference in references) {
            if (!constraint.canResolve(reference)) continue
            buildDataFromReference(reference, fileData)
        }
    }

    override fun buildDataForExpression(element: ParadoxScriptStringExpressionElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>, definitionInfo: ParadoxDefinitionInfo) {
        if (!constraint.canResolveReference(element)) return
        val references = ParadoxExpressionManager.getExpressionReferences(element) // use expression references only to optimize performance
        for (reference in references) {
            if (!constraint.canResolve(reference)) continue
            buildDataFromReference(reference, fileData)
        }
    }

    private fun buildDataFromReference(reference: PsiReference, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        val resolved = withState(PlsStates.resolveForMergedIndex) { reference.resolve() }
        if (resolved !is ParadoxParameterLightElement) return
        // note that `element.startOffset` may not equal to actual `parameterElement.startOffset` (e.g. in a script value expression)
        val info = ParadoxParameterIndexInfo(resolved.name, resolved.contextKey, resolved.readWriteAccess, resolved.gameType)
        addToFileData(info, fileData)
    }

    override fun compressData(value: List<ParadoxParameterIndexInfo>): List<ParadoxParameterIndexInfo> {
        return value.sortedWith(compressComparator).distinct()
    }

    override fun saveData(storage: DataOutput, info: ParadoxParameterIndexInfo, previousInfo: ParadoxParameterIndexInfo?, gameType: ParadoxGameType) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.contextKey }, { storage.writeUTFFast(it) })
        storage.writeByte(info.readWriteAccess.optimized(OptimizerRegistry.forAccess()))
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxParameterIndexInfo?, gameType: ParadoxGameType): ParadoxParameterIndexInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        val contextKey = storage.readOrReadFrom(previousInfo, { it.contextKey }, { storage.readUTFFast() })
        val readWriteAccess = storage.readByte().deoptimized(OptimizerRegistry.forAccess())
        return ParadoxParameterIndexInfo(name, contextKey, readWriteAccess, gameType)
    }
}

class ParadoxLocalisationParameterMergedIndexSupport : ParadoxMergedIndexSupport<ParadoxLocalisationParameterIndexInfo> {
    private val constraint = ParadoxResolveConstraint.LocalisationParameter
    private val compressComparator = compareBy<ParadoxLocalisationParameterIndexInfo>({ it.localisationName }, { it.name })

    override val id = ParadoxIndexInfoType.LocalisationParameter.id

    override val type = ParadoxLocalisationParameterIndexInfo::class.java

    override fun buildDataForExpression(element: ParadoxScriptStringExpressionElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>, definitionInfo: ParadoxDefinitionInfo) {
        if (!constraint.canResolveReference(element)) return
        val references = ParadoxExpressionManager.getExpressionReferences(element) // use expression references only to optimize performance
        for (reference in references) {
            if (!constraint.canResolve(reference)) continue
            buildDataFromReference(reference, fileData)
        }
    }

    private fun buildDataFromReference(reference: PsiReference, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        val resolved = withState(PlsStates.resolveForMergedIndex) { reference.resolve() }
        if (resolved !is ParadoxLocalisationParameterLightElement) return
        val info = ParadoxLocalisationParameterIndexInfo(resolved.name, resolved.localisationName, resolved.gameType)
        addToFileData(info, fileData)
    }

    override fun compressData(value: List<ParadoxLocalisationParameterIndexInfo>): List<ParadoxLocalisationParameterIndexInfo> {
        return value.sortedWith(compressComparator).distinct()
    }

    override fun saveData(storage: DataOutput, info: ParadoxLocalisationParameterIndexInfo, previousInfo: ParadoxLocalisationParameterIndexInfo?, gameType: ParadoxGameType) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.localisationName }, { storage.writeUTFFast(it) })
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxLocalisationParameterIndexInfo?, gameType: ParadoxGameType): ParadoxLocalisationParameterIndexInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        val localisationName = storage.readOrReadFrom(previousInfo, { it.localisationName }, { storage.readUTFFast() })
        return ParadoxLocalisationParameterIndexInfo(name, localisationName, gameType)
    }
}
