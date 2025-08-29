package icu.windea.pls.lang.index

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import icu.windea.pls.config.util.CwtConfigSymbolManager
import icu.windea.pls.core.readIntFast
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.writeByte
import icu.windea.pls.core.writeIntFast
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.cwt.psi.CwtStringExpressionElement
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.deoptimizeValue
import icu.windea.pls.model.indexInfo.CwtConfigSymbolIndexInfo
import icu.windea.pls.model.optimizeValue
import java.io.DataInput
import java.io.DataOutput

/**
 * 用于索引CWT规则文件中的符号信息。
 */
class CwtConfigSymbolIndex : CwtConfigFileBasedIndex<List<CwtConfigSymbolIndexInfo>>() {
    override fun getName() = CwtConfigIndexKeys.Symbol

    override fun getVersion() = 73 // VERSION for 2.0.2

    override fun indexData(file: PsiFile, fileData: MutableMap<String, List<CwtConfigSymbolIndexInfo>>) {
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is CwtStringExpressionElement) {
                    visitStringExpressionElement(element)
                    return
                }
                super.visitElement(element)
            }

            private fun visitStringExpressionElement(element: CwtStringExpressionElement) {
                val infos = CwtConfigSymbolManager.getInfos(element)
                if(infos.isEmpty()) return
                infos.forEach { info ->
                    val list = fileData.getOrPut(info.type) { mutableListOf() } as MutableList
                    list += info
               }
            }
        })
    }

    override fun writeData(storage: DataOutput, value: List<CwtConfigSymbolIndexInfo>) {
        val size = value.size
        storage.writeIntFast(size)
        if (value.isEmpty()) return

        val firstInfo = value.first()
        storage.writeUTFFast(firstInfo.type)
        storage.writeByte(firstInfo.gameType.optimizeValue())
        value.forEach { info ->
            storage.writeUTFFast(info.name)
            storage.writeByte(info.readWriteAccess.optimizeValue())
            storage.writeIntFast(info.offset)
            storage.writeIntFast(info.elementOffset)
        }
    }

    override fun readData(storage: DataInput): List<CwtConfigSymbolIndexInfo> {
        val size = storage.readIntFast()
        if (size == 0) return emptyList()

        val type = storage.readUTFFast()
        val gameType = storage.readByte().deoptimizeValue<ParadoxGameType>()
        val list = mutableListOf<CwtConfigSymbolIndexInfo>()
        repeat(size) {
            val name = storage.readUTFFast()
            val readWriteAccess = storage.readByte().deoptimizeValue<ReadWriteAccessDetector.Access>()
            val offset = storage.readIntFast()
            val elementOffset = storage.readIntFast()
            list.add(CwtConfigSymbolIndexInfo(name, type, readWriteAccess, offset, elementOffset, gameType))
        }
        return list
    }
}
