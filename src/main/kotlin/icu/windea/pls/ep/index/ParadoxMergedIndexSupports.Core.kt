package icu.windea.pls.ep.index

import com.intellij.psi.PsiElement
import icu.windea.pls.core.readOrReadFrom
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.util.ReadWriteAccessC
import icu.windea.pls.core.util.optimized
import icu.windea.pls.core.withState
import icu.windea.pls.core.writeByte
import icu.windea.pls.core.writeOrWriteFrom
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.lang.index.ParadoxMergedIndexContext
import icu.windea.pls.lang.index.ParadoxMergedIndexScriptContext
import icu.windea.pls.lang.index.ParadoxMergedIndexThreadContext
import icu.windea.pls.lang.index.ParadoxMergedIndexTypes
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxLocalisationParameterLightElement
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.resolve.ParadoxParameterService
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxReferenceConstraint
import icu.windea.pls.model.index.ParadoxDynamicValueIndexInfo
import icu.windea.pls.model.index.ParadoxLocalisationParameterIndexInfo
import icu.windea.pls.model.index.ParadoxParameterIndexInfo
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptConditionParameter
import java.io.DataInput
import java.io.DataOutput

class ParadoxDynamicValueMergedIndexSupport : ParadoxMergedIndexSupportFromExpressionReferencesBase<ParadoxDynamicValueIndexInfo>() {
    // NOTE 3.0.0 do not make `compressComparator` depend on `name` - should keep declaration order per type (or context type)

    private val compressComparator = compareBy<ParadoxDynamicValueIndexInfo> { it.type }

    override val type = ParadoxMergedIndexTypes.DynamicValue
    override val constraint = ParadoxReferenceConstraint.DynamicValue

    override fun buildDataFromResolved(resolved: PsiElement, context: ParadoxMergedIndexContext) {
        if (resolved !is ParadoxDynamicValueLightElement) return
        for (dynamicValueType in resolved.types) {
            val info = ParadoxDynamicValueIndexInfo(resolved.name, dynamicValueType, resolved.readWriteAccess, resolved.gameType)
            addToFileData(info, context)
        }
    }

    override fun compressData(value: List<ParadoxDynamicValueIndexInfo>): List<ParadoxDynamicValueIndexInfo> {
        return value.sortedWith(compressComparator).distinct()
    }

    override fun saveData(storage: DataOutput, info: ParadoxDynamicValueIndexInfo, previousInfo: ParadoxDynamicValueIndexInfo?, gameType: ParadoxGameType) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.type }, { storage.writeUTFFast(it) })
        storage.writeByte(info.readWriteAccess.optimized())
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxDynamicValueIndexInfo?, gameType: ParadoxGameType): ParadoxDynamicValueIndexInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        val dynamicValueType = storage.readOrReadFrom(previousInfo, { it.type }, { storage.readUTFFast() })
        val readWriteAccess = storage.readByte().let { ReadWriteAccessC.deoptimized(it) }
        return ParadoxDynamicValueIndexInfo(name, dynamicValueType, readWriteAccess, gameType)
    }
}

class ParadoxParameterMergedIndexSupport : ParadoxMergedIndexSupportFromExpressionReferencesBase<ParadoxParameterIndexInfo>() {
    // NOTE 3.0.0 do not make `compressComparator` depend on `name` - should keep declaration order per type (or context type)

    private val compressComparator = compareBy<ParadoxParameterIndexInfo> { it.contextKey }

    override val type = ParadoxMergedIndexTypes.Parameter
    override val constraint = ParadoxReferenceConstraint.Parameter

    override fun buildDataFromResolved(resolved: PsiElement, context: ParadoxMergedIndexContext) {
        if (resolved !is ParadoxParameterLightElement) return
        val info = ParadoxParameterIndexInfo(resolved.name, resolved.contextKey, resolved.readWriteAccess, resolved.gameType)
        addToFileData(info, context)
    }

