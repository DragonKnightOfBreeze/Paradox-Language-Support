package icu.windea.pls.lang.index

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import icu.windea.pls.core.IndexInputFilter
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.writeByte
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.model.ParadoxFilePathInfo
import icu.windea.pls.model.forGameType
import java.io.DataInput
import java.io.DataOutput
import java.util.*

/**
 * 文件的路径信息的索引。
 *
 * @see ParadoxFilePathInfo
 */
class ParadoxFilePathIndex : FileBasedIndexExtension<String, ParadoxFilePathInfo>() {
    private val inputFilter = IndexInputFilter { it.fileInfo != null }
    private val indexer = DataIndexer<String, ParadoxFilePathInfo, FileContent> { indexData(it) }
    private val keyDescriptor = EnumeratorStringDescriptor.INSTANCE
    private val valueExternalizer = object : DataExternalizer<ParadoxFilePathInfo> {
        override fun save(storage: DataOutput, value: ParadoxFilePathInfo) = saveValue(storage, value)
        override fun read(storage: DataInput) = readValue(storage)
    }

    override fun getName() = PlsIndexKeys.FilePath

    override fun getVersion() = PlsIndexVersions.FilePath

    override fun getInputFilter() = inputFilter

    override fun dependsOnFileContent() = false

    override fun getIndexer() = indexer

    override fun getKeyDescriptor() = keyDescriptor

    override fun getValueExternalizer() = valueExternalizer

    override fun indexDirectories() = true

    private fun indexData(fileContent: FileContent): Map<String, ParadoxFilePathInfo> {
        // 这里索引的路径，使用相对于入口目录的路径
        val file = fileContent.file
        val fileInfo = file.fileInfo ?: return emptyMap()
        val path = fileInfo.path.path
        val directoryPath = fileInfo.path.parent
        val gameType = fileInfo.rootInfo.gameType
        val included = PlsIndexUtil.includeForFilePathIndex(file)
        val info = ParadoxFilePathInfo(directoryPath, included, gameType)
        return Collections.singletonMap(path, info)
    }

    private fun saveValue(storage: DataOutput, value: ParadoxFilePathInfo) {
        storage.writeUTFFast(value.directory)
        storage.writeByte(value.gameType.optimized(OptimizerRegistry.forGameType()))
        storage.writeBoolean(value.included)
    }

    private fun readValue(storage: DataInput): ParadoxFilePathInfo {
        val directory = storage.readUTFFast()
        val gameType = storage.readByte().deoptimized(OptimizerRegistry.forGameType())
        val included = storage.readBoolean()
        return ParadoxFilePathInfo(directory, included, gameType)
    }
}
