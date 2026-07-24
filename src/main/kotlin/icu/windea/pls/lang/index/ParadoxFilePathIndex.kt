package icu.windea.pls.lang.index

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ScalarIndexExtension
import com.intellij.util.indexing.hints.AcceptAllFilesAndDirectoriesIndexingHint
import com.intellij.util.io.EnumeratorStringDescriptor
import icu.windea.pls.lang.fileInfo
import java.util.*

/**
 * 文件的路径信息的索引。
 *
 * 键为相对于入口目录的路径。
 */
class ParadoxFilePathIndex : ScalarIndexExtension<String>() {
    // NOTE 3.0.1 can be any files (with any file types) and directories - use `AcceptAllFilesAndDirectoriesIndexingHint` to speed up scanning
    @Suppress("UnstableApiUsage")
    private val inputFilter = AcceptAllFilesAndDirectoriesIndexingHint
    private val indexer = DataIndexer<String, Void, FileContent> { indexData(it) }
    private val keyDescriptor = EnumeratorStringDescriptor.INSTANCE

    override fun getName() = ChronicleIndexKeys.FilePath

    override fun getVersion() = ChronicleIndexVersions.FilePath

    override fun dependsOnFileContent() = false

    override fun indexDirectories() = true

    override fun getInputFilter() = inputFilter

    override fun getIndexer() = indexer

    override fun getKeyDescriptor() = keyDescriptor

    private fun indexData(fileContent: FileContent): Map<String, Void?> {
        // indexed path here is relative to entry path
        val file = fileContent.file
        val fileInfo = file.fileInfo ?: return emptyMap()
        val path = fileInfo.path.path
        return Collections.singletonMap(path, null)
    }
}
