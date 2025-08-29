package icu.windea.pls.tool.cwt

import icu.windea.pls.core.removeSurrounding
import icu.windea.pls.core.surroundsWith
import icu.windea.pls.model.ParadoxGameType
import java.io.File

/**
 * 用于从`localisations.log`生成
 */
class CwtLocalisationConfigGenerator(
    val gameType: ParadoxGameType,
    val logPath: String,
    val cwtPath: String,
) {
    data class LocalisationInfo(
        var name: String = "",
        val promotions: MutableSet<String> = mutableSetOf(),
        val properties: MutableSet<String> = mutableSetOf(),
    )

    enum class Position {
        ScopeName, Promotions, Properties
    }

    fun generate() {
        val logFile = File(logPath)
        val infos = mutableListOf<LocalisationInfo>()
        var info = LocalisationInfo()
        var position = Position.ScopeName
        val allLines = logFile.bufferedReader().readLines()
        for (line in allLines) {
            val l = line.trim()
            if (l.surroundsWith("--", "--")) {
                if (info.name.isNotEmpty()) {
                    infos.add(info)
                    info = LocalisationInfo()
                }
                info.name = l.removeSurrounding("--", "--")
                position = Position.ScopeName
                continue
            }
            if (l == "Promotions:") {
                position = Position.Promotions
                continue
            }
            if (l == "Properties") {
                position = Position.Properties
                continue
            }
            when (position) {
                Position.Promotions -> {
                    val v = l.takeIf { it.isNotEmpty() && it.all { c -> c != '=' } }
                    if (v != null) info.promotions.add(v)
                }
                Position.Properties -> {
                    val v = l.takeIf { it.isNotEmpty() && it.all { c -> c != '=' } }
                    if (v != null) info.properties.add(v)
                }
                else -> {}
            }
        }
        if (info.name.isNotEmpty()) {
            infos.add(info)
        }
        val newText = getText(infos)
        val cwtFile = File(cwtPath)
        cwtFile.writeText(newText)
    }

    private fun getText(infos: List<LocalisationInfo>): String {
        return buildString {
            run {
                append("localisation_promotions = {").appendLine()
                val map = mutableMapOf<String, MutableSet<String>>()
                infos.forEach { info ->
                    val scopeIds = getScopeIds(info.name)
                    info.promotions.forEach { prop ->
                        val scopes = map.getOrPut(prop) { mutableSetOf() }
                        scopes += scopeIds
                    }
                }
                map.forEach { (k, v) ->
                    val s = when {
                        v.isEmpty() -> return@forEach
                        v.contains("any") -> return@forEach
                        else -> v.joinToString(" ", "{ ", " }")
                    }
                    append("    ").append("$k = $s").appendLine()
                }
                append("}").appendLine()
            }

            run {
                append("localisation_commands = {").appendLine()
                val map = mutableMapOf<String, MutableSet<String>>()
                infos.forEach { info ->
                    val scopeIds = getScopeIds(info.name)
                    info.properties.forEach { prop ->
                        val scopes = map.getOrPut(prop) { mutableSetOf() }
                        scopes += scopeIds
                    }
                }
                map.forEach { (k, v) ->
                    val s = when {
                        v.isEmpty() -> "{}"
                        v.contains("any") -> "{ any }"
                        else -> v.joinToString(" ", "{ ", " }")
                    }
                    append("    ").append("$k = $s").appendLine()
                }
                append("}").appendLine()
            }
        }
    }

    private fun getScopeIds(text: String): Set<String> {
        return when {
            text == "Base Scope" -> setOf("any")
            text == "Ship (and Starbase)" -> setOf("ship", "starbase")
            else -> setOf(text.lowercase().replace(" ", "_"))
        }
    }
}
