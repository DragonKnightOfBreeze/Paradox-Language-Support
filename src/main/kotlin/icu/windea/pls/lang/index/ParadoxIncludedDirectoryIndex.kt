package icu.windea.pls.lang.index

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ScalarIndexExtension
import com.intellij.util.indexing.hints.AcceptAllFilesAndDirectoriesIndexingHint
import com.intellij.util.io.EnumeratorStringDescriptor
import icu.windea.pls.lang.fileInfo
import java.util.*

/**
 * 已包含的目录的索引。
 *
 * 键的格式为 `{gameTypeId}:{directoryPath}`，其中 `{directoryPath}` 是相对于入口目录的路径。
 *
 * 仅索引通过包含检查的目录（排除隐藏目录和某些特定目录）。
 */
class ParadoxIncludedDirectoryIndex : ScalarIndexExtension<String>() {
    // NOTE 3.0.1 can be any directories - use `AcceptAllFilesAndDirectoriesIndexingHint` to speed up scanning
    @Suppress("UnstableApiUsage")
    private val inputFilter = AcceptAllFilesAndDirectoriesIndexingHint
    private val indexer = DataIndexer<String, Void, FileContent> { indexData(it) }
    private val keyDescriptor = EnumeratorStringDescriptor.INSTANCE

    override fun getName() = ChronicleIndexKeys.IncludedDirectory

    override fun getVersion() = ChronicleIndexVersions.IncludedDirectory

    override fun getInputFilter() = inputFilter

    override fun indexDirectories() = true

    override fun dependsOnFileContent() = false

    override fun getIndexer() = indexer

    override fun getKeyDescriptor() = keyDescriptor

    private fun indexData(fileContent: FileContent): Map<String, Void?> {
        val file = fileContent.file
        if (!file.isDirectory) return emptyMap()
        if (ChronicleIndexUtil.isExcludedDirectory(file)) return emptyMap()
        val fileInfo = file.fileInfo ?: return emptyMap()
        val gameType = fileInfo.gameType
        val path = fileInfo.path.path
        val key = "${gameType.id}:$path"
        return Collections.singletonMap(key, null)
    }
}
