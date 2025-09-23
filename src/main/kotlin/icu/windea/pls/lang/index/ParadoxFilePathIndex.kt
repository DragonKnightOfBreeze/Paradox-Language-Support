package icu.windea.pls.lang.index

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.PlsConstants
import icu.windea.pls.model.deoptimizeValue
import icu.windea.pls.model.optimizeValue
import java.io.DataInput
import java.io.DataOutput
import java.util.*

/**
 * 用于索引文件的路径信息。
 */
class ParadoxFilePathIndex : FileBasedIndexExtension<String, ParadoxFilePathIndex.Info>() {
    data class Info(
        val directory: String,
        val gameType: ParadoxGameType,
        val included: Boolean
    )

    override fun getName() = ParadoxIndexKeys.FilePath

    override fun getVersion() = 75 // VERSION for 2.0.5

    override fun getIndexer(): DataIndexer<String, Info, FileContent> {
        return DataIndexer { inputData ->
            //这里索引的路径，使用相对于入口目录的路径
            val fileInfo = inputData.file.fileInfo ?: return@DataIndexer emptyMap()
            val path = fileInfo.path.path
            val directoryPath = fileInfo.path.parent
            val gameType = fileInfo.rootInfo.gameType
            val included = isIncluded(inputData.file)
            val info = Info(directoryPath, gameType, included)
            Collections.singletonMap(path, info)
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): DataExternalizer<Info> {
        return object : DataExternalizer<Info> {
            override fun save(storage: DataOutput, value: Info) {
                storage.writeUTFFast(value.directory)
                storage.writeByte(value.gameType.optimizeValue())
                storage.writeBoolean(value.included)
            }

            override fun read(storage: DataInput): Info {
                val path = storage.readUTFFast()
                val gameType = storage.readByte().deoptimizeValue<ParadoxGameType>()
                val included = storage.readBoolean()
                return Info(path, gameType, included)
            }
        }
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { it.fileInfo != null }
    }

    override fun indexDirectories(): Boolean {
        return true
    }

    override fun dependsOnFileContent(): Boolean {
        return false
    }

    private fun isIncluded(file: VirtualFile): Boolean {
        if (file.fileInfo == null) return false
        val parent = file.parent
        if (parent != null && parent.fileInfo != null && !isIncluded(parent)) return false
        val fileName = file.name
        if (fileName.startsWith('.')) return false //排除隐藏目录或文件
        if (file.isDirectory) {
            if (fileName in ParadoxIndexManager.excludeDirectoriesForFilePathIndex) return false //排除一些特定的目录
            return true
        }
        val fileExtension = fileName.substringAfterLast('.')
        if (fileExtension.isEmpty()) return false
        return fileExtension in PlsConstants.scriptFileExtensions
            || fileExtension in PlsConstants.localisationFileExtensions
            || fileExtension in PlsConstants.csvFileExtensions
            || fileExtension in PlsConstants.imageFileExtensions
    }
}

