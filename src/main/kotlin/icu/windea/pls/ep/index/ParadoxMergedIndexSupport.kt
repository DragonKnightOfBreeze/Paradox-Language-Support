package icu.windea.pls.ep.index

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.collections.asMutable
import icu.windea.pls.lang.index.ParadoxMergedIndex
import icu.windea.pls.lang.index.PlsIndexStatisticService
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.ParadoxIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import java.io.DataInput
import java.io.DataOutput

/**
 * 提供对在合并索引中处理的各种索引数据的支持。包括构建、压缩、保存和读取等功能。
 *
 * @see ParadoxMergedIndex
 * @see ParadoxIndexInfo
 */
interface ParadoxMergedIndexSupport<T : ParadoxIndexInfo> {
    val id: Byte

    val type: Class<T>

    fun buildData(element: PsiElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {}

    fun buildDataForExpression(element: ParadoxScriptStringExpressionElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>, definitionInfo: ParadoxDefinitionInfo?) {}

    fun buildDataForExpression(element: ParadoxScriptStringExpressionElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>, definitionInfo: ParadoxDefinitionInfo, configs: List<CwtMemberConfig<*>>) {}

    fun buildDataForExpression(element: ParadoxLocalisationExpressionElement, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {}

    fun compressData(value: List<T>): List<T> = value

    fun saveData(storage: DataOutput, info: T, previousInfo: T?, gameType: ParadoxGameType)

    fun readData(storage: DataInput, previousInfo: T?, gameType: ParadoxGameType): T

    fun <T : ParadoxIndexInfo> addToFileData(info: T, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        PlsIndexStatisticService.recordMerged(info.gameType, id)

        fileData.getOrPut(id.toString()) { mutableListOf() }.asMutable() += info
    }

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxMergedIndexSupport<*>>("icu.windea.pls.mergedIndexSupport")
    }
}
