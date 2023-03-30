package icu.windea.pls.core.index

import com.intellij.openapi.util.registry.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.lang.model.*
import java.io.*
import java.util.*

class ParadoxFilePathIndex : ScalarIndexExtension<ParadoxFilePathInfo>() {
    companion object {
        @JvmField val NAME = ID.create<ParadoxFilePathInfo, Void>("paradox.file.path.index")
    }
    
    override fun getName(): ID<ParadoxFilePathInfo, Void> {
        return NAME
    }
    
    override fun getIndexer(): DataIndexer<ParadoxFilePathInfo, Void, FileContent> {
        return DataIndexer { inputData ->
            val fileInfo = inputData.file.fileInfo ?: return@DataIndexer emptyMap()
            val path = fileInfo.path.path
            val gameType = fileInfo.rootInfo.gameType
            val info = ParadoxFilePathInfo(path, gameType)
            Collections.singletonMap<ParadoxFilePathInfo, Void>(info, null)
        }
    }
    
    override fun getKeyDescriptor(): KeyDescriptor<ParadoxFilePathInfo> {
        return ParadoxFilePathKeyDescriptor
    }
    
    override fun getVersion(): Int {
        return 4 + (if(Registry.`is`("indexing.paradox.file.path.over.vfs")) 0xff else 0)
    }
    
    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { it.fileInfo != null }
    }
    
    override fun dependsOnFileContent(): Boolean {
        return false
    }
    
    object ParadoxFilePathKeyDescriptor : KeyDescriptor<ParadoxFilePathInfo> {
        override fun getHashCode(value: ParadoxFilePathInfo): Int {
            return value.hashCode()
        }
        
        override fun isEqual(val1: ParadoxFilePathInfo, val2: ParadoxFilePathInfo): Boolean {
            return val1 == val2
        }
        
        override fun save(storage: DataOutput, value: ParadoxFilePathInfo) {
            IOUtil.writeUTF(storage, value.path)
            IOUtil.writeUTF(storage, value.gameType.id)
        }
        
        override fun read(storage: DataInput): ParadoxFilePathInfo {
            val path = IOUtil.readUTF(storage)
            val gameType = IOUtil.readUTF(storage).let { ParadoxGameType.resolve(it) }.orDefault()
            return ParadoxFilePathInfo(path, gameType)
        }
    }
}
