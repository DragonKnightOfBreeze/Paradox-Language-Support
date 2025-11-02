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
import java.io.DataInput
import java.io.DataOutput

/**
 * @see icu.windea.pls.model.index.IndexInfo
 */
abstract class IndexInfoAwareFileBasedIndex<T> : FileBasedIndexExtension<String, T>() {
    private val inputFilter = IndexInputFilter { filterFile(it) }
    private val indexer = DataIndexer<String, T, FileContent> { calculateData(it.psiFile) }
    private val keyDescriptor = EnumeratorStringDescriptor.INSTANCE
    private val valueExternalizer = object : DataExternalizer<T> {
        override fun save(storage: DataOutput, value: T) = saveValue(storage, value)
        override fun read(storage: DataInput) = readValue(storage)
    }

    private val gistValueExternalizer by lazy {
        object : DataExternalizer<Map<String, T>> {
            override fun save(storage: DataOutput, value: Map<String, T>) = saveGistValue(storage, value)
            override fun read(storage: DataInput) = readGistValue(storage)
        }
    }
    private val gist by lazy {
        val gistName = name.name + ".lazy"
        val gistVersion = version
        GistManager.getInstance().newPsiFileGist(gistName, gistVersion, gistValueExternalizer) { calculateGistData(it) }
    }

    abstract override fun getName(): ID<String, T>

    override fun getInputFilter() = inputFilter

    override fun dependsOnFileContent() = true

    override fun getIndexer() = indexer

    override fun getKeyDescriptor() = keyDescriptor

    override fun getValueExternalizer() = valueExternalizer

    protected open fun filterFile(file: VirtualFile): Boolean = true

    protected open fun useLazyIndex(file: VirtualFile): Boolean = false

    protected abstract fun indexData(psiFile: PsiFile): Map<String, T>

    protected open fun indexLazyData(psiFile: PsiFile): Map<String, T> = emptyMap()

    protected abstract fun saveValue(storage: DataOutput, value: T)

    protected abstract fun readValue(storage: DataInput): T

    private fun calculateData(psiFile: PsiFile): Map<String, T> {
        if (useLazyIndex(psiFile.virtualFile)) {
            return indexLazyData(psiFile)
        }
        return indexData(psiFile)
    }

    private fun calculateGistData(psiFile: PsiFile): Map<String, T> {
        val file = psiFile.virtualFile ?: return emptyMap()
        if (!filterFile(file)) return emptyMap()
        if (!useLazyIndex(file)) return emptyMap()
        return indexData(psiFile)
    }

    private fun saveGistValue(storage: DataOutput, value: Map<String, T>) {
        storage.writeIntFast(value.size)
        value.forEach { (k, infos) ->
            storage.writeUTFFast(k)
            saveValue(storage, infos)
        }
    }

    private fun readGistValue(storage: DataInput): Map<String, T> = buildMap {
        repeat(storage.readIntFast()) {
            val key = storage.readUTFFast()
            val value = readValue(storage)
            put(key, value)
        }
    }

    fun getFileData(file: VirtualFile, project: Project): Map<String, T> {
        if (useLazyIndex(file)) {
            val psiFile = file.toPsiFile(project) ?: return emptyMap()
            return gist.getFileData(psiFile)
        }
        return FileBasedIndex.getInstance().getFileData(name, file, project)
    }

    fun getFileDataWithKey(file: VirtualFile, project: Project, key: String): T? {
        if (useLazyIndex(file)) {
            val psiFile = file.toPsiFile(project) ?: return null
            val fileData = gist.getFileData(psiFile) ?: return null
            return fileData[key]
        }
        val values = FileBasedIndex.getInstance().getValues(name, key, GlobalSearchScope.fileScope(project, file))
        return values.firstOrNull()
    }
}
