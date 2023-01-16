package icu.windea.pls.tool.cwt

import icu.windea.pls.config.core.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import java.io.File

/**
 * 用于从`effects.log`生成`effects.cwt`。
 */
class CwtEffectConfigGenerator(
    val gameType: ParadoxGameType,
    val logPath: String,
    val cwtPath: String
) {
    data class EffectInfo(
        var name: String = "",
        var description: MutableList<String> = mutableListOf(),
        var declaration: MutableList<String> = mutableListOf(),
        var supportedScopes: Set<String> = emptySet()
    )
    
    enum class LinePosition {
        Name, Documentation, Declaration, Scopes
    }
    
    fun generate() {
        val infos = parseLog()
        generateCwt(infos)
    }
    
    private fun parseLog(): MutableMap<String, EffectInfo> {
        val infos = mutableMapOf<String, EffectInfo>()
        var isDocumentation = false
        var position = LinePosition.Name
        
        var info = EffectInfo()
        val logFile = File(logPath)
        val lines = logFile.bufferedReader().readLines()
        var lineIndex = 0
        while(lineIndex < lines.size) {
            val line = lines[lineIndex]
            if(line == "== EFFECT DOCUMENTATION ==") {
                isDocumentation = true
                lineIndex++
                continue
            }
            if(line == "=================") {
                isDocumentation = false
                lineIndex++
                continue
            }
            if(!isDocumentation) {
                lineIndex++
                continue
            }
            if(line.isBlank()) {
                lineIndex++
                continue
            }
    
            if(line.startsWith("Supported Scopes:")) {
                position = LinePosition.Scopes
            }
            when(position) {
                LinePosition.Name -> {
                    val list = line.split('-', limit = 2).map { it.trim() }
                    val (name, documentation) = list
                    if(info.name.isNotEmpty() && info.declaration.isNotEmpty()) infos.put(info.name, info)
                    info = EffectInfo()
                    info.name = name
                    info.description += documentation
                    position = LinePosition.Documentation
                }
                LinePosition.Documentation -> {
                    if(line.startsWith(info.name + " = ")) {
                        position = LinePosition.Declaration
                        continue
                    } else {
                        info.description += line.trim()
                    }
                }
                LinePosition.Declaration -> {
                    info.declaration += line
                }
                LinePosition.Scopes -> {
                    val scopes = line.removePrefix("Supported Scopes:").trim()
                    info.supportedScopes = scopes.splitByBlank().toSet()
                    position = LinePosition.Name
                }
            }
            lineIndex++
        }
        if(info.name.isNotEmpty()) infos.put(info.name, info)
        return infos
    }
    
    private fun generateCwt(infos: Map<String, EffectInfo>) {
        val missingNames = infos.keys.toMutableSet()
        val unknownNames = mutableSetOf<String>()
        val cwtFile = File(cwtPath)
        val lines = cwtFile.bufferedReader().readLines() as MutableList<String>
        var lineIndex = 0
        while(lineIndex < lines.size) {
            val line = lines[lineIndex]
            if(line.startsWith("alias")) {
                val name = line.substringBefore('=', "").trim().substringIn("alias[effect:", "]", "")
                missingNames.remove(name)
                val info = infos[name]
                if(info != null) {
                    setDocumentation(lineIndex, lines, info).let { lineIndex +=it }
                    setScopeOption(lineIndex, lines, info).let { lineIndex+=it }
                } else {
                    if(CwtKeyExpression.resolve(name).type == CwtDataType.Constant) {
                        unknownNames.add(name)
                    }
                }
            }
            lineIndex++
        }
        
        if(missingNames.isNotEmpty()) {
            println("Missing modifiers:")
            for(name in missingNames) {
                println("- $name")
            }
            for(name in missingNames) {
                val info = infos[name] ?: continue
                lines.add("")
                info.description.forEach { lines.add("# ### $it") }
                val scopesText = getScopesText(info)
                info.supportedScopes.let { lines.add("# ## $scopesText") }
                info.declaration.forEach { lines.add("# $it") }
            }
        }
        
        if(unknownNames.isNotEmpty()) {
            println("Unknown modifiers:")
            for(name in unknownNames) {
                println("- $name")
            }
        }
        
        cwtFile.writeText(lines.joinToString("\n"))
    }
    
    private fun setDocumentation(lineIndex: Int, lines: MutableList<String>, info: EffectInfo) : Int {
        var offset = 0
        var prevIndex = lineIndex - 1
        while(true) {
            if(!lines.getOrNull(prevIndex).orEmpty().startsWith('#')) break
            if(lines.getOrNull(prevIndex).orEmpty().startsWith("###")) {
                lines.removeAt(prevIndex)
                offset -= 1
            }
            prevIndex--
        }
        info.description.forEach { desc ->
            lines.add(lineIndex + offset, "### $desc")
            offset += 1
        }
        return offset
    }
    
    private fun setScopeOption(lineIndex: Int, lines: MutableList<String>, info: EffectInfo) : Int {
        var offset = 0
        var prevIndex = lineIndex - 1
        var scopeOptionIndex = -1
        while(true) {
            if(!lines.getOrNull(prevIndex).orEmpty().startsWith('#')) break
            if(lines.getOrNull(prevIndex)?.removePrefixOrNull("##").orEmpty().startsWith("scope")) {
                scopeOptionIndex = prevIndex
                break
            }
            prevIndex--
        }
        val scopesText = getScopesText(info)
        if(scopeOptionIndex != -1) {
            lines.set(scopeOptionIndex, "## $scopesText")
        } else {
            lines.add(lineIndex, "## $scopesText")
            offset += 1
        }
        return offset
    }
    
    private fun getScopesText(info: EffectInfo) = when {
        info.supportedScopes.singleOrNull().let { it == "any" || it == "all" } -> "## scopes = any"
        else -> "scopes = { ${info.supportedScopes.joinToString(" ")} }"
    }
}