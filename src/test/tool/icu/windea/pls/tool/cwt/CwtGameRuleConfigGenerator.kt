package icu.windea.pls.tool.cwt

import icu.windea.pls.PlsFacade
import icu.windea.pls.model.ParadoxGameType
import java.io.File

/**
 * 用于比较`game_rules.txt`与`game_rules.cwt`，输出新增项和移除项。
 */
class CwtGameRuleConfigGenerator(
    val gameType: ParadoxGameType,
    val txtDirPath: String,
    val cwtPath: String,
) {
    fun generate() {
        val oldItems = mutableSetOf<String>()
        val oldItemRegex = """\s+([\w<>]+)(?:\s*=\s*\{.*)?""".toRegex()
        val cwtFile = File(cwtPath)
        cwtFile.forEachLine {
            val oldName = oldItemRegex.matchEntire(it)?.groupValues?.getOrNull(1)
            if (oldName != null) oldItems.add(oldName)
        }
        val gamePath = PlsFacade.getDataProvider().getSteamGamePath(gameType.id, gameType.title) ?: throw IllegalStateException()
        val newItems = mutableSetOf<String>()
        val txtDirFile = gamePath.resolve(txtDirPath).toFile()
        txtDirFile.walk().filter { it.isFile && it.extension == "txt" }.forEach { txtFile ->
            val newItemRegex = """(\w+)\s*=\s*\{.*""".toRegex()
            txtFile.forEachLine {
                val newName = newItemRegex.matchEntire(it)?.groupValues?.getOrNull(1)
                if (newName != null) newItems.add(newName)
            }
        }
        val old = oldItems.toMutableSet().apply { removeAll(newItems) }
        val new = newItems.toMutableSet().apply { removeAll(oldItems) }
        println("Deleted game rules:")
        for (n in old) {
            println(n)
        }
        println()
        println("Added game rules:")
        for (n in new) {
            println(n)
        }
    }
}
