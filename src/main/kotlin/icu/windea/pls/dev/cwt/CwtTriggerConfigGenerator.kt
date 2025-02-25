package icu.windea.pls.dev.cwt

import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import java.io.*

/**
 * 用于从`triggers.log`生成`triggers.cwt`。
 */
class CwtTriggerConfigGenerator(
    val gameType: ParadoxGameType,
    val logPath: String,
    val cwtPath: String,
) {
    var overrideDocumentation = true
    var generateMissing = true
    val ignoredNames = mutableSetOf<String>()

    companion object {
        private const val startMarker = "== TRIGGER DOCUMENTATION =="
        private const val endMarker = "================="
        private val optionNames = listOf("scope", "scopes", "push_scope", "severity")
    }

    data class TriggerInfo(
        var name: String = "",
        val description: MutableList<String> = mutableListOf(),
        val declaration: MutableList<String> = mutableListOf(),
        val supportedScopes: MutableSet<String> = mutableSetOf()
    )

    fun generate() {
        val infos = parseLog()
        generateCwt(infos)
    }

    private fun parseLog(): Map<String, TriggerInfo> {
        val infos = mutableMapOf<String, TriggerInfo>()
        val logFile = File(logPath)
        val allLines = logFile.bufferedReader().readLines()
        val startIndex = allLines.indexOf(startMarker)
        val endIndex = allLines.lastIndexOf(endMarker)
        val lines = allLines.subList(startIndex + 1, endIndex - 1)
        var isName = true
        var isDeclaration = false
        lateinit var info: TriggerInfo
        for (line in lines) {
            if (line.isBlank()) continue
            if (isName) {
                isName = false
                val list = line.split('-', limit = 2)
                if (list.size < 2) throw IllegalStateException()
                val (name, desc) = list
                info = TriggerInfo()
                info.name = name.trim()
                info.description += desc.trim()
                infos.put(info.name, info)
            } else {
                val scopes = line.removePrefixOrNull("Supported Scopes: ")
                if (scopes != null) {
                    isName = true
                    isDeclaration = false
                    info.supportedScopes += scopes.splitByBlank()
                    continue
                }
                if (!isDeclaration) {
                    if (line.startsWith(info.name + " ")) {
                        isDeclaration = true
                        info.declaration += line
                    } else {
                        info.description += line.trim()
                    }
                } else {
                    info.declaration += line
                }
            }
        }
        return infos
    }

    private fun generateCwt(infos: Map<String, TriggerInfo>) {
        val missingNames = infos.keys.toMutableSet()
        missingNames.removeAll(ignoredNames)
        val unknownNames = mutableSetOf<String>()
        val cwtFile = File(cwtPath)
        val lines = cwtFile.bufferedReader().readLines() as MutableList<String>
        var lineIndex = 0
        while (lineIndex < lines.size) {
            val line = lines[lineIndex]
            if (line.startsWith("alias")) {
                val name = line.substringBefore('=', "").trim().substringIn("alias[trigger:", "]", "")
                missingNames.remove(name)
                val info = infos[name]
                if (info != null) {
                    setDocumentation(lineIndex, lines, info).let { lineIndex += it }
                    setScopeOption(lineIndex, lines, info).let { lineIndex += it }
                    resortOptions(lineIndex, lines).let { lineIndex += it }
                } else {
                    if (name.isIdentifier()) {
                        unknownNames.add(name)
                    }
                }
            }
            lineIndex++
        }

        if (missingNames.isNotEmpty()) {
            println("Missing triggers:")
            for (name in missingNames) {
                println("- $name")
            }
            if (generateMissing) {
                lines.add("")
                lines.add("# TODO missing triggers")
                for (name in missingNames) {
                    val info = infos[name] ?: continue
                    lines.add("")
                    info.description.forEach { lines.add("### $it") }
                    val scopesText = getScopesText(info)
                    info.supportedScopes.let { lines.add("## $scopesText") }
                    info.name.let { lines.add("alias[trigger:$it] = {") }
                    info.declaration.forEach { lines.add("# $it") }
                    lines.add("}")
                }
            }
        }

        if (unknownNames.isNotEmpty()) {
            println("Unknown triggers:")
            for (name in unknownNames) {
                println("- $name")
            }
        }

        cwtFile.writeText(lines.joinToString("\n"))
    }

    private fun setDocumentation(lineIndex: Int, lines: MutableList<String>, info: TriggerInfo): Int {
        var offset = 0
        var delta = 0
        var prevIndex = lineIndex - 1
        while (true) {
            val prevLine = lines.getOrNull(prevIndex) ?: break
            if (!prevLine.startsWith('#')) break
            if (prevLine.startsWith("##")) {
                offset -= 1
            }
            if (prevLine.startsWith("###")) {
                if (!overrideDocumentation) return 0
                lines.removeAt(prevIndex)
                delta -= 1
            }
            prevIndex--
        }
        info.description.forEach { desc ->
            lines.add(lineIndex + offset, "### $desc")
            offset += 1
            delta += 1
        }
        return delta
    }

    private fun setScopeOption(lineIndex: Int, lines: MutableList<String>, info: TriggerInfo): Int {
        var offset = 0
        var delta = 0
        var prevIndex = lineIndex - 1
        while (true) {
            val prevLine = lines.getOrNull(prevIndex) ?: break
            if (!prevLine.startsWith('#')) break
            if (prevLine.startsWith("##") && !prevLine.startsWith("###")) {
                val optionDocText = prevLine.removePrefix("##").trim()
                if (optionDocText.startsWith("scope")) {
                    lines.removeAt(prevIndex)
                    delta -= 1
                    offset -= 1
                }
            }
            prevIndex--
        }
        val scopesText = getScopesText(info)
        lines.add(lineIndex + offset, "## $scopesText")
        delta += 1
        return delta
    }

    private fun resortOptions(lineIndex: Int, lines: MutableList<String>): Int {
        var offset = 0
        val optionTextList = mutableListOf<String>()
        var prevIndex = lineIndex - 1
        while (true) {
            val prevLine = lines.getOrNull(prevIndex) ?: break
            if (!prevLine.startsWith('#')) break
            if (prevLine.startsWith("##") && !prevLine.startsWith("###")) {
                val optionText = prevLine.removePrefix("##").trim()
                optionTextList.add(optionText)
                lines.removeAt(prevIndex)
                offset -= 1
            }
            prevIndex--
        }
        optionTextList.sortBy { it.substringBefore('=').trim().let { optionName -> optionNames.indexOf(optionName) } }
        optionTextList.forEach { optionText ->
            lines.add(lineIndex + offset, "## $optionText")
            offset += 1
        }
        return 0
    }

    private fun getScopesText(info: TriggerInfo) = when {
        info.supportedScopes.singleOrNull().let { it == "any" || it == "all" } -> "scopes = any"
        else -> "scopes = { ${info.supportedScopes.joinToString(" ")} }"
    }
}
