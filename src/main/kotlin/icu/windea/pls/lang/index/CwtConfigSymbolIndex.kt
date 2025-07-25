package icu.windea.pls.lang.index

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.psi.symbols.*
import icu.windea.pls.model.*
import icu.windea.pls.model.indexInfo.*
import java.io.*

/**
 * 用于索引CWT规则文件中的符号信息。
 */
class CwtConfigSymbolIndex : CwtConfigFileBasedIndex<List<CwtConfigSymbolIndexInfo>>() {
    companion object {
        private const val VERSION = 70 //2.0.0-dev
    }

    override fun getName() = CwtConfigIndexManager.SymbolName

    override fun getVersion() = VERSION

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
                val info = CwtConfigSymbolManager.getSymbolInfo(element) ?: return
                val list = fileData.getOrPut(info.type) { mutableListOf() } as MutableList
                list += info
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
            val elementOffset = storage.readIntFast()
            list.add(CwtConfigSymbolIndexInfo(name, type, elementOffset, gameType))
        }
        return list
    }
}
