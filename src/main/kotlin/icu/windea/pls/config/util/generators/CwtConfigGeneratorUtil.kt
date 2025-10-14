package icu.windea.pls.config.util.generators

import com.intellij.openapi.application.readAction
import com.intellij.psi.PsiElement
import com.intellij.psi.util.siblings
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.children
import icu.windea.pls.core.collections.filterIsInstance
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.splitByBlank
import icu.windea.pls.core.toFile
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.ParadoxGameType
import java.io.File

object CwtConfigGeneratorUtil {
    fun getFileInGameDirectory(path: String, gameType: ParadoxGameType): File? {
        if (path.startsWith('/')) return path.toFile()
        val gamePath = PlsFacade.getDataProvider().getSteamGamePath(gameType.id, gameType.title) ?: return null
        return gamePath.resolve(path).toFile()
    }

    fun parseName(line: String): String {
        return line.substringBefore('-').trimEnd()
    }

    fun parseDescription(line: String): String {
        return line.substringAfter('-').trimStart()
    }

    fun parseSupportedScopes(line: String): Set<String> {
        val text = line.removePrefixOrNull("Supported Scopes:")?.trimStart()?.orNull() ?: return emptySet()
        return text.splitByBlank().toSet()
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
