package icu.windea.pls.dev.cwt

import icu.windea.pls.core.*
import icu.windea.pls.model.*
import java.io.*

/**
 * 用于比较`game_rules.txt`与`game_rules.cwt`，输出新增项和移除项。
 */
class CwtGameRuleConfigGenerator(
    val gameType: ParadoxGameType,
    val txtPath: String,
    val cwtPath: String,
) {
    fun generate() {
        val cwtFile = File(cwtPath)
        val oldItems = mutableSetOf<String>()
        val oldItemRegex = """\s+([\w<>]+)(?:\s*=\s*\{.*)?""".toRegex()
        cwtFile.forEachLine {
            val oldName = oldItemRegex.matchEntire(it)?.groupValues?.getOrNull(1)
            if(oldName != null) oldItems.add(oldName)
        }
        val gamePath = getSteamGamePath(gameType.id, gameType.title) ?: throw IllegalStateException()
        val txtFile = File(gamePath, txtPath)
        val newItems = mutableSetOf<String>()
        val newItemRegex = """(\w+)\s*=\s*\{.*""".toRegex()
        txtFile.forEachLine {
            val newName = newItemRegex.matchEntire(it)?.groupValues?.getOrNull(1)
            if(newName != null) newItems.add(newName)
        }
        val old = oldItems.toMutableSet().apply { removeAll(newItems) }
        val new = newItems.toMutableSet().apply { removeAll(oldItems) }
        println("Deleted game rules:")
        for(n in old) {
            println(n)
        }
        println()
        println("Added game rules:")
        for(n in new) {
            println(n)
        }
    }
}