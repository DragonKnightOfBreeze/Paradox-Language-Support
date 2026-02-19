package icu.windea.pls.lang.index

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
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import icu.windea.pls.core.IndexInputFilter
import icu.windea.pls.core.readIntFast
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.writeIntFast
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.IndexInfo
import java.io.DataInput
import java.io.DataOutput

/**
 * 各种索引信息的文件索引的基类。
 */
sealed class IndexInfoAwareFileBasedIndex<V, out T : IndexInfo> : FileBasedIndexExtension<String, V>() {
    private val inputFilter = IndexInputFilter { filterFile(it) }
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

    override fun getInputFilter() = inputFilter

    override fun dependsOnFileContent() = true

    override fun getIndexer() = indexer

    override fun getKeyDescriptor() = keyDescriptor

    override fun getValueExternalizer() = valueExternalizer

    protected open fun filterFile(file: VirtualFile): Boolean = true

    protected open fun useLazyIndex(file: VirtualFile): Boolean = false

    protected open fun indexData(fileContent: FileContent): Map<String, V> {
        val fileData = when {
            useLazyIndex(fileContent.file) -> indexLazyData(fileContent.psiFile)
            else -> indexData(fileContent.psiFile)
        }
        if (fileData.isEmpty()) return emptyMap()
        return fileData
    }

    protected open fun indexData(psiFile: PsiFile): Map<String, V> = emptyMap()

    protected open fun indexLazyData(psiFile: PsiFile): Map<String, V> = emptyMap()

    protected abstract fun saveValue(storage: DataOutput, value: V)

    protected abstract fun readValue(storage: DataInput): V

    private fun calculateGistData(project: Project, file: VirtualFile): Map<String, V> {
        if (!filterFile(file)) return emptyMap()
        val psiFile = file.toPsiFile(project) ?: return emptyMap()
        return indexData(psiFile)
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
        if (useLazyIndex(file)) {
            return gist.getFileData(project, file)
        }
        return FileBasedIndex.getInstance().getFileData(name, file, project)
    }

    fun getFileDataWithKey(file: VirtualFile, project: Project, key: String): V? {
        if (useLazyIndex(file)) {
            val fileData = gist.getFileData(project, file) ?: return null
            return fileData[key]
        }
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

