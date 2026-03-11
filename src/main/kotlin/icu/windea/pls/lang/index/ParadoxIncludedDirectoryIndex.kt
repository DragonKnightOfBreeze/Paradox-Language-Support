package icu.windea.pls.lang.index

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ScalarIndexExtension
import com.intellij.util.io.EnumeratorStringDescriptor
import icu.windea.pls.core.IndexInputFilter
import icu.windea.pls.lang.fileInfo
import java.util.*

/**
 * 已包含的目录的索引。
 *
 * 键的格式为 `{gameTypeId}:{directoryPath}`，其中 `{directoryPath}` 是相对于入口目录的路径。
 *
 * 仅索引通过包含检查的目录（排除隐藏目录和某些特定目录）。
 *
 * @see PlsIndexUtil.isIncludedDirectory
 */
class ParadoxIncludedDirectoryIndex : ScalarIndexExtension<String>() {
    private val inputFilter = IndexInputFilter { it.fileInfo != null }
    private val indexer = DataIndexer<String, Void, FileContent> { indexData(it) }
    private val keyDescriptor = EnumeratorStringDescriptor.INSTANCE

    override fun getName() = PlsIndexKeys.IncludedDirectory

    override fun getVersion() = PlsIndexVersions.IncludedDirectory

    override fun getInputFilter() = inputFilter

    override fun dependsOnFileContent() = false

    override fun getIndexer() = indexer

    override fun getKeyDescriptor() = keyDescriptor

    override fun indexDirectories() = true

    private fun indexData(fileContent: FileContent): Map<String, Void?> {
        val file = fileContent.file
        if (!file.isDirectory) return emptyMap()
        if (!PlsIndexUtil.isIncludedDirectory(file)) return emptyMap()
        val fileInfo = file.fileInfo ?: return emptyMap()
        val gameType = fileInfo.rootInfo.gameType
        val path = fileInfo.path.path
        val key = "${gameType.id}:$path"
        return Collections.singletonMap(key, null)
    }
}
