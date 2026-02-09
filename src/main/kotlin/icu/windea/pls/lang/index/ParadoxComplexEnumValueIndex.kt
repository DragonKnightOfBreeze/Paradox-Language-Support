package icu.windea.pls.lang.index

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.collections.asMutable
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
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.CwtComplexEnumConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.match.ParadoxConfigMatchService.matchesComplexEnum
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.model.forGameType
import icu.windea.pls.model.index.ParadoxComplexEnumValueIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptPsiUtil
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isExpression
import java.io.DataInput
import java.io.DataOutput

/**
 * 复杂枚举值的索引。使用枚举名作为索引键。
 */
class ParadoxComplexEnumValueIndex : IndexInfoAwareFileBasedIndex<List<ParadoxComplexEnumValueIndexInfo>>() {
    companion object {
        const val LazyIndexKey = "__lazy__"
    }

    private val compressComparator = compareBy<ParadoxComplexEnumValueIndexInfo>({ it.enumName }, { it.name })

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
        return buildMap {
            buildData(psiFile, this)
            compressData(this)
        }
    }

    private fun buildData(psiFile: PsiFile, fileData: MutableMap<String, List<ParadoxComplexEnumValueIndexInfo>>) {
        if (psiFile !is ParadoxScriptFile) return
        val gameType = selectGameType(psiFile) ?: return

        // 要求存在候选项
        val configGroup = PlsFacade.getConfigGroup(psiFile.project, gameType)
        val path = psiFile.fileInfo?.path ?: return
        val matchContext = CwtComplexEnumConfigMatchContext(configGroup, path)
        val candidates = ParadoxConfigMatchService.getComplexEnumConfigCandidates(matchContext)
        if (candidates.isEmpty()) return
        matchContext.matchPath = false

        psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptStringExpressionElement ) visitStringExpressionElement(element)
                if (!ParadoxScriptPsiUtil.isMemberContextElement(element)) return // optimize
                super.visitElement(element)
            }

            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                if (!element.isExpression()) return

                // 2.1.3 直接匹配，不经过缓存数据，以优化性能
                val name = element.value
                if (name.isParameterized()) return // 排除可能带参数的情况
                if (ParadoxInlineScriptManager.isMatched(name, gameType)) return // 排除是内联脚本用法的情况
                val config = candidates.find { matchesComplexEnum(matchContext, element, it) } ?: return
                val enumName = config.name

                // 2.1.3 兼容定义注入
                val definitionElementOffset = if (config.perDefinition) selectScope { element.parentDefinitionOrInjection() }?.startOffset ?: -1 else -1
                val info = ParadoxComplexEnumValueIndexInfo(name, enumName, definitionElementOffset, gameType)
                fileData.getOrPut(info.enumName) { mutableListOf() }.asMutable() += info
            }
        })
    }

    private fun compressData(fileData: MutableMap<String, List<ParadoxComplexEnumValueIndexInfo>>) {
        if (fileData.isEmpty()) return
        for ((key, value) in fileData) {
            if (value.size <= 1) continue
            val newValue = value.sortedWith(compressComparator)
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
