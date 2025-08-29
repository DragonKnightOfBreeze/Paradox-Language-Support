package icu.windea.pls.lang.index

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.gist.GistManager
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import icu.windea.pls.core.property
import icu.windea.pls.core.readIntFast
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.writeIntFast
import icu.windea.pls.core.writeUTFFast
import java.io.DataInput
import java.io.DataOutput

abstract class ParadoxFileBasedIndex<T> : FileBasedIndexExtension<String, T>() {
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
        return FileBasedIndex.InputFilter { file -> filterFile(file) && !useLazyIndex(file) }
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    private val indexName by lazy {
        property<ParadoxFileBasedIndex<T>, ID<String, T>>("name").get()
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
        val gistName = indexName.name + ".lazy"
        val gistVersion = version
        GistManager.getInstance().newVirtualFileGist(gistName, gistVersion, gistValueExternalizer) builder@{ project, file ->
            if (!filterFile(file)) return@builder emptyMap()
            if (!useLazyIndex(file)) return@builder emptyMap()
            val psiFile = file.toPsiFile(project) ?: return@builder emptyMap()
            buildFileData(psiFile)
        }
    }

    private fun buildFileData(file: PsiFile): Map<String, T> {
        return buildMap {
            indexData(file, this)
        }
    }

    protected abstract fun indexData(file: PsiFile, fileData: MutableMap<String, T>)

    protected abstract fun writeData(storage: DataOutput, value: T)

    protected abstract fun readData(storage: DataInput): T

    protected abstract fun filterFile(file: VirtualFile): Boolean

    protected open fun useLazyIndex(file: VirtualFile): Boolean = false

    fun getFileData(file: VirtualFile, project: Project): Map<String, T> {
        val useLazyIndex = useLazyIndex(file)
        if (useLazyIndex) return gist.getFileData(project, file)
        return FileBasedIndex.getInstance().getFileData(indexName, file, project)
    }
}
