package icu.windea.pls.core.index

import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.*
import java.util.*

class ParadoxFilePathIndex : ScalarIndexExtension<String>() {
    companion object {
        @JvmField val NAME = ID.create<String, Void>("paradox.file.path.index")
        private const val VERSION = 22 //1.0.0
    }
    
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun getIndexer(): DataIndexer<String, Void, FileContent> {
        return DataIndexer { inputData ->
            val fileInfo = inputData.file.fileInfo ?: return@DataIndexer emptyMap()
            val path = fileInfo.path.path
            Collections.singletonMap<String, Void>(path, null)
        }
    }
    
    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }
    
    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { it.fileInfo != null }
    }
    
    override fun dependsOnFileContent(): Boolean {
        return false
    }
}

