package icu.windea.pls.lang.index

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
import com.intellij.util.io.KeyDescriptor
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.core.property
import icu.windea.pls.core.readIntFast
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.writeIntFast
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.cwt.CwtFileType
import java.io.DataInput
import java.io.DataOutput

abstract class CwtConfigFileBasedIndex<T>: FileBasedIndexExtension<String, T>() {
    override fun getIndexer(): DataIndexer<String, T, FileContent> {
        return DataIndexer { inputData ->
            val psiFile = inputData.psiFile
            buildFileData(psiFile)
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
        return FileBasedIndex.InputFilter { file -> file.fileType is CwtFileType && CwtConfigManager.getContainingConfigGroup(file, getDefaultProject(), forRepo = true) != null }
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    private val indexName by lazy {
        property<CwtConfigFileBasedIndex<T>, ID<String, T>>("name").get()
    }

    private fun buildFileData(file: PsiFile): Map<String, T> {
        return buildMap {
            indexData(file, this)
        }
    }

    protected abstract fun indexData(file: PsiFile, fileData: MutableMap<String, T>)

    protected abstract fun writeData(storage: DataOutput, value: T)

    protected abstract fun readData(storage: DataInput): T

    fun getFileData(file: VirtualFile, project: Project): Map<String, T> {
        return FileBasedIndex.getInstance().getFileData(indexName, file, project)
    }
}
