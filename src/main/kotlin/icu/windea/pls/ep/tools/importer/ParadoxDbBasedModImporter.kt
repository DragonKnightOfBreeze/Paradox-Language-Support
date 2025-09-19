package icu.windea.pls.ep.tools.importer

import com.intellij.icons.AllIcons

/**
 * 使用 SQLite 数据库文件作为数据文件的模组导入器。
 */
abstract class ParadoxDbBasedModImporter: ParadoxModImporter {
    override val icon = AllIcons.Providers.Sqlite
}
