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
import icu.windea.pls.core.letIf
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.core.orNull
import icu.windea.pls.core.readIntFast
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.writeByte
import icu.windea.pls.core.writeIntFast
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.CwtTypeConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.resolve.ParadoxDefinitionService
import icu.windea.pls.lang.resolve.ParadoxMemberService
import icu.windea.pls.lang.settings.PlsInternalSettings
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.model.ParadoxDefinitionSource
import icu.windea.pls.model.constraints.ParadoxDefinitionIndexConstraint
import icu.windea.pls.model.forDefinitionSource
import icu.windea.pls.model.forGameType
import icu.windea.pls.model.index.ParadoxDefinitionIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPsiUtil
import java.io.DataInput
import java.io.DataOutput

class ParadoxDefinitionIndex : ParadoxIndexInfoAwareFileBasedIndex<List<ParadoxDefinitionIndexInfo>, ParadoxDefinitionIndexInfo>() {
    private val compressComparator = compareBy<ParadoxDefinitionIndexInfo>({ it.type }, { it.name })
    private val maxDepth = PlsInternalSettings.getInstance().maxDefinitionDepth

    override fun getName() = PlsIndexKeys.Definition

    override fun getVersion() = PlsIndexVersions.Definition

    override fun filterFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if (fileType != ParadoxScriptFileType) return false
        if (file.fileInfo == null) return false
        return true
    }

    override fun useLazyIndex(file: VirtualFile): Boolean {
        if (PlsFileManager.isInjectedFile(file)) return true
        return false
    }

    override fun indexData(psiFile: PsiFile): Map<String, List<ParadoxDefinitionIndexInfo>> {
        return buildMap {
            buildData(psiFile, this)
            compressData(this)
        }
    }

    private fun buildData(psiFile: PsiFile, fileData: MutableMap<String, List<ParadoxDefinitionIndexInfo>>) {
        if (psiFile !is ParadoxScriptFile) return
        val fileInfo = psiFile.fileInfo ?: return
        val gameType = fileInfo.rootInfo.gameType
        ProgressManager.checkCanceled()

        // 2.1.3 要求存在候选项
        val configGroup = PlsFacade.getConfigGroup(psiFile.project, gameType)
        val path = fileInfo.path
        val fileLevelMatchContext = CwtTypeConfigMatchContext(configGroup, path)
        val fileLevelTypeConfigs = ParadoxConfigMatchService.getTypeConfigCandidates(fileLevelMatchContext)
        if (fileLevelTypeConfigs.isEmpty()) return
        fileLevelMatchContext.matchPath = false

        // 2.1.3 这里需要使用 accept 而非 acceptChildren，因为 psiFile 也可能是一个定义
        psiFile.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxDefinitionElement) visitDefinitionElement(element)
                if (!ParadoxScriptPsiUtil.isMemberContextElement(element)) return // optimize
                super.visitElement(element)
            }

            private fun visitDefinitionElement(element: ParadoxDefinitionElement) {
                ProgressManager.checkCanceled()

                // 2.1.3 直接匹配，不经过缓存数据，以优化性能
                val typeKey = ParadoxDefinitionManager.getTypeKey(element) ?: return
                val rootKeys = ParadoxMemberService.getRootKeys(element, maxDepth = maxDepth) ?: return
                if (rootKeys.any { it.isParameterized() }) return // 排除顶级键可能带参数的情况
                val typeKeyPrefix = lazy { ParadoxMemberService.getKeyPrefix(element) }
                val matchContext = fileLevelMatchContext.copy(typeKey = typeKey, rootKeys = rootKeys, typeKeyPrefix = typeKeyPrefix)
                val typeConfig = fileLevelTypeConfigs.find { ParadoxConfigMatchService.matchesType(matchContext, element, it) } ?: return
                val type = typeConfig.name.orNull() ?: return
                val name = ParadoxDefinitionService.resolveName(element, typeKey, typeConfig)
                val subtypes = ParadoxConfigMatchService.getFastMatchedSubtypeConfigs(typeConfig, typeKey)?.map { it.name }?.optimized()
                val source = when (element) {
                    is ParadoxScriptFile -> ParadoxDefinitionSource.File
                    is ParadoxScriptProperty -> ParadoxDefinitionSource.Property
                    else -> return // unexpected
                }

                val info = ParadoxDefinitionIndexInfo(name, type, subtypes, typeKey, source, element.startOffset, gameType)
                addToFileData(info, fileData)
            }
        })
    }

    private fun addToFileData(info: ParadoxDefinitionIndexInfo, fileData: MutableMap<String, List<ParadoxDefinitionIndexInfo>>) {
        val ignoreCase = ParadoxDefinitionIndexConstraint.entries.any { it.ignoreCase && it.test(info.type) }
        val name = info.name.letIf(ignoreCase) { it.lowercase() }
        val type = info.type
        fileData.getOrPut(PlsIndexUtil.createAllKey()) { mutableListOf() }.asMutable() += info
        fileData.getOrPut(PlsIndexUtil.createTypeKey(type)) { mutableListOf() }.asMutable() += info
        if (name.isEmpty()) return
        fileData.getOrPut(PlsIndexUtil.createNameKey(name)) { mutableListOf() }.asMutable() += info
        fileData.getOrPut(PlsIndexUtil.createNameTypeKey(name, type)) { mutableListOf() }.asMutable() += info
    }

    private fun compressData(fileData: MutableMap<String, List<ParadoxDefinitionIndexInfo>>) {
        if (fileData.isEmpty()) return
        for ((key, value) in fileData) {
            if (value.size <= 1) continue
            val newValue = value.sortedWith(compressComparator)
            fileData[key] = newValue
        }
    }

    override fun indexLazyData(psiFile: PsiFile): Map<String, List<ParadoxDefinitionIndexInfo>> {
        return mapOf(PlsIndexUtil.createLazyKey() to emptyList())
    }

    override fun saveValue(storage: DataOutput, value: List<ParadoxDefinitionIndexInfo>) {
        storage.writeIntFast(value.size)
        if (value.isEmpty()) return

        val gameType = value.first().gameType
        storage.writeByte(gameType.optimized(OptimizerRegistry.forGameType()))
        value.forEach { info ->
            storage.writeUTFFast(info.name)
            storage.writeUTFFast(info.type)
            val subtypes = info.subtypes
            if (subtypes == null) {
                storage.writeIntFast(-1)
            } else {
                storage.writeIntFast(subtypes.size)
                subtypes.forEach { storage.writeUTFFast(it) }
            }
            storage.writeUTFFast(info.typeKey)
            storage.writeByte(info.source.optimized(OptimizerRegistry.forDefinitionSource()))
            storage.writeIntFast(info.elementOffset)
        }
    }

    override fun readValue(storage: DataInput): List<ParadoxDefinitionIndexInfo> {
        val size = storage.readIntFast()
        if (size == 0) return emptyList()

        val gameType = storage.readByte().deoptimized(OptimizerRegistry.forGameType())
        return MutableList(size) {
            val name = storage.readUTFFast()
            val type = storage.readUTFFast()
            val subtypesSize = storage.readIntFast()
            val subtypes = if (subtypesSize < 0) null else List(subtypesSize) { storage.readUTFFast() }
            val typeKey = storage.readUTFFast()
            val source = storage.readByte().deoptimized(OptimizerRegistry.forDefinitionSource())
            val elementOffset = storage.readIntFast()
            ParadoxDefinitionIndexInfo(name, type, subtypes, typeKey, source, elementOffset, gameType)
        }
    }
}
