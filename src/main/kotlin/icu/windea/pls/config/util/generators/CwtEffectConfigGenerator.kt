package icu.windea.pls.config.util.generators

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import icu.windea.pls.config.CwtApiStatus
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.configGroup.CwtConfigGroupImpl
import icu.windea.pls.config.documentation
import icu.windea.pls.config.util.generators.CwtConfigGenerator.*
import icu.windea.pls.core.collections.caseInsensitiveStringSet
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.toFile
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.registerKey
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
 * 从 `effects.log` 生成 `effects.cwt`。
 *
 * @property ignoredNames 需要忽略的效果的名字（忽略大小写）。
 *
 * @see CwtAliasConfig
 */
class CwtEffectConfigGenerator(override val project: Project) : CwtConfigGenerator {
    val ignoredNames = caseInsensitiveStringSet()

    init {
        configureDefaults()
    }

    private fun configureDefaults() {
        ignoredNames += setOf("if", "else_if", "else", "switch", "inverted_switch", "while", "hidden_effect", "tooltip")
    }

    override fun getName() = "EffectConfigGenerator"

    override fun getDefaultInputName() = "effects.log"

    override fun getDefaultOutputName() = "effects.cwt"

    override suspend fun generate(gameType: ParadoxGameType, inputPath: String, outputPath: String): Hint {
        val infos = parseLogFile(inputPath)
        val configInfos = parseConfigFile(outputPath, gameType)
        return generateHint(outputPath, infos, configInfos)
    }

    private suspend fun parseLogFile(inputPath: String): Map<String, EffectInfo> {
        val file = inputPath.toFile()
        val lines = withContext(Dispatchers.IO) { file.readLines() }
        val chunks = CwtConfigGeneratorUtil.splitChunks(lines, SPLIT_CHUNKS_PREDICATE)
        val infos = chunks.mapNotNull { parseInfo(it) }
        return infos.associateBy { it.name }
    }

    private fun parseInfo(chunkLines: List<String>): EffectInfo? {
        // first line - doc line - include name (preferred) and docs
        val docLine = chunkLines.first()
        val name = CwtConfigGeneratorUtil.parseName(docLine) ?: return null // null -> skipped
        val description = CwtConfigGeneratorUtil.parseDescription(docLine)

        // lines drop and only drop first line - declaration lines - include declaration and scope infos
        val declarationLines = chunkLines.drop(1)
        val declaration = declarationLines.joinToString().trim()
        val supportedScopes = CwtConfigGeneratorUtil.parseValues(declarationLines, SUPPORTED_SCOPES_PREFIX)
        val supportedTargets = CwtConfigGeneratorUtil.parseValues(declarationLines, SUPPORTED_TARGETS_PREFIX)

        return EffectInfo(name, description, declaration, supportedScopes, supportedTargets)
    }

    private suspend fun parseConfigFile(outputPath: String, gameType: ParadoxGameType): Map<String, EffectConfigInfo> {
        val file = outputPath.toFile()
        if (!file.exists()) return emptyMap() // file not exist -> return empty
        val text = withContext(Dispatchers.IO) { file.readText() }
        val psiFile = readAction { CwtElementFactory.createDummyFile(project, text) }
        return readAction {
            val fileConfig = CwtFileConfig.resolve(psiFile, CwtConfigGroupImpl(project, gameType), file.name)
            val configs = fileConfig.properties.mapNotNull { CwtAliasConfig.resolve(it) }
                .filter { it.name == "effect" && it.subName.isIdentifier() }
            configs.groupBy { it.subName }.mapValues { (_, v) -> parseConfigInfo(v) }
        }
    }

    private fun parseConfigInfo(configs: List<CwtAliasConfig>): EffectConfigInfo {
        val name = configs.first().subName
        val description = configs.firstNotNullOfOrNull { it.config.documentation }.orEmpty()
        val supportedScopes = configs.first().supportedScopes
        val apiStatus = configs.firstNotNullOfOrNull { it.config.optionData { apiStatus } }
        return EffectConfigInfo(name, description, supportedScopes, apiStatus)
    }

