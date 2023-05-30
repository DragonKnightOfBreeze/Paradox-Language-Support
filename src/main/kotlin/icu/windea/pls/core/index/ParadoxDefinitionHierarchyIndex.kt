package icu.windea.pls.core.index

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.script.*

/**
 * 用于索引定义声明中的定义引用、参数引用、本地化参数引用等。
 */
class ParadoxDefinitionHierarchyIndex: FileBasedIndexExtension<String, String>() {
    companion object {
        @JvmField val NAME = ID.create<String, String>("paradox.definition.hierarchy.index")
        private const val VERSION = 26 //1.0.4
        
        fun getData(file: VirtualFile, project: Project): Map<String, String> {
            return FileBasedIndex.getInstance().getFileData(NAME, file, project)
        }
    }
    
    data class ContextInfo(
        val expression: String,
        val configExpression: CwtDataExpression,
        val definitionInfo: DefinitionInfo
    ): UserDataHolderBase()
    
    data class DefinitionInfo(
        val name: String,
        val type: String,
        val subtypes: List<String>
    )
    
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun getIndexer(): DataIndexer<String, String, FileContent> {
        TODO("Not yet implemented")
    }
    
    override fun getKeyDescriptor(): KeyDescriptor<String> {
        TODO("Not yet implemented")
    }
    
    override fun getValueExternalizer(): DataExternalizer<String> {
        TODO("Not yet implemented")
    }
    
    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter p@{ file ->
            val fileType = file.fileType
            if(fileType != ParadoxScriptFileType) return@p false
            if(file.fileInfo == null) return@p false
            true
        }
    }
    
    override fun dependsOnFileContent(): Boolean {
        return true
    }
}