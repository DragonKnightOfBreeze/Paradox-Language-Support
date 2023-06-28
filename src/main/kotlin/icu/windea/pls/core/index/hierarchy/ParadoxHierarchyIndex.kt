package icu.windea.pls.core.index.hierarchy

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.gist.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.core.*
import java.io.*

/**
 * 基于脚本层级的索引。
 * 
 * 构建此类索引时需要遍历脚本文件的内容，一般仅需遍历脚本文件中的所有表达式。
 */
abstract class ParadoxHierarchyIndex<T>: FileBasedIndexExtension<String, T>() {
    abstract override fun getName(): ID<String, T>
    
    abstract override fun getVersion(): Int
    
    override fun getIndexer(): DataIndexer<String, T, FileContent> {
        return DataIndexer { inputData ->
            val file = inputData.psiFile
            buildMap { indexData(file, this) }
        }
    }
    
    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }
    
    override fun getValueExternalizer(): DataExternalizer<T> {
        return object : DataExternalizer<T> {
            override fun save(storage: DataOutput, value: T) {
                writeData(storage, value)
            }
            
            override fun read(storage: DataInput): T {
                return readData(storage)
            }
        }
    }
    
    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { file -> filterFile(file) && !useLazyIndex(file) }
    }
    
    override fun dependsOnFileContent(): Boolean {
        return true
    }
    
    private val gistValueExternalizer by lazy { 
        object : DataExternalizer<Map<String, T>> {
            override fun save(storage: DataOutput, value: Map<String, T>) {
                storage.writeIntFast(value.size)
                value.forEach { (k, infos) ->
                    storage.writeUTFFast(k)
                    writeData(storage, infos)
                }
            }
            
            override fun read(storage: DataInput): Map<String, T> {
                return buildMap {
                    repeat(storage.readIntFast()) {
                        val key = storage.readUTFFast()
                        val value = readData(storage)
                        put(key, value)
                    }
                }
            }
        }
    }
    
    private val gist by lazy { 
        val gistName = name.name + ".lazy"
        val gistVersion = version
        GistManager.getInstance().newVirtualFileGist(gistName, gistVersion, gistValueExternalizer) builder@{ project, file ->
            if(!filterFile(file)) return@builder emptyMap()
            if(!useLazyIndex(file)) return@builder emptyMap()
            val psiFile = file.toPsiFile(project) ?: return@builder emptyMap()
            buildMap { indexData(psiFile, this) }
        }
    }
    
    protected abstract fun indexData(file: PsiFile, fileData: MutableMap<String, T>)
    
    protected abstract fun writeData(storage: DataOutput, value: T) 
    
    protected abstract fun readData(storage: DataInput): T
    
    protected abstract fun filterFile(file: VirtualFile): Boolean
    
    open fun useLazyIndex(file: VirtualFile): Boolean = false
    
    fun getFileData(file: VirtualFile, project: Project): Map<String, T> {
        val useLazyIndex = useLazyIndex(file)
        if(useLazyIndex) return gist.getFileData(project, file)
        return FileBasedIndex.getInstance().getFileData(name, file, project)
    }
}