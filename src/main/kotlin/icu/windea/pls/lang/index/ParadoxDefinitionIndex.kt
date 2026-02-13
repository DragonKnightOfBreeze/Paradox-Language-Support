package icu.windea.pls.lang.index

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.util.startOffset
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.core.collections.asMutable
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.core.readIntFast
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.writeByte
import icu.windea.pls.core.writeIntFast
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.model.ParadoxDefinitionSource
import icu.windea.pls.model.forGameType
import icu.windea.pls.model.index.ParadoxDefinitionIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptPsiUtil
import java.io.DataInput
import java.io.DataOutput

class ParadoxDefinitionIndex : ParadoxIndexInfoAwareFileBasedIndex<List<ParadoxDefinitionIndexInfo>, ParadoxDefinitionIndexInfo>() {
    private val compressComparator = compareBy<ParadoxDefinitionIndexInfo>({ it.type }, { it.name })

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
        val gameType = selectGameType(psiFile) ?: return

        psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxDefinitionElement) {
                    visitDefinitionElement(element)
                }

                if (!ParadoxScriptPsiUtil.isMemberContextElement(element)) return // optimize
                super.visitElement(element)
            }

            private fun visitDefinitionElement(element: ParadoxDefinitionElement) {
                ProgressManager.checkCanceled()

                val definitionInfo = element.definitionInfo ?: return
                val typeConfig = definitionInfo.typeConfig
                if (typeConfig.typePerFile) return

                val typeKey = definitionInfo.typeKey
                val name = definitionInfo.name
                val type = definitionInfo.type
                if (type.isEmpty()) return

                val subtypes = run {
                    if (typeConfig.subtypes.isEmpty()) return@run null
                    val typeKey = definitionInfo.typeKey
                    val result = mutableListOf<CwtSubtypeConfig>()
                    for (subtypeConfig in typeConfig.subtypes.values) {
                        val fastResult = ParadoxConfigMatchService.matchesSubtypeFast(subtypeConfig, result, typeKey)
                        if (fastResult == null) return@run null
                        if (fastResult) result.add(subtypeConfig)
                    }
                    result.map { it.name }
                }

                val info = ParadoxDefinitionIndexInfo(ParadoxDefinitionSource.Property, name, type, subtypes, typeKey, element.startOffset, gameType)
                fileData.getOrPut(PlsIndexUtil.createAllKey()) { mutableListOf() }.asMutable() += info
                fileData.getOrPut(PlsIndexUtil.createTypeKey(type)) { mutableListOf() }.asMutable() += info
                if (name.isNotEmpty()) {
                    fileData.getOrPut(PlsIndexUtil.createNameKey(name)) { mutableListOf() }.asMutable() += info
                    fileData.getOrPut(PlsIndexUtil.createNameTypeKey(name, type)) { mutableListOf() }.asMutable() += info
                }
            }
        })
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
            storage.writeUTFFast(info.source.name)
            storage.writeUTFFast(info.name)
            storage.writeUTFFast(info.type)
            val subtypes = info.subtypes
            if (subtypes == null) {
                storage.writeIntFast(-1)
            } else {
                storage.writeIntFast(subtypes.size)
                subtypes.forEach { storage.writeUTFFast(it) }
            }
            storage.writeIntFast(info.elementOffset)
        }
    }

    override fun readValue(storage: DataInput): List<ParadoxDefinitionIndexInfo> {
        val size = storage.readIntFast()
        if (size == 0) return emptyList()

        val gameType = storage.readByte().deoptimized(OptimizerRegistry.forGameType())
        return MutableList(size) {
            val source = ParadoxDefinitionSource.valueOf(storage.readUTFFast())
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
