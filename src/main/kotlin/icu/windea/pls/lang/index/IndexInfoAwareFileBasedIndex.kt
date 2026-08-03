package icu.windea.pls.lang.index

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.gist.GistManager
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.indexing.hints.FileTypeInputFilterPredicate
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.readIntFast
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.writeIntFast
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.IndexInfo
import java.io.DataInput
import java.io.DataOutput
import java.util.Collections.*

/**
 * 各种索引信息的文件索引的基类。
 */
@Optimized
sealed class IndexInfoAwareFileBasedIndex<V, out T : IndexInfo> : FileBasedIndexExtension<String, V>() {
    // NOTE 3.0.1 mainly depends on specific file types - use `FileTypeInputFilterPredicate` to speed up scanning
    @Suppress("UnstableApiUsage")
    private val inputFilter = FileTypeInputFilterPredicate { filterFileType(it) }
    private val indexer = DataIndexer<String, V, FileContent> { indexData(it) }
    private val keyDescriptor = EnumeratorStringDescriptor.INSTANCE
    private val valueExternalizer = object : DataExternalizer<V> {
        override fun save(storage: DataOutput, value: V) = saveValue(storage, value)
        override fun read(storage: DataInput) = readValue(storage)
    }

    // NOTE 2.0.6 优先使用 `VirtualFileGist`（验证发现 `PsiFileGist` 有时会不稳定）
    private val gistValueExternalizer by lazy {
        object : DataExternalizer<Map<String, V>> {
            override fun save(storage: DataOutput, value: Map<String, V>) = saveGistValue(storage, value)
            override fun read(storage: DataInput) = readGistValue(storage)
        }
    }
    private val gist by lazy {
        val gistName = name.name + ".lazy"
        val gistVersion = version
        GistManager.getInstance().newVirtualFileGist(gistName, gistVersion, gistValueExternalizer) { project, file -> calculateGistData(project, file) }
    }

    abstract override fun getName(): ID<String, V>

    override fun dependsOnFileContent() = true

    override fun getInputFilter() = inputFilter

    override fun getIndexer() = indexer

    override fun getKeyDescriptor() = keyDescriptor

    override fun getValueExternalizer() = valueExternalizer

    protected open fun filterFileType(fileType: FileType): Boolean = true

    protected open fun filterFile(file: VirtualFile): Boolean = true

    protected open fun indexData(fileContent: FileContent): Map<String, V> {
        // fast return (unnecessary, pre-checked by the input filter)
        // if (!filterFileType(fileContent.fileType)) return emptyMap()
        // fast return
        if (!filterFile(fileContent.file)) return emptyMap()

        if (useLazyIndex(fileContent.file)) {
            // use lazy index (`VirtualFileGist`)
            return indexLazyData(fileContent.psiFile)
        }

        // use file based index (`FileBasedIndex`)
        val result = indexData(fileContent.psiFile)
        if (result.isEmpty()) return emptyMap() // 3.0.1 optimize: for empty map
        return result
    }

    protected open fun indexData(psiFile: PsiFile): Map<String, V> = emptyMap()

    protected open fun useLazyIndex(file: VirtualFile): Boolean = false

    protected open fun indexLazyData(psiFile: PsiFile): Map<String, V> = emptyMap()

    protected abstract fun saveValue(storage: DataOutput, value: V)

    protected abstract fun readValue(storage: DataInput): V

    private fun calculateGistData(project: Project, file: VirtualFile): Map<String, V> {
        // fast return
        if (!filterFileType(file.fileType)) return emptyMap()
        // fast return
        if (!filterFile(file)) return emptyMap()

        val psiFile = file.toPsiFile(project) ?: return emptyMap()
        val result = indexData(psiFile)
        if (result.isEmpty()) return emptyMap() // 3.0.1 optimize: for empty map
        return result
    }

    private fun saveGistValue(storage: DataOutput, value: Map<String, V>) {
        storage.writeIntFast(value.size)
        value.forEach { (k, infos) ->
            storage.writeUTFFast(k)
            saveValue(storage, infos)
        }
    }

    private fun readGistValue(storage: DataInput): Map<String, V> {
        val fileData = buildMap {
            repeat(storage.readIntFast()) {
                val key = storage.readUTFFast()
                val value = readValue(storage)
                put(key, value)
            }
        }
        if (fileData.isEmpty()) return emptyMap()
        return fileData
    }

    open fun checkFile(file: VirtualFile, project: Project, expectGameType: ParadoxGameType?): Boolean {
        return true
    }

    fun getFileData(file: VirtualFile, project: Project): Map<String, V> {
        // fast return
        if (!filterFileType(file.fileType)) return emptyMap()
        // fast return
        if (!filterFile(file)) return emptyMap()

        if (useLazyIndex(file)) {
            // use lazy index (`VirtualFileGist`)
            return gist.getFileData(project, file).orEmpty()
        }

        // use file based index (`FileBasedIndex`)
        return FileBasedIndex.getInstance().getFileData(name, file, project)
    }

    fun getFileDataWithKey(file: VirtualFile, project: Project, key: String): V? {
        // fast return
        if (!filterFileType(file.fileType)) return null
        // fast return
        if (!filterFile(file)) return null

        if (useLazyIndex(file)) {
            // use lazy index (`VirtualFileGist`)
            return gist.getFileData(project, file)?.get(key)
        }

        // use file based index (`FileBasedIndex`)
        // use fast return value processor to optimize performance
        val valueProcessor = FastReturnValueProcessor<V>()
        FileBasedIndex.getInstance().processValues(name, key, file, valueProcessor, GlobalSearchScope.fileScope(project, file))
        return valueProcessor.result
    }

    private class FastReturnValueProcessor<V> : FileBasedIndex.ValueProcessor<V> {
        var result: V? = null

        override fun process(file: VirtualFile, value: V): Boolean {
            result = value
            return false
        }
    }
}

