package icu.windea.pls.ep.tools.importer

import com.intellij.icons.AllIcons
import icu.windea.pls.PlsFacade
import icu.windea.pls.model.ParadoxGameType

/**
 * 使用 SQLite 数据库文件作为数据文件的模组导入器。
 */
abstract class ParadoxDbBasedModImporter : ParadoxModImporter {
    override val icon = AllIcons.Providers.Sqlite

    override fun isAvailable(gameType: ParadoxGameType) = PlsFacade.Capacities.includeSqlite()
}
