package icu.windea.pls.core.index

import com.intellij.openapi.util.registry.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.lang.model.*
import java.util.*

class ParadoxFilePathIndex : ScalarIndexExtension<ParadoxFilePathInfo>() {
    companion object {
        @JvmField val NAME = ID.create<ParadoxFilePathInfo, Void>("paradox.file.path.index")
    }
    
    override fun getName(): ID<ParadoxFilePathInfo, Void> {
        return NAME
    }
    
    override fun getVersion(): Int {
        return 4 + (if(Registry.`is`("indexing.paradox.file.path.over.vfs")) 0xff else 0)
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
    
    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { it.fileInfo != null }
    }
    
    override fun dependsOnFileContent(): Boolean {
        return false
    }
}

