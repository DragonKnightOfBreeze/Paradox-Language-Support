package icu.windea.pls.lang.index

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.FileContent
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.writeByte
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.model.forGameType
import icu.windea.pls.model.index.ParadoxFilePathIndexInfo
import java.io.DataInput
import java.io.DataOutput
import java.util.*

/**
 * 文件的路径信息的索引。
 *
 * @see ParadoxFilePathIndexInfo
 */
class ParadoxFilePathIndex : IndexInfoAwareFileBasedIndex<ParadoxFilePathIndexInfo>() {
    override fun getName() = PlsIndexKeys.FilePath

    override fun getVersion() = PlsIndexVersions.FilePath

    override fun dependsOnFileContent() = false

    override fun indexDirectories() = true

    override fun filterFile(file: VirtualFile): Boolean {
        return file.fileInfo != null
    }

    override fun indexData(fileContent: FileContent): Map<String, ParadoxFilePathIndexInfo> {
        // 这里索引的路径，使用相对于入口目录的路径
        val file = fileContent.file
        val fileInfo = file.fileInfo ?: return emptyMap()
        val path = fileInfo.path.path
        val directoryPath = fileInfo.path.parent
        val gameType = fileInfo.rootInfo.gameType
        val included = PlsIndexUtil.includeForFilePathIndex(file)
        val info = ParadoxFilePathIndexInfo(directoryPath, included, gameType)
        return Collections.singletonMap(path, info)
    }

    override fun saveValue(storage: DataOutput, value: ParadoxFilePathIndexInfo) {
        storage.writeUTFFast(value.directory)
        storage.writeByte(value.gameType.optimized(OptimizerRegistry.forGameType()))
        storage.writeBoolean(value.included)
    }

    override fun readValue(storage: DataInput): ParadoxFilePathIndexInfo {
        val path = storage.readUTFFast()
        val gameType = storage.readByte().deoptimized(OptimizerRegistry.forGameType())
        val included = storage.readBoolean()
        return ParadoxFilePathIndexInfo(path, included, gameType)
    }
}

