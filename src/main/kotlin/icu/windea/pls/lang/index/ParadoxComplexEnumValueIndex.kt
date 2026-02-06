package icu.windea.pls.lang.index

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.util.startOffset
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.core.readIntFast
import icu.windea.pls.core.readOrReadFrom
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.writeByte
import icu.windea.pls.core.writeIntFast
import icu.windea.pls.core.writeOrWriteFrom
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxComplexEnumValueManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.model.forGameType
import icu.windea.pls.model.index.ParadoxComplexEnumValueIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isExpression
import java.io.DataInput
import java.io.DataOutput

class ParadoxComplexEnumValueIndex : IndexInfoAwareFileBasedIndex<List<ParadoxComplexEnumValueIndexInfo>>() {
    companion object {
        const val LazyIndexKey = "__lazy__"
    }

    override fun getName() = PlsIndexKeys.ComplexEnumValue

    override fun getVersion() = PlsIndexVersions.ComplexEnumValue

    override fun filterFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if (fileType != ParadoxScriptFileType) return false
        if (file.fileInfo == null) return false
        return true
    }

    override fun useLazyIndex(file: VirtualFile): Boolean {
        if (PlsFileManager.isInjectedFile(file)) return true
        if (ParadoxInlineScriptManager.getInlineScriptExpression(file) != null) return true // inline script files should be lazy indexed
        return false
    }

    override fun indexData(psiFile: PsiFile): Map<String, List<ParadoxComplexEnumValueIndexInfo>> {
        return buildMap { buildData(psiFile, this) }
    }

    private fun buildData(psiFile: PsiFile, fileData: MutableMap<String, List<ParadoxComplexEnumValueIndexInfo>>) {
        val gameType = selectGameType(psiFile) ?: return
        if (psiFile !is ParadoxScriptFile) return

        val definitionStack = ArrayDeque<ParadoxScriptDefinitionElement>()
        psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptDefinitionElement) {
                    definitionStack.addLast(element)
                }

                if (element is ParadoxScriptStringExpressionElement && element.isExpression()) {
                    val info0 = ParadoxComplexEnumValueManager.getInfo(element)
                    if (info0 != null) {
                        val definitionElementOffset = when {
                            // TODO 2.1.0+ 考虑兼容定义注入
                            info0.config.perDefinition -> definitionStack.lastOrNull()?.startOffset ?: -1
                            else -> -1
                        }
                        val info = ParadoxComplexEnumValueIndexInfo(info0.name, info0.enumName, definitionElementOffset, gameType)
                        val list = fileData.getOrPut(info.enumName) { mutableListOf() } as MutableList
                        list.add(info)
                    }
                }

                super.visitElement(element)
            }

            override fun elementFinished(element: PsiElement) {
                if (element is ParadoxScriptDefinitionElement) {
                    definitionStack.removeLastOrNull()
                }
            }
        })

        compressData(fileData)
    }

    private fun compressData(fileData: MutableMap<String, List<ParadoxComplexEnumValueIndexInfo>>) {
        if (fileData.isEmpty()) return
        for ((key, value) in fileData) {
            if (value.size <= 1) continue
            val newValue = value.sortedWith(compareBy({ it.name }, { it.enumName }, { it.definitionElementOffset })).distinct()
            fileData[key] = newValue
        }
    }

    override fun indexLazyData(psiFile: PsiFile): Map<String, List<ParadoxComplexEnumValueIndexInfo>> {
        // 用于兼容懒加载的索引
        return mapOf(LazyIndexKey to emptyList())
    }

    override fun saveValue(storage: DataOutput, value: List<ParadoxComplexEnumValueIndexInfo>) {
        storage.writeIntFast(value.size)
        if (value.isEmpty()) return

        val gameType = value.first().gameType
        storage.writeByte(gameType.optimized(OptimizerRegistry.forGameType()))
        var previousInfo: ParadoxComplexEnumValueIndexInfo? = null
        value.forEach { info ->
            storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
            storage.writeOrWriteFrom(info, previousInfo, { it.enumName }, { storage.writeUTFFast(it) })
            storage.writeIntFast(info.definitionElementOffset)
            previousInfo = info
        }
    }

    override fun readValue(storage: DataInput): List<ParadoxComplexEnumValueIndexInfo> {
        val size = storage.readIntFast()
        if (size == 0) return emptyList()

        val gameType = storage.readByte().deoptimized(OptimizerRegistry.forGameType())
        var previousInfo: ParadoxComplexEnumValueIndexInfo? = null
        return MutableList(size) {
            val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
            val enumName = storage.readOrReadFrom(previousInfo, { it.enumName }, { storage.readUTFFast() })
            val definitionElementOffset = storage.readIntFast()
            ParadoxComplexEnumValueIndexInfo(name, enumName, definitionElementOffset, gameType).also { previousInfo = it }
        }
    }
}
