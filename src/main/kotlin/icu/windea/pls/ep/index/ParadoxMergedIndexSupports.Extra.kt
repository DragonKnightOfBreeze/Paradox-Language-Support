package icu.windea.pls.ep.index

import com.intellij.psi.PsiElement
import icu.windea.pls.core.readOrReadFrom
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.writeOrWriteFrom
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.lang.index.ParadoxMergedIndexContext
import icu.windea.pls.lang.index.ParadoxMergedIndexTypes
import icu.windea.pls.lang.psi.light.ParadoxShaderEffectLightElement
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxReferenceConstraint
import icu.windea.pls.model.index.ParadoxMeshLocatorIndexInfo
import icu.windea.pls.model.index.ParadoxShaderEffectIndexInfo
import java.io.DataInput
import java.io.DataOutput

class ParadoxShaderEffectMergedIndexSupport : ParadoxMergedIndexSupportFromExpressionReferencesBase<ParadoxShaderEffectIndexInfo>() {
    override val type = ParadoxMergedIndexTypes.ShaderEffect
    override val constraint = ParadoxReferenceConstraint.ShaderEffect

    override fun buildDataFromResolved(resolved: PsiElement, context: ParadoxMergedIndexContext) {
        if (resolved !is ParadoxShaderEffectLightElement) return
        val info = ParadoxShaderEffectIndexInfo(resolved.name, resolved.gameType)
        addToFileData(info, context)
    }

    override fun compressData(value: List<ParadoxShaderEffectIndexInfo>): List<ParadoxShaderEffectIndexInfo> {
        return value.distinct()
    }

    override fun saveData(storage: DataOutput, info: ParadoxShaderEffectIndexInfo, previousInfo: ParadoxShaderEffectIndexInfo?, gameType: ParadoxGameType) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxShaderEffectIndexInfo?, gameType: ParadoxGameType): ParadoxShaderEffectIndexInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        return ParadoxShaderEffectIndexInfo(name, gameType)
    }
}

class ParadoxMeshLocatorMergedIndexSupport : ParadoxMergedIndexSupportFromExpressionReferencesBase<ParadoxMeshLocatorIndexInfo>() {
    override val type = ParadoxMergedIndexTypes.MeshLocator
    override val constraint = ParadoxReferenceConstraint.MeshLocator

    override fun buildDataFromResolved(resolved: PsiElement, context: ParadoxMergedIndexContext) {
        if (resolved !is ParadoxShaderEffectLightElement) return
        val info = ParadoxShaderEffectIndexInfo(resolved.name, resolved.gameType)
        addToFileData(info, context)
    }

    override fun compressData(value: List<ParadoxMeshLocatorIndexInfo>): List<ParadoxMeshLocatorIndexInfo> {
        return value.distinct()
    }

    override fun saveData(storage: DataOutput, info: ParadoxMeshLocatorIndexInfo, previousInfo: ParadoxMeshLocatorIndexInfo?, gameType: ParadoxGameType) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxMeshLocatorIndexInfo?, gameType: ParadoxGameType): ParadoxMeshLocatorIndexInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        return ParadoxMeshLocatorIndexInfo(name, gameType)
    }
}
