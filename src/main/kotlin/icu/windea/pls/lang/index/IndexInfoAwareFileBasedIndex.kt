package icu.windea.pls.lang.index

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import icu.windea.pls.core.IndexInputFilter
import java.io.DataInput
import java.io.DataOutput

/**
 * @see icu.windea.pls.model.index.IndexInfo
 */
abstract class IndexInfoAwareFileBasedIndex<T> : FileBasedIndexExtension<String, T>() {
    private val inputFilter = IndexInputFilter(*filterFileTypes()) { filterFile(it) }
    private val indexer = DataIndexer<String, T, FileContent> { indexData(it.psiFile) }
    private val keyDescriptor = EnumeratorStringDescriptor.INSTANCE
    private val valueExternalizer = object : DataExternalizer<T> {
        override fun save(storage: DataOutput, value: T) = saveValue(storage, value)
        override fun read(storage: DataInput) = readValue(storage)
    }

    abstract override fun getName(): ID<String, T>

    override fun getInputFilter() = inputFilter

    override fun dependsOnFileContent() = true

    override fun getIndexer() = indexer

    override fun getKeyDescriptor() = keyDescriptor

    override fun getValueExternalizer() = valueExternalizer

    protected open fun filterFileTypes(): Array<FileType> = FileType.EMPTY_ARRAY

    protected open fun filterFile(file: VirtualFile): Boolean = true

    protected abstract fun indexData(psiFile: PsiFile): Map<String, T>

    protected abstract fun saveValue(storage: DataOutput, value: T)

    protected abstract fun readValue(storage: DataInput): T

    fun getFileData(file: VirtualFile, project: Project): Map<String, T> {
        return FileBasedIndex.getInstance().getFileData(name, file, project)
    }
}
