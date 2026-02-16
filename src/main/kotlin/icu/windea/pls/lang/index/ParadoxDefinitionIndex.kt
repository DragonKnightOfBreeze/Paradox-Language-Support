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
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.model.ParadoxDefinitionSource
import icu.windea.pls.model.constraints.ParadoxDefinitionIndexConstraint
import icu.windea.pls.model.forDefinitionSource
import icu.windea.pls.model.forGameType
import icu.windea.pls.model.index.ParadoxDefinitionIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPsiUtil
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
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

        val typeConfigForInjection = when {
            ParadoxDefinitionInjectionManager.isSupported(gameType) -> fileLevelTypeConfigs.find { ParadoxConfigMatchService.matchesTypeForInjection(fileLevelMatchContext, it) }
            else -> null
        }

        // 2.1.3 这里需要使用 accept 而非 acceptChildren，因为 psiFile 也可能是一个定义
        psiFile.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxDefinitionElement) visitDefinitionElement(element)
                if (!ParadoxScriptPsiUtil.isMemberContextElement(element)) return // optimize
                super.visitElement(element)
            }

            private fun visitDefinitionElement(element: ParadoxDefinitionElement) {
                ProgressManager.checkCanceled()
                // 2.1.3 检查是否是 create_mode 的定义注入
                processDefinitionFromInjection(element).let { if (!it) return }
                // 2.1.3 直接匹配，不经过缓存数据，以优化性能
                processDefinition(element)
            }

            private fun processDefinition(element: ParadoxDefinitionElement): Boolean {
                val source = ParadoxDefinitionService.resolveSource(element) ?: return true
                val typeKey = ParadoxDefinitionManager.getTypeKey(element) ?: return true

                val rootKeys = ParadoxMemberService.getRootKeys(element, maxDepth = maxDepth) ?: return false
                if (rootKeys.any { it.isParameterized() }) return false // 排除顶级键可能带参数的情况
                val typeKeyPrefix = lazy { ParadoxMemberService.getKeyPrefix(element) }
                val matchContext = fileLevelMatchContext.copy(typeKey = typeKey, rootKeys = rootKeys, typeKeyPrefix = typeKeyPrefix)
                val typeConfig = fileLevelTypeConfigs.find { ParadoxConfigMatchService.matchesType(matchContext, element, it) } ?: return false
                val type = typeConfig.name.orNull() ?: return false
                val name = ParadoxDefinitionService.resolveName(element, typeKey, typeConfig)
                val subtypes = ParadoxConfigMatchService.getFastMatchedSubtypeConfigs(typeConfig, typeKey)?.map { it.name }?.optimized()

                val info = ParadoxDefinitionIndexInfo(source, name, type, subtypes, typeKey, element.startOffset, gameType)
                addToFileData(info, fileData)
                return false
            }

            private fun processDefinitionFromInjection(element: ParadoxDefinitionElement): Boolean {
                if (element !is ParadoxScriptProperty) return true

                // 可用性检查
                if (element.parent !is ParadoxScriptRootBlock) return true
                val propertyValue = element.propertyValue
                if (propertyValue !is ParadoxScriptBlock) return true

                // 匹配性检查
                val source = ParadoxDefinitionSource.Injection
                val expression = element.name
                if (expression.isEmpty() || expression.isParameterized()) return true
                val mode = ParadoxDefinitionInjectionManager.getModeFromExpression(expression) ?: return true
                if (mode.isEmpty()) return false
                if (!ParadoxDefinitionInjectionManager.isCreateMode(mode, configGroup)) return false
                val target = ParadoxDefinitionInjectionManager.getTargetFromExpression(expression) ?: return false
                if (target.isEmpty()) return false

                // 匹配类型
                val typeConfig = typeConfigForInjection ?: return false
                val type = typeConfig.name.orNull() ?: return false
                val name = ParadoxDefinitionService.resolveName(element, target, typeConfig)
                val subtypes = ParadoxConfigMatchService.getFastMatchedSubtypeConfigs(typeConfig, target)?.map { it.name }?.optimized()

                val info = ParadoxDefinitionIndexInfo(source, name, type, subtypes, target, element.startOffset, gameType)
                addToFileData(info, fileData)
                return false
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
            storage.writeByte(info.source.optimized(OptimizerRegistry.forDefinitionSource()))
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
            storage.writeIntFast(info.elementOffset)
        }
    }

    override fun readValue(storage: DataInput): List<ParadoxDefinitionIndexInfo> {
        val size = storage.readIntFast()
        if (size == 0) return emptyList()

        val gameType = storage.readByte().deoptimized(OptimizerRegistry.forGameType())
        return MutableList(size) {
            val source = storage.readByte().deoptimized(OptimizerRegistry.forDefinitionSource())
            val name = storage.readUTFFast()
            val type = storage.readUTFFast()
            val subtypesSize = storage.readIntFast()
            val subtypes = if (subtypesSize < 0) null else List(subtypesSize) { storage.readUTFFast() }
            val typeKey = storage.readUTFFast()
            val elementOffset = storage.readIntFast()
            ParadoxDefinitionIndexInfo(source, name, type, subtypes, typeKey, elementOffset, gameType)
        }
    }
}
