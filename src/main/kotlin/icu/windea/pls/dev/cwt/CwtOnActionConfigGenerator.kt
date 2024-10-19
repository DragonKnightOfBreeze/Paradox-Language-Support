package icu.windea.pls.dev.cwt

import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import java.io.*

/**
 * 用于比较`on_actions.txt`与`on_actions.cwt`，输出新增项和移除项。
 */
class CwtOnActionConfigGenerator(
    val gameType: ParadoxGameType,
    val txtDirPath: String,
    val cwtPath: String,
) {
    fun generate() {
        val cwtFile = File(cwtPath)
        val oldItems = mutableSetOf<String>()
        val oldItemRegex = """\s+([\w<>]+)(?:\s*=\s*\{.*)?""".toRegex()
        cwtFile.forEachLine {
            val oldName = oldItemRegex.matchEntire(it)?.groupValues?.getOrNull(1)
            if (oldName != null) oldItems.add(oldName)
        }
        val gamePath = getDataProvider().getSteamGamePath(gameType.id, gameType.title) ?: throw IllegalStateException()
        val newItems = mutableSetOf<String>()
        val txtDirFile = File(gamePath, txtDirPath)
        txtDirFile.walk().filter { it.isFile && it.extension == "txt" }.forEach { txtFile ->
            val newItemRegex = """(\w+)\s*=\s*\{.*""".toRegex()
            txtFile.forEachLine {
                val newName = newItemRegex.matchEntire(it)?.groupValues?.getOrNull(1)
                if (newName != null) newItems.add(newName)
            }
        }
        val old = oldItems.toMutableSet().apply { removeAll(newItems) }
        val new = newItems.toMutableSet().apply { removeAll(oldItems) }
        println("Deleted on actions:")
        for (n in old) {
            println(n)
        }
        println()
        println("Added on actions:")
        for (n in new) {
            println(n)
        }
    }
}
