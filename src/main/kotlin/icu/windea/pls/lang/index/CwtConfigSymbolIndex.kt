package icu.windea.pls.lang.index

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import icu.windea.pls.config.util.CwtConfigSymbolManager
import icu.windea.pls.core.collections.asMutable
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.core.optimizer.forAccess
import icu.windea.pls.core.readIntFast
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.writeByte
import icu.windea.pls.core.writeIntFast
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.cwt.CwtFileType
import icu.windea.pls.cwt.psi.CwtStringExpressionElement
import icu.windea.pls.model.forGameType
import icu.windea.pls.model.index.CwtConfigSymbolIndexInfo
import java.io.DataInput
import java.io.DataOutput

/**
 * 规则文件中的各种符号信息的索引。
 */
class CwtConfigSymbolIndex : CwtConfigIndexInfoAwareFileBasedIndex<List<CwtConfigSymbolIndexInfo>, CwtConfigSymbolIndexInfo>() {
    override fun getName() = PlsIndexKeys.ConfigSymbol

    override fun getVersion() = PlsIndexVersions.ConfigSymbol

    override fun filterFile(file: VirtualFile): Boolean {
        // 仅判断文件类型，不判断是否从属于某个规则分组
        val fileType = file.fileType
        return fileType is CwtFileType
    }

    override fun indexData(psiFile: PsiFile): Map<String, List<CwtConfigSymbolIndexInfo>> {
        return buildMap {
            buildData(psiFile, this)
        }
    }

    private fun buildData(psiFile: PsiFile, fileData: MutableMap<String, List<CwtConfigSymbolIndexInfo>>) {
        psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is CwtStringExpressionElement) {
                    visitStringExpressionElement(element)
                    return
                }
                super.visitElement(element)
            }

            private fun visitStringExpressionElement(element: CwtStringExpressionElement) {
                val infos = CwtConfigSymbolManager.getInfos(element)
                if (infos.isEmpty()) return
                infos.forEach { info ->
                    fileData.getOrPut(info.type) { mutableListOf() }.asMutable() += info
                }
            }
        })
    }

    override fun saveValue(storage: DataOutput, value: List<CwtConfigSymbolIndexInfo>) {
        val size = value.size
        storage.writeIntFast(size)
        if (value.isEmpty()) return

        val firstInfo = value.first()
        storage.writeUTFFast(firstInfo.type)
        storage.writeByte(firstInfo.gameType.optimized(OptimizerRegistry.forGameType()))
        value.forEach { info ->
            storage.writeUTFFast(info.name)
            storage.writeByte(info.readWriteAccess.optimized(OptimizerRegistry.forAccess()))
            storage.writeIntFast(info.offset)
            storage.writeIntFast(info.elementOffset)
        }
    }

    override fun readValue(storage: DataInput): List<CwtConfigSymbolIndexInfo> {
        val size = storage.readIntFast()
        if (size == 0) return emptyList()

        val type = storage.readUTFFast()
        val gameType = storage.readByte().deoptimized(OptimizerRegistry.forGameType())
        val list = mutableListOf<CwtConfigSymbolIndexInfo>()
        repeat(size) {
            val name = storage.readUTFFast()
            val readWriteAccess = storage.readByte().deoptimized(OptimizerRegistry.forAccess())
            val offset = storage.readIntFast()
            val elementOffset = storage.readIntFast()
            list.add(CwtConfigSymbolIndexInfo(name, type, readWriteAccess, offset, elementOffset, gameType))
        }
        return list
    }
}
