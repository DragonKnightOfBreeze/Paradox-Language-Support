package icu.windea.pls.config.util.generators

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.documentation
import icu.windea.pls.config.util.generators.CwtConfigGenerator.Hint
import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.collections.chunkedBy
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.toFile
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.cwt.psi.CwtElementFactory
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 从 `triggers.log` 生成 `triggers.cwt`。
 *
 * @property ignoredNames 需要忽略的修正的名字（忽略大小写）。
 *
 * @see CwtAliasConfig
 */
class CwtTriggerConfigGenerator(override val project: Project) : CwtConfigGenerator {
    val ignoredNames = caseInsensitiveStringSet()

    init {
        configureDefaults()
    }

    private fun configureDefaults() {
        ignoredNames += setOf("and", "or", "not", "nand", "nor", "hidden_trigger")
    }

    override fun getName() = "TriggerConfigGenerator"

    override fun getGeneratedFileName() = "triggers.cwt"

    override suspend fun generate(gameType: ParadoxGameType, inputPath: String, outputPath: String): Hint {
        val infos = parseLogFile(inputPath)
        val configInfos = parseConfigFile(outputPath, gameType)
        return generateHint(outputPath, infos, configInfos)
    }

    private suspend fun parseLogFile(inputPath: String): Map<String, TriggerInfo> {
        val file = inputPath.toFile()
        val allLines = withContext(Dispatchers.IO) { file.readLines() }
        val startMarkerIndex = allLines.indexOf(START_MARKER)
        val endMarkerIndex = allLines.lastIndexOf(END_MARKER)
        val startIndex = if (startMarkerIndex == -1) 0 else startMarkerIndex + 1
        val endIndex = if (endMarkerIndex == -1) allLines.size else endMarkerIndex - 1
        val lines = allLines.subList(startIndex, endIndex)
        val chunks = lines.map { it.trim() }.chunkedBy { it.isEmpty() }.filter { it.isNotEmpty() }
        val infos = chunks.map { parseInfo(it) }
        return infos.associateBy { it.name }
    }

    private fun parseInfo(chunkLines: List<String>): TriggerInfo {
        val name = CwtConfigGeneratorUtil.parseName(chunkLines.first())
        val description = CwtConfigGeneratorUtil.parseDescription(chunkLines.first())
        val supportedScopes = if (chunkLines.size >= 2) CwtConfigGeneratorUtil.parseSupportedScopes(chunkLines.last()) else emptySet()
        val declaration = if (chunkLines.size >= 2) chunkLines.subList(1, chunkLines.size - 1).joinToString("\n") else ""
        return TriggerInfo(name, description, supportedScopes, declaration)
    }

    private suspend fun parseConfigFile(outputPath: String, gameType: ParadoxGameType): Map<String, TriggerConfigInfo> {
        val file = outputPath.toFile()
        val text = withContext(Dispatchers.IO) { file.readText() }
        val psiFile = readAction { CwtElementFactory.createDummyFile(project, text) }
        return readAction {
            val fileConfig = CwtFileConfig.resolve(psiFile, file.name, CwtConfigGroup(project, gameType))
            val configs = fileConfig.properties.mapNotNull { CwtAliasConfig.resolve(it) }
                .filter { it.name == "trigger" && it.subName.isIdentifier() }
            configs.groupBy { it.subName }.mapValues { (_, v) -> parseConfigInfo(v) }
        }
    }

    private fun parseConfigInfo(configs: List<CwtAliasConfig>): TriggerConfigInfo {
        val name = configs.first().subName
        val description = configs.firstNotNullOfOrNull { it.config.documentation }.orEmpty()
        val supportedScopes = configs.first().supportedScopes
        return TriggerConfigInfo(name, description, supportedScopes)
    }

    private suspend fun generateHint(outputPath: String, infos: Map<String, TriggerInfo>, configInfos: Map<String, TriggerConfigInfo>): Hint {
        val oldNames = configInfos.keys.filter { it !in ignoredNames }.toSet()
        val newNames = infos.keys.filter { it !in ignoredNames }.toSet()
        val missingNames = newNames - oldNames
        val unknownNames = oldNames - newNames

        val summary = buildString {
            if (missingNames.isNotEmpty()) {
                appendLine("${missingNames.size} missing triggers.")
            }
            if (unknownNames.isNotEmpty()) {
                appendLine("${unknownNames.size} unknown triggers.")
            }
            if (isEmpty()) {
                appendLine("No missing or unknown triggers.")
            }
        }.trimEnd()
        val details = buildString {
            if (missingNames.isNotEmpty()) {
                appendLine("Missing triggers:")
                missingNames.forEach { appendLine("- $it") }
            }
            if (unknownNames.isNotEmpty()) {
                appendLine("Unknown triggers:")
                unknownNames.forEach { appendLine("- $it") }
            }
        }.trimEnd()
        val fileText = buildString {
            val file = outputPath.toFile()
            val text = withContext(Dispatchers.IO) { file.readText() }
            val psiFile = readAction { CwtElementFactory.createDummyFile(project, text) }
            val elementsToDelete = readAction { CwtConfigGeneratorUtil.getElementsToDelete(psiFile) { toDelete(it, unknownNames) } }
            val modifiedText = CwtConfigGeneratorUtil.getFileText(psiFile, elementsToDelete)
            appendLine(modifiedText)
            appendLine()
            appendLine(NOTE_UNKNOWN_TRIGGERS)
            appendLine()
            appendLine(TODO_MISSING_TRIGGERS)
            for (name in missingNames) {
                val info = infos[name] ?: continue
                appendLine()
                appendLine("### ${info.description}")
                appendLine("## ${CwtConfigGeneratorUtil.getScopesOptionText(info.supportedScopes)}")
                append("alias[trigger:${name}] = ")
                if (info.declaration.isEmpty()) {
                    appendLine("scalar # TODO")
                } else {
                    appendLine("{} # TODO")
                    info.declaration.lines().forEach { appendLine("# $it") }
                }
            }
        }.trimEnd()

        val hint = Hint(summary, details, fileText)
        hint.putUserData(Keys.missingNames, missingNames)
        hint.putUserData(Keys.unknownNames, unknownNames)
        hint.putUserData(Keys.triggerInfos, infos)
        hint.putUserData(Keys.triggerConfigInfos, configInfos)
        return hint
    }

    private fun toDelete(member: CwtMember, unknownNames: Set<String>): Boolean {
        return member is CwtProperty && member.name.removeSurroundingOrNull("alias[trigger:", "]") in unknownNames
    }

    data class TriggerInfo(
        val name: String,
        val description: String,
        val supportedScopes: Set<String>,
        val declaration: String,
    )

    data class TriggerConfigInfo(
        val name: String,
        val description: String,
        val supportedScopes: Set<String>
    )

    object Keys : KeyRegistry() {
        val missingNames by createKey<Set<String>>(Keys)
        val unknownNames by createKey<Set<String>>(Keys)
        val triggerInfos by createKey<Map<String, TriggerInfo>>(Keys)
        val triggerConfigInfos by createKey<Map<String, TriggerConfigInfo>>(Keys)
    }

    companion object {
        private const val START_MARKER = "== TRIGGER DOCUMENTATION =="
        private const val END_MARKER = "================="
        private const val NOTE_UNKNOWN_TRIGGERS = "# NOTE unknown effects are deleted"
        private const val TODO_MISSING_TRIGGERS = "# TODO missing triggers"
    }
}
