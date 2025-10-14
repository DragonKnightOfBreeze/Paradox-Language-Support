package icu.windea.pls.config.util.generators

import com.intellij.openapi.application.readAction
import com.intellij.psi.PsiElement
import icu.windea.pls.core.collections.toSetOrThis
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.splitByBlank
import icu.windea.pls.cwt.psi.CwtFile

object CwtConfigGeneratorUtil {
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
}
