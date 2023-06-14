package icu.windea.pls.core.index

import com.intellij.openapi.vfs.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.lang.model.*
import java.io.*
import java.util.*

class ParadoxFilePathIndex : FileBasedIndexExtension<String, ParadoxFilePathInfo>() {
    companion object {
        @JvmField val NAME = ID.create<String, ParadoxFilePathInfo>("paradox.file.path.index")
        private const val VERSION = 27 //1.0.5
    }
    
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun getIndexer(): DataIndexer<String, ParadoxFilePathInfo, FileContent> {
        return DataIndexer { inputData ->
            val fileInfo = inputData.file.fileInfo ?: return@DataIndexer emptyMap()
            val path = fileInfo.path.path
            val directoryPath = fileInfo.path.parent
            val gameType = fileInfo.rootInfo.gameType
            val included = isIncluded(inputData.file)
            val info = ParadoxFilePathInfo(directoryPath, gameType, included)
            Collections.singletonMap(path, info)
        }
    }
    
    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }
    
    
    override fun getValueExternalizer(): DataExternalizer<ParadoxFilePathInfo> {
        return object: DataExternalizer<ParadoxFilePathInfo> {
            override fun save(storage: DataOutput, value: ParadoxFilePathInfo) {
                storage.writeUTF(value.directory)
                storage.writeByte(value.gameType.toByte())
                storage.writeBoolean(value.included)
            }
            
            override fun read(storage: DataInput): ParadoxFilePathInfo {
                val path = storage.readUTF()
                val gameType = storage.readByte().toGameType()
                val included = storage.readBoolean()
                return ParadoxFilePathInfo(path, gameType, included)
            }
        }
    }
    
    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { it.fileInfo != null }
    }
    
    override fun dependsOnFileContent(): Boolean {
        return false
    }
    
    private fun isIncluded(file: VirtualFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        val path = fileInfo.path.path
        val extension = path.substringAfterLast('.')
        if(extension.isEmpty()) return false
        return extension in PlsConstants.scriptFileExtensions
            || extension in PlsConstants.localisationFileExtensions
            || extension in PlsConstants.ddsFileExtensions
            || extension == "png"
            || extension == "tga"
    }
}