    override fun compressData(value: List<ParadoxParameterIndexInfo>): List<ParadoxParameterIndexInfo> {
        return value.sortedWith(compressComparator).distinct()
    }

    override fun saveData(storage: DataOutput, info: ParadoxParameterIndexInfo, previousInfo: ParadoxParameterIndexInfo?, gameType: ParadoxGameType) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.contextKey }, { storage.writeUTFFast(it) })
        storage.writeByte(info.readWriteAccess.optimized())
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxParameterIndexInfo?, gameType: ParadoxGameType): ParadoxParameterIndexInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        val contextKey = storage.readOrReadFrom(previousInfo, { it.contextKey }, { storage.readUTFFast() })
        val readWriteAccess = storage.readByte().let { ReadWriteAccessC.deoptimized(it) }
        return ParadoxParameterIndexInfo(name, contextKey, readWriteAccess, gameType)
    }
}

class ParadoxLocalisationParameterMergedIndexSupport : ParadoxMergedIndexSupportFromExpressionReferencesBase<ParadoxLocalisationParameterIndexInfo>() {
    // NOTE 3.0.0 do not make `compressComparator` depend on `name` - should keep declaration order per type (or context type)

    private val compressComparator = compareBy<ParadoxLocalisationParameterIndexInfo> { it.localisationName }

    override val type = ParadoxMergedIndexTypes.LocalisationParameter
    override val constraint = ParadoxReferenceConstraint.LocalisationParameter

    override fun buildDataFromResolved(resolved: PsiElement, context: ParadoxMergedIndexContext) {
        if (resolved !is ParadoxLocalisationParameterLightElement) return
        val info = ParadoxLocalisationParameterIndexInfo(resolved.name, resolved.localisationName, resolved.gameType)
        addToFileData(info, context)
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

class ParadoxParameterWithReadAccessMergedIndexSupport : ParadoxMergedIndexSupportBase<ParadoxParameterIndexInfo>() {
    // NOTE 3.0.0 do not make `compressComparator` depend on `name` - should keep declaration order per type (or context type)

    private val compressComparator = compareBy<ParadoxParameterIndexInfo> { it.contextKey }

    override val type = ParadoxMergedIndexTypes.ParameterWithReadAccess

    override fun buildData(element: PsiElement, context: ParadoxMergedIndexScriptContext) {
        // 3.0.1 although it's not very necessary
        if (!checkAvailable(context)) return

        val resolved = ParadoxMergedIndexThreadContext.isResolving.withState { resolve(element) }
        if (resolved == null) return
        val info = ParadoxParameterIndexInfo(resolved.name, resolved.contextKey, resolved.readWriteAccess, resolved.gameType)
        addToFileData(info, context)
    }

    private fun resolve(element: PsiElement): ParadoxParameterLightElement? {
        return when (element) {
            is ParadoxParameter -> ParadoxParameterService.resolveParameter(element)
            is ParadoxScriptConditionParameter -> ParadoxParameterService.resolveConditionParameter(element)
            else -> null
        }
    }

    override fun compressData(value: List<ParadoxParameterIndexInfo>): List<ParadoxParameterIndexInfo> {
        return value.sortedWith(compressComparator).distinct()
    }

    override fun saveData(storage: DataOutput, info: ParadoxParameterIndexInfo, previousInfo: ParadoxParameterIndexInfo?, gameType: ParadoxGameType) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.contextKey }, { storage.writeUTFFast(it) })
        storage.writeByte(info.readWriteAccess.optimized())
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxParameterIndexInfo?, gameType: ParadoxGameType): ParadoxParameterIndexInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        val contextKey = storage.readOrReadFrom(previousInfo, { it.contextKey }, { storage.readUTFFast() })
        val readWriteAccess = storage.readByte().let { ReadWriteAccessC.deoptimized(it) }
        return ParadoxParameterIndexInfo(name, contextKey, readWriteAccess, gameType)
    }
}