    private suspend fun generateHint(outputPath: String, infos: Map<String, EffectInfo>, configInfos: Map<String, EffectConfigInfo>): Hint {
        val keptNames = configInfos.filterValues { it.apiStatus == CwtApiStatus.Kept }.keys
        val oldNames = configInfos.keys.filter { it !in ignoredNames && it !in keptNames }.toSet()
        val newNames = infos.keys.filter { it !in ignoredNames && it !in keptNames }.toSet()
        val missingNames = newNames - oldNames
        val unknownNames = oldNames - newNames

        val summary = buildString {
            if (missingNames.isNotEmpty()) {
                appendLine("${missingNames.size} missing effects.")
            }
            if (unknownNames.isNotEmpty()) {
                appendLine("${unknownNames.size} unknown effects.")
            }
            if (isEmpty()) {
                appendLine("No missing or unknown effects.")
            }
        }.trimEnd()
        val details = buildString {
            if (missingNames.isNotEmpty()) {
                appendLine("Missing effects:")
                missingNames.forEach { appendLine("- $it") }
            }
            if (unknownNames.isNotEmpty()) {
                appendLine("Unknown effects:")
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
            if (unknownNames.isNotEmpty()) {
                appendLine()
                appendLine(NOTE_UNKNOWN_EFFECTS)
            }
            if (missingNames.isNotEmpty()) {
                appendLine()
                appendLine(TODO_MISSING_EFFECTS)
            }
            for (name in missingNames) {
                val info = infos[name] ?: continue
                appendLine()
                appendLine("### ${info.description}")
                appendLine("## ${CwtConfigGeneratorUtil.getScopesOptionText(info.supportedScopes)}")
                append("alias[effect:${name}] = ")
                if (info.declaration.isEmpty()) {
                    appendLine("scalar # TODO")
                } else {
                    appendLine("{} # TODO")
                    info.declaration.lines().forEach { appendLine("# $it") }
                }
            }
        }.trimEnd() + "\n" // ensure ends with a line break

        val hint = Hint(summary, details, fileText)
        hint.putUserData(Keys.missingNames, missingNames)
        hint.putUserData(Keys.unknownNames, unknownNames)
        hint.putUserData(Keys.infos, infos)
        hint.putUserData(Keys.configInfos, configInfos)
        return hint
    }

    private fun toDelete(member: CwtMember, unknownNames: Set<String>): Boolean {
        return member is CwtProperty && member.name.removeSurroundingOrNull("alias[effect:", "]") in unknownNames
    }

    data class EffectInfo(
        val name: String,
        val description: String,
        val declaration: String,
        val supportedScopes: Set<String>,
        val supportedTargets: Set<String>,
    )

    data class EffectConfigInfo(
        val name: String,
        val description: String,
        val supportedScopes: Set<String>,
        val apiStatus: CwtApiStatus?,
    )

    object Keys : KeyRegistry() {
        val missingNames by registerKey<Set<String>>(Keys)
        val unknownNames by registerKey<Set<String>>(Keys)
        val infos by registerKey<Map<String, EffectInfo>>(Keys)
        val configInfos by registerKey<Map<String, EffectConfigInfo>>(Keys)
    }

    companion object {
        private val SPLIT_CHUNKS_PREDICATE: (String) -> Boolean = { it.isEmpty() || it.all { c -> c.isWhitespace() || c == '-' || c == '=' } }
        private const val SUPPORTED_SCOPES_PREFIX = "Supported Scopes:"
        private const val SUPPORTED_TARGETS_PREFIX = "Supported Targets:"

        private const val NOTE_UNKNOWN_EFFECTS = "# NOTE unknown effects are deleted"
        private const val TODO_MISSING_EFFECTS = "# TODO missing effects"
    }
}
