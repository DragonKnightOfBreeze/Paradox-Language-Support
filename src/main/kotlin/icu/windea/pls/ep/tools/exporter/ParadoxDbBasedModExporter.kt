package icu.windea.pls.ep.tools.exporter

import com.intellij.icons.AllIcons
import icu.windea.pls.model.ParadoxGameType

/**
 * 使用 SQLite 数据库文件作为数据文件的模组导出器。
 */
abstract class ParadoxDbBasedModExporter : ParadoxModExporter {
    override val icon = AllIcons.Providers.Sqlite

    /** 得到默认使用的 JSON 文件的名字。*/
    abstract fun getJsonFileName(gameType: ParadoxGameType): String
}
