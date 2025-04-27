package icu.windea.pls.integration.tiger

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import icu.windea.pls.integration.tiger.TigerChecker
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange

/**
 * 使用 tiger 工具检查 Paradox Script 和 Localisation 文件的 Inspection。
 */
class ParadoxTigerInspection : LocalInspectionTool() {
    override fun getDisplayName() = "Tiger Mod Validator"
    override fun getShortName() = "ParadoxTigerInspection"
    override fun isEnabledByDefault() = true

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                val project = file.project
                val virtualFile = file.virtualFile ?: return
                val modRoot = findModRoot(virtualFile, project) ?: return
                val tigerPath = getTigerExecutablePath(project) ?: return
                val results = TigerChecker.check(modRoot.path, tigerPath)
                // 针对当前文件的结果
                val fileResults = results.filter {
                    it.locations?.any { loc -> loc.fullpath?.replace("/", "\\")?.endsWith(virtualFile.path.replace("/", "\\")) == true } == true
                }
                for (result in fileResults) {
                    result.locations?.forEach { loc ->
                        val lineNr = (loc.linenr ?: 1) - 1
                        val column = (loc.column ?: 1) - 1
                        val length = loc.length ?: 1
                        val doc = file.viewProvider.document ?: return@forEach
                        val startOffset = try { doc.getLineStartOffset(lineNr) + column } catch (_: Exception) { 0 }
                        val endOffset = (startOffset + length).coerceAtMost(doc.textLength)
                        val range = TextRange(startOffset, endOffset)
                        holder.registerProblem(
                            file,
                            range,
                            result.message ?: "Tiger validator issue",
                            when(result.severity) {
                                "error" -> ProblemHighlightType.ERROR
                                "untidy" -> ProblemHighlightType.WEAK_WARNING
                                else -> ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                            }
                        )
                    }
                }
            }
        }
    }

    private fun findModRoot(file: VirtualFile, project: Project): VirtualFile? {
        // 简单假设：模组根目录含有 descriptor.mod 文件
        var dir = if(file.isDirectory) file else file.parent
        while(dir != null) {
            if(dir.findChild("descriptor.mod") != null) return dir
            dir = dir.parent
        }
        return null
    }

    private fun getTigerExecutablePath(project: Project): String? {
        // TODO: 可改为设置项或自动检测
        // 默认尝试 PATH 或常见位置
        return "vic3-tiger.exe" // 或完整路径
    }
}
