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
import icu.windea.pls.lang.match.CwtTypeConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.model.forGameType
import icu.windea.pls.model.index.ParadoxDefinitionInjectionIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPsiUtil
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import java.io.DataInput
import java.io.DataOutput

/**
 * 定义注入的索引。
 */
class ParadoxDefinitionInjectionIndex : ParadoxIndexInfoAwareFileBasedIndex<List<ParadoxDefinitionInjectionIndexInfo>, ParadoxDefinitionInjectionIndexInfo>() {
    private val compressComparator = compareBy<ParadoxDefinitionInjectionIndexInfo>({ it.type }, { it.mode })

    override fun getName() = PlsIndexKeys.DefinitionInjection

    override fun getVersion() = PlsIndexVersions.DefinitionInjection

    override fun filterFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if (fileType != ParadoxScriptFileType) return false
        val fileInfo = file.fileInfo ?: return false
        return ParadoxPathConstraint.AcceptDefinitionInjection.test(fileInfo.path)
    }

    override fun useLazyIndex(file: VirtualFile): Boolean {
        if (PlsFileManager.isInjectedFile(file)) return true
        return false
    }

    override fun indexData(psiFile: PsiFile): Map<String, List<ParadoxDefinitionInjectionIndexInfo>> {
        return buildMap {
            buildData(psiFile, this)
            compressData(this)
        }
    }

    private fun buildData(psiFile: PsiFile, fileData: MutableMap<String, List<ParadoxDefinitionInjectionIndexInfo>>) {
        if (psiFile !is ParadoxScriptFile) return
        val gameType = selectGameType(psiFile) ?: return
        if (!ParadoxDefinitionInjectionManager.isSupported(gameType)) return

        val fileInfo = psiFile.fileInfo ?: return
        val configGroup = PlsFacade.getConfigGroup(psiFile.project, gameType)
        val config = configGroup.directivesModel.definitionInjection ?: return

        val path = fileInfo.path
        val matchContext = CwtTypeConfigMatchContext(configGroup, path)
        val typeConfig = ParadoxConfigMatchService.getMatchedTypeConfigForInjection(matchContext) ?: return
        val type = typeConfig.name
        if (type.isEmpty()) return

        psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptProperty) {
                    visitProperty(element)
                }

                if (!ParadoxScriptPsiUtil.isMemberContextElement(element)) return // optimize
                super.visitElement(element)
            }

            private fun visitProperty(element: ParadoxScriptProperty) {
                ProgressManager.checkCanceled()

                if (element.parent !is ParadoxScriptRootBlock) return
                val propertyValue = element.propertyValue
                if (propertyValue !is ParadoxScriptBlock) return

                val expression = element.name
                if (expression.isEmpty() || expression.isParameterized()) return
                val mode = ParadoxDefinitionInjectionManager.getModeFromExpression(expression) ?: return
                if (mode.isEmpty()) return
                if (config.modeConfigs[mode] == null) return
                val target = ParadoxDefinitionInjectionManager.getTargetFromExpression(expression) ?: return
                if (target.isEmpty()) return

                val info = ParadoxDefinitionInjectionIndexInfo(mode, target, type, element.startOffset, gameType)
                fileData.getOrPut(PlsIndexUtil.createAllKey()) { mutableListOf() }.asMutable() += info
                fileData.getOrPut(PlsIndexUtil.createNameKey(info.target)) { mutableListOf() }.asMutable() += info
                fileData.getOrPut(PlsIndexUtil.createTypeKey(info.type)) { mutableListOf() }.asMutable() += info
                fileData.getOrPut(PlsIndexUtil.createNameTypeKey(info.target, info.type)) { mutableListOf() }.asMutable() += info
            }
        })
    }

    private fun compressData(fileData: MutableMap<String, List<ParadoxDefinitionInjectionIndexInfo>>) {
        if (fileData.isEmpty()) return
        for ((key, value) in fileData) {
            if (value.size <= 1) continue
            val newValue = value.sortedWith(compressComparator)
            fileData[key] = newValue
        }
    }

    override fun indexLazyData(psiFile: PsiFile): Map<String, List<ParadoxDefinitionInjectionIndexInfo>> {
        // 用于兼容懒加载的索引，真实数据通过 gist 计算
        return mapOf(PlsIndexUtil.createLazyKey() to emptyList())
    }

    override fun saveValue(storage: DataOutput, value: List<ParadoxDefinitionInjectionIndexInfo>) {
        storage.writeIntFast(value.size)
        if (value.isEmpty()) return

        val gameType = value.first().gameType
        storage.writeByte(gameType.optimized(OptimizerRegistry.forGameType()))
        var previousInfo: ParadoxDefinitionInjectionIndexInfo? = null
        value.forEach { info ->
            storage.writeOrWriteFrom(info, previousInfo, { it.mode }, { storage.writeUTFFast(it) })
            storage.writeUTFFast(info.target)
            storage.writeOrWriteFrom(info, previousInfo, { it.type }, { storage.writeUTFFast(it) })
            storage.writeIntFast(info.elementOffset)
            previousInfo = info
        }
    }

    override fun readValue(storage: DataInput): List<ParadoxDefinitionInjectionIndexInfo> {
        val size = storage.readIntFast()
        if (size == 0) return emptyList()

        val gameType = storage.readByte().deoptimized(OptimizerRegistry.forGameType())
        var previousInfo: ParadoxDefinitionInjectionIndexInfo? = null
        return MutableList(size) {
            val mode = storage.readOrReadFrom(previousInfo, { it.mode }, { storage.readUTFFast() })
            val target = storage.readUTFFast()
            val type = storage.readOrReadFrom(previousInfo, { it.type }, { storage.readUTFFast() })
            val elementOffset = storage.readIntFast()
            ParadoxDefinitionInjectionIndexInfo(mode, target, type, elementOffset, gameType).also { previousInfo = it }
        }
    }
}
