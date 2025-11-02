package icu.windea.pls.lang.index

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.writeByte
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.model.ValueOptimizers.ForParadoxGameType
import icu.windea.pls.model.deoptimized
import icu.windea.pls.model.index.ParadoxFilePathIndexInfo
import icu.windea.pls.model.optimized
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

    override fun indexData(psiFile: PsiFile): Map<String, ParadoxFilePathIndexInfo> {
        // 这里索引的路径，使用相对于入口目录的路径
        val file = psiFile.virtualFile
        val fileInfo = file.fileInfo ?: return emptyMap()
        val path = fileInfo.path.path
        val directoryPath = fileInfo.path.parent
        val gameType = fileInfo.rootInfo.gameType
        val included = PlsIndexManager.includeForFilePathIndex(file)
        val info = ParadoxFilePathIndexInfo(directoryPath, gameType, included)
        return Collections.singletonMap(path, info)
    }

    override fun saveValue(storage: DataOutput, value: ParadoxFilePathIndexInfo) {
        storage.writeUTFFast(value.directory)
        storage.writeByte(value.gameType.optimized(ForParadoxGameType))
        storage.writeBoolean(value.included)
    }

    override fun readValue(storage: DataInput): ParadoxFilePathIndexInfo {
        val path = storage.readUTFFast()
        val gameType = storage.readByte().deoptimized(ForParadoxGameType)
        val included = storage.readBoolean()
        return ParadoxFilePathIndexInfo(path, gameType, included)
    }
}

