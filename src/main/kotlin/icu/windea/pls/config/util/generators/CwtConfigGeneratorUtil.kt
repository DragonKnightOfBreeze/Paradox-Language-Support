package icu.windea.pls.config.util.generators

import com.intellij.openapi.application.readAction
import com.intellij.psi.PsiElement
import com.intellij.psi.util.siblings
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.children
import icu.windea.pls.core.collections.chunkedBy
import icu.windea.pls.core.collections.filterIsInstance
import icu.windea.pls.core.formatted
import icu.windea.pls.core.toCommaDelimitedStringSet
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.model.ParadoxGameType
import java.nio.file.Path
import kotlin.io.path.exists

object CwtConfigGeneratorUtil {
    fun getPathInGameDirectory(path: String, gameType: ParadoxGameType): Path? {
        val absPath = path.toPathOrNull()?.takeIf { it.isAbsolute }
        if (absPath != null) return absPath
        val resultPath = ParadoxFileManager.getPathInGameDirectory(path, gameType)
        return resultPath?.takeIf { it.exists() }
    }

    fun getQuickInputPath(gameType: ParadoxGameType, generator: CwtConfigGenerator): Path? {
        val resultPath = if (generator.fromScripts) {
            val path = generator.getDefaultInputName()
            ParadoxFileManager.getPathInGameDirectory(path, gameType)
        } else {
            // TODO 2.0.6+ 需要确定对于群星以外的游戏，这里的相对路径是否固定是 `logs/script_documentation`
            val fileName = generator.getDefaultInputName()
            val gameDataPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title)
            gameDataPath?.resolve("logs/script_documentation")?.resolve(fileName)?.formatted()
        }
        return resultPath?.takeIf { it.exists() }
    }

    fun splitChunks(lines: List<String>, predicate: (String) -> Boolean): List<List<String>> {
        return lines.map { it.trimEnd() }.chunkedBy(false, predicate)
    }

    fun parseName(line: String): String? {
        return line.substringBefore('-', "").trim().takeIf { it.isNotEmpty() && it.isIdentifier() }
    }

    fun parseDescription(line: String): String {
        return line.substringAfter('-', "").trim()
    }

    fun parseValue(lines: List<String>, prefix: String): String? {
        val line = lines.find { it.startsWith(prefix) } ?: return null
        return line.drop(prefix.length).trim()
    }

    fun parseValues(lines: List<String>, prefix: String): Set<String> {
        val line = lines.find { it.startsWith(prefix) } ?: return emptySet()
        return line.drop(prefix.length).trim().toCommaDelimitedStringSet()
    }

    fun getScopesOptionText(supportedScopes: Set<String>): String {
        val text = when {
            supportedScopes.isEmpty() -> "any"
            "any" in supportedScopes || "all" in supportedScopes -> "any"
            else -> supportedScopes.joinToString(" ", "{ ", " }")
        }
        return "scopes = $text"
    }

    fun getElementsToDelete(
        file: CwtFile,
        predicate: (CwtMember) -> Boolean,
    ): List<PsiElement> {
        val members = file.block?.children()?.filterIsInstance<CwtMember>() ?: return emptyList()
        val membersToDelete = members.filterIsInstance<CwtMember> { predicate(it) }
        return getElementsToDelete(membersToDelete)
    }

    fun getElementsToDelete(
        file: CwtFile,
        containerPropertyName: String,
        predicate: (CwtMember) -> Boolean,
    ): List<PsiElement> {
        val container = file.block?.children()?.filterIsInstance<CwtProperty>()?.find { it.name == containerPropertyName } ?: return emptyList()
        val members = container.propertyValue?.children()?.filterIsInstance<CwtMember>() ?: return emptyList()
        val membersToDelete = members.filterIsInstance<CwtMember> { predicate(it) }
        return getElementsToDelete(membersToDelete)
    }

    fun getElementsToDelete(membersToDelete: Sequence<CwtMember>): List<PsiElement> {
        val result = mutableListOf<PsiElement>()
        membersToDelete.forEach { p ->
            result += p
            p.siblings(forward = false, withSelf = false).takeWhile { e -> e !is CwtProperty }.forEach { e -> result += e }
        }
        return result
    }

    suspend fun getFileText(file: CwtFile, elementsToDelete: List<PsiElement>): String {
        // NOTE 如果直接修改 PSI 的话，无法避免使用 writeCommandAction，因此这里改为直接按文本范围处理文件文本
        // return getFileTextWithPsiModification(file, elementsToDelete)
        return getFileTextWithRangeDelete(file, elementsToDelete)
    }

    // @Suppress("UnstableApiUsage")
    // private suspend fun getFileTextWithPsiModification(file: CwtFile, elementsToDelete: List<PsiElement>): String {
    //     writeCommandAction(file.project, "CwtTriggerConfigGenerator") { elementsToDelete.forEach { it.delete() } }
    //     val finalText = getFileText(file)
    //     return finalText
    // }

    private suspend fun getFileTextWithRangeDelete(file: CwtFile, elementsToDelete: List<PsiElement>): String {
        val text = readAction { file.text.trimEnd() }
        if (elementsToDelete.isEmpty()) return text
        val textRangesToDelete = readAction { elementsToDelete.map { it.textRange } }
        val finalText = textRangesToDelete
            .sortedByDescending { it.startOffset }
            .fold(text) { a, b -> b.replace(a, "") }
        return finalText
    }

    suspend fun insertIntoContainer(
        file: CwtFile,
        containerPropertyName: String,
        insertBlock: String,
    ): String {
        val container = readAction {
            val rootProps = file.block?.children()?.filterIsInstance<CwtProperty>()?.toList().orEmpty()
            rootProps.firstOrNull { it.name == containerPropertyName }
        }
        val fileText = readAction { file.text }
        if (container != null) {
            val insertionOffset = readAction {
                val containerText = container.text
                val relIndex = containerText.lastIndexOf('}')
                val rel = if (relIndex == -1) containerText.length else relIndex
                container.textRange.startOffset + rel
            }
            return buildString(fileText.length + insertBlock.length + 64) {
                appendLine(fileText.substring(0, insertionOffset))
                appendLine(insertBlock.prependIndent())
                append(fileText.substring(insertionOffset))
            }
        }
        // fallback：容器缺失，直接在文件末尾追加完整容器
        val containerPatch = buildString {
            appendLine("${containerPropertyName} = {")
            appendLine(insertBlock.prependIndent())
            append("}")
        }
        return buildString(fileText.length + containerPatch.length + 64) {
            append(fileText.trimEnd())
            appendLine()
            append(containerPatch)
        }
    }
}
