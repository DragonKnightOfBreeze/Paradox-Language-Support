package icu.windea.pls.lang.index

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.core.IndexInputFilter
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
import icu.windea.pls.model.forGameType
import icu.windea.pls.model.index.ParadoxFileDefinitionData
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptFile
import java.io.DataInput
import java.io.DataOutput

class ParadoxFileDefinitionIndex : FileBasedIndexExtension<String, ParadoxFileDefinitionData>() {
    private val inputFilter = IndexInputFilter { filterFile(it) }
    private val indexer = DataIndexer<String, ParadoxFileDefinitionData, FileContent> { indexData(it) }
    private val keyDescriptor = EnumeratorStringDescriptor.INSTANCE
    private val valueExternalizer = object : DataExternalizer<ParadoxFileDefinitionData> {
        override fun save(storage: DataOutput, value: ParadoxFileDefinitionData) = saveValue(storage, value)
        override fun read(storage: DataInput) = readValue(storage)
    }

    override fun getName() = PlsIndexKeys.FileDefinition

    override fun getVersion() = PlsIndexVersions.FileDefinition

    override fun getInputFilter() = inputFilter

    override fun dependsOnFileContent() = true

    override fun getIndexer() = indexer

    override fun getKeyDescriptor() = keyDescriptor

    override fun getValueExternalizer() = valueExternalizer

    private fun filterFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if (fileType != ParadoxScriptFileType) return false
        if (file.fileInfo == null) return false
        return true
    }

    private fun indexData(fileContent: FileContent): Map<String, ParadoxFileDefinitionData> {
        val psiFile = fileContent.psiFile
        if (psiFile !is ParadoxScriptFile) return emptyMap()
        ProgressManager.checkCanceled()

        val definitionInfo = psiFile.definitionInfo ?: return emptyMap()
        val typeConfig = definitionInfo.typeConfig
        if (!typeConfig.typePerFile) return emptyMap()

        val typeKey = definitionInfo.typeKey
        val name = definitionInfo.name
        val type = definitionInfo.type
        val subtypes = run {
            if (typeConfig.subtypes.isEmpty()) return@run null
            val result = mutableListOf<CwtSubtypeConfig>()
            for (subtypeConfig in typeConfig.subtypes.values) {
                val fastResult = ParadoxConfigMatchService.matchesSubtypeFast(subtypeConfig, result, typeKey) ?: return@run null
                if (fastResult) result.add(subtypeConfig)
            }
            result.map { it.name }
        }

        val data = ParadoxFileDefinitionData(name, type, subtypes, typeKey, definitionInfo.gameType)

        val fileData = mutableMapOf<String, ParadoxFileDefinitionData>()
        fileData[PlsIndexUtil.createAllKey()] = data
        fileData[PlsIndexUtil.createTypeKey(type)] = data
        if (name.isNotEmpty()) {
            fileData[PlsIndexUtil.createNameKey(name)] = data
            fileData[PlsIndexUtil.createNameTypeKey(name, type)] = data
        }
        return fileData
    }

    private fun saveValue(storage: DataOutput, value: ParadoxFileDefinitionData) {
        storage.writeByte(value.gameType.optimized(OptimizerRegistry.forGameType()))
        storage.writeUTFFast(value.name)
        storage.writeUTFFast(value.type)
        val subtypes = value.subtypes
        if (subtypes == null) {
            storage.writeIntFast(-1)
        } else {
            storage.writeIntFast(subtypes.size)
            subtypes.forEach { storage.writeUTFFast(it) }
        }
        storage.writeUTFFast(value.typeKey)
    }

    private fun readValue(storage: DataInput): ParadoxFileDefinitionData {
        val gameType = storage.readByte().deoptimized(OptimizerRegistry.forGameType())
        val name = storage.readUTFFast()
        val type = storage.readUTFFast()
        val size = storage.readIntFast()
        val subtypes = if (size < 0) null else List(size) { storage.readUTFFast() }
        val typeKey = storage.readUTFFast()
        return ParadoxFileDefinitionData(name, type, subtypes, typeKey, gameType)
    }
}
