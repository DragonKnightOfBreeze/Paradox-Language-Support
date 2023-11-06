package icu.windea.pls.dev.cwt

import icu.windea.pls.core.*
import icu.windea.pls.model.*
import java.io.*

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
        for(line in allLines) {
            val l = line.trim()
            if(l.surroundsWith("--", "--")) {
                if(info.name.isNotEmpty()) {
                    infos.add(info)
                    info = LocalisationInfo()
                }
                info.name = l.removeSurrounding("--", "--")
                position = Position.ScopeName
                continue
            }
            if(l == "Promotions:") {
                position = Position.Promotions
                continue
            }
            if(l == "Properties") {
                position = Position.Properties
                continue
            }
            when(position) {
                Position.Promotions -> {
                    val v = l.takeIf { it.isNotEmpty() && it.all { c -> c != '=' } }
                    if(v != null) info.promotions.add(v)
                }
                Position.Properties -> {
                    val v = l.takeIf { it.isNotEmpty() && it.all { c -> c != '=' } }
                    if(v != null) info.properties.add(v)
                }
                else -> {}
            }
        }
        if(info.name.isNotEmpty()) {
            infos.add(info)
        }
        val newLocLines = getLocLines(infos)
        val cwtFile = File(cwtPath)
        val allCwtLines = cwtFile.bufferedReader().readLines()
        val newLines = mutableListOf<String>()
        var flag = false
        for(line in allCwtLines) {
            if(line == "localisation_commands = {") {
                flag = true
                newLines.addAll(newLocLines)
                continue
            }
            if(flag && line == "}") {
                flag = false
                continue
            }
            if(flag) continue
            newLines.add(line)
        }
        cwtFile.writeText(newLines.joinToString("\n"))
    }
    
    private fun getLocLines(infos: List<LocalisationInfo>): List<String> {
        val map = mutableMapOf<String, MutableSet<String>>()
        infos.forEach { info ->
            info.properties.forEach { prop ->
                val set = map.getOrPut(prop) { mutableSetOf() }
                val scope = info.name
                when {
                    scope == "Base Scope" -> {
                        set.add("any")
                    }
                    scope == "Ship (and Starbase)" -> {
                        set.add("ship")
                        set.add("starbase")
                    }
                    else -> {
                        set.add(scope.lowercase())
                    }
                }
            }
        }
        val result = mutableListOf<String>()
        result.add("localisation_commands = {")
        map.forEach { (k, v) ->
            val vs = when {
                v.isEmpty() -> "{}"
                v.contains("any") -> "{ any }"
                else -> v.joinToString(" ", "{ ", " }")
            }
            result.add("    $k = $vs")
        }
        result.add("}")
        return result
    }
}