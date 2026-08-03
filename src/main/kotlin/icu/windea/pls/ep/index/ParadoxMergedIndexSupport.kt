package icu.windea.pls.ep.index

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.index.ParadoxMergedIndex
import icu.windea.pls.lang.index.ParadoxMergedIndexCsvContext
import icu.windea.pls.lang.index.ParadoxMergedIndexLocalisationContext
import icu.windea.pls.lang.index.ParadoxMergedIndexScriptContext
import icu.windea.pls.lang.index.ParadoxMergedIndexType
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
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
    val type: ParadoxMergedIndexType<T>

    fun buildData(element: PsiElement, context: ParadoxMergedIndexScriptContext) {}

    fun buildDataForExpression(element: ParadoxScriptStringExpressionElement, context: ParadoxMergedIndexScriptContext) {}

    fun buildDataForExpression(element: ParadoxLocalisationExpressionElement, context: ParadoxMergedIndexLocalisationContext) {}

    fun buildDataForExpression(element: ParadoxCsvExpressionElement, context: ParadoxMergedIndexCsvContext) {}

    fun compressData(value: List<T>): List<T> = value

    fun saveData(storage: DataOutput, info: T, previousInfo: T?, gameType: ParadoxGameType)

    fun readData(storage: DataInput, previousInfo: T?, gameType: ParadoxGameType): T

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxMergedIndexSupport<*>>("icu.windea.pls.mergedIndexSupport")
    }
}
