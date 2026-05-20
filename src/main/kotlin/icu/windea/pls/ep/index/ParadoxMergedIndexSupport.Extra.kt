package icu.windea.pls.ep.index

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.match.CwtConfigExpressionMatchService
import icu.windea.pls.core.readOrReadFrom
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.writeOrWriteFrom
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.model.ParadoxDefinitionCandidateInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.ParadoxIndexInfo
import icu.windea.pls.model.index.ParadoxIndexInfoTypes
import icu.windea.pls.model.index.ParadoxMeshLocatorIndexInfo
import icu.windea.pls.model.index.ParadoxShaderEffectIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import java.io.DataInput
import java.io.DataOutput

class ParadoxShaderEffectMergedIndexSupport : ParadoxMergedIndexSupport<ParadoxShaderEffectIndexInfo> {
    private val compressComparator = compareBy<ParadoxShaderEffectIndexInfo> { it.name }

    override val indexInfoType = ParadoxIndexInfoTypes.ShaderEffect

    override fun buildDataForExpression(element: ParadoxScriptStringExpressionElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>, info: ParadoxDefinitionCandidateInfo?, configs: List<CwtMemberConfig<*>>) {
        val expression = element.value
        if (expression.isEmpty() || expression.isParameterized()) return // skip if expression is empty or parameterized
        val config = configs.find { matchesConfig(it) }
        if (config == null) return

        val name = element.value
        val info = ParadoxShaderEffectIndexInfo(name, config.configGroup.gameType)
        addToFileData(info, fileData)
    }

    private fun matchesConfig(config: CwtMemberConfig<*>): Boolean {
        return CwtConfigExpressionMatchService.matchesShaderEffect(config.configExpression)
    }

    override fun compressData(value: List<ParadoxShaderEffectIndexInfo>): List<ParadoxShaderEffectIndexInfo> {
        return value.sortedWith(compressComparator).distinct()
    }

    override fun saveData(storage: DataOutput, info: ParadoxShaderEffectIndexInfo, previousInfo: ParadoxShaderEffectIndexInfo?, gameType: ParadoxGameType) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxShaderEffectIndexInfo?, gameType: ParadoxGameType): ParadoxShaderEffectIndexInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        return ParadoxShaderEffectIndexInfo(name, gameType)
    }
}

class ParadoxMeshLocatorMergedIndexSupport : ParadoxMergedIndexSupport<ParadoxMeshLocatorIndexInfo> {
    private val compressComparator = compareBy<ParadoxMeshLocatorIndexInfo> { it.name }

    override val indexInfoType = ParadoxIndexInfoTypes.MeshLocator

    override fun buildDataForExpression(element: ParadoxScriptStringExpressionElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>, info: ParadoxDefinitionCandidateInfo?, configs: List<CwtMemberConfig<*>>) {
        val expression = element.value
        if (expression.isEmpty() || expression.isParameterized()) return // skip if expression is empty or parameterized
        val config = configs.find { matchesConfig(it) }
        if (config == null) return

        val name = element.value
        val info = ParadoxMeshLocatorIndexInfo(name, config.configGroup.gameType)
        addToFileData(info, fileData)
    }

    private fun matchesConfig(config: CwtMemberConfig<*>): Boolean {
        return CwtConfigExpressionMatchService.matchesMeshLocator(config.configExpression)
    }

    override fun compressData(value: List<ParadoxMeshLocatorIndexInfo>): List<ParadoxMeshLocatorIndexInfo> {
        return value.sortedWith(compressComparator).distinct()
    }

    override fun saveData(storage: DataOutput, info: ParadoxMeshLocatorIndexInfo, previousInfo: ParadoxMeshLocatorIndexInfo?, gameType: ParadoxGameType) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
    }

    override fun readData(storage: DataInput, previousInfo: ParadoxMeshLocatorIndexInfo?, gameType: ParadoxGameType): ParadoxMeshLocatorIndexInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        return ParadoxMeshLocatorIndexInfo(name, gameType)
    }
}
