package icu.windea.pls.lang.index

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*
import java.io.*

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
