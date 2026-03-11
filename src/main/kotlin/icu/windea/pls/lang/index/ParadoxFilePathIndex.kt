package icu.windea.pls.lang.index

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ScalarIndexExtension
import com.intellij.util.io.EnumeratorStringDescriptor
import icu.windea.pls.core.IndexInputFilter
import icu.windea.pls.lang.fileInfo
import java.util.*

/**
 * 文件的路径信息的索引。
 *
 * 键为相对于入口目录的路径。
 */
class ParadoxFilePathIndex : ScalarIndexExtension<String>() {
    private val inputFilter = IndexInputFilter { it.fileInfo != null }
    private val indexer = DataIndexer<String, Void, FileContent> { indexData(it) }
    private val keyDescriptor = EnumeratorStringDescriptor.INSTANCE

    override fun getName() = PlsIndexKeys.FilePath

    override fun getVersion() = PlsIndexVersions.FilePath

    override fun getInputFilter() = inputFilter

    override fun dependsOnFileContent() = false

    override fun getIndexer() = indexer

    override fun getKeyDescriptor() = keyDescriptor

    override fun indexDirectories() = true

    private fun indexData(fileContent: FileContent): Map<String, Void?> {
        // 这里索引的路径，使用相对于入口目录的路径
        val file = fileContent.file
        val fileInfo = file.fileInfo ?: return emptyMap()
        val path = fileInfo.path.path
        return Collections.singletonMap(path, null)
    }
}
