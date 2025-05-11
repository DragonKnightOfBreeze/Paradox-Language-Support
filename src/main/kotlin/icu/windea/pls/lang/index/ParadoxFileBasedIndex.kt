package icu.windea.pls.lang.index

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.gist.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import java.io.*

abstract class ParadoxFileBasedIndex<T : Any> : SingleEntryFileBasedIndexExtension<Map<String, T>>() {
    abstract override fun getVersion(): Int

    override fun getIndexer(): SingleEntryIndexer<Map<String, T>> {
        return object : SingleEntryIndexer<Map<String, T>>(false) {
            override fun computeValue(inputData: FileContent): Map<String, T> {
                val psiFile = inputData.psiFile
                return buildFileData(psiFile)
            }
        }
    }

    override fun getValueExternalizer(): DataExternalizer<Map<String, T>> {
        return object : DataExternalizer<Map<String, T>> {
            override fun save(storage: DataOutput, value: Map<String, T>) {
                storage.writeIntFast(value.size)
                if (value.isEmpty()) return
                value.forEach { (k, v) ->
                    storage.writeUTFFast(k)
                    writeData(storage, v)
                }
            }

            override fun read(storage: DataInput): Map<String, T> {
                val size = storage.readIntFast()
                if (size == 0) return emptyMap()
                return buildMap {
                    repeat(size) {
                        val key = storage.readUTFFast()
                        val value = readData(storage)
                        put(key, value)
                    }
                }
            }
        }
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { file -> filterFile(file) && !useLazyIndex(file) }
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
            try {
                PlsManager.indexing.set(true)
                indexData(file, this)
            } finally {
                PlsManager.indexing.remove()
            }
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
