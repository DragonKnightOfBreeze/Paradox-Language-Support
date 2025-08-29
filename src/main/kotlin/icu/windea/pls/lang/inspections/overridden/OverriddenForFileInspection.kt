package icu.windea.pls.lang.inspections.overridden

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.quickfix.NavigateToFix
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.selector.file
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.lang.util.PlsVfsManager
import icu.windea.pls.model.ParadoxFileType
import icu.windea.pls.model.ParadoxRootInfo

/**
 * 检查是否存在对文件的重载
 */
class OverriddenForFileInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (PlsVfsManager.isLightFile(file.virtualFile)) return false //不检查临时文件
        if (selectRootFile(file) == null) return false
        if (!inProject(file)) return false //only for project files
        return true
    }

    private fun inProject(file: PsiFile): Boolean {
        val vFile = file.virtualFile ?: return false
        return ProjectFileIndex.getInstance(file.project).isInContent(vFile)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        val project = holder.project
        val fileInfo = file.fileInfo ?: return PsiElementVisitor.EMPTY_VISITOR
        if (!shouldCheckFile(file, fileInfo.fileType)) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                val selector = selector(project, file).file()
                val path = fileInfo.path.path
                val results = ParadoxFilePathSearch.search(path, null, selector).findAll().mapNotNull { it.toPsiFile(project) }
                if (results.size < 2) return //no override -> skip

                val locationElement = file
                val message = PlsBundle.message("inspection.overriddenForFile.desc", path)
                val fix = NavigateToOverriddenFilesFix(path, file, results)
                holder.registerProblem(locationElement, message, fix)
            }
        }
    }

    private fun shouldCheckFile(file: PsiFile, fileType: ParadoxFileType): Boolean {
        return ParadoxFileManager.canOverrideFile(file, fileType)
    }

    private class NavigateToOverriddenFilesFix(key: String, element: PsiElement, elements: Collection<PsiElement>) : NavigateToFix(key, element, elements) {
        override fun getText() = PlsBundle.message("inspection.overriddenForFile.fix.1")

        override fun getPopupTitle(editor: Editor) = PlsBundle.message("inspection.overriddenForFile.fix.1.popup.title", key)

        override fun getPopupText(editor: Editor, value: PsiElement): String {
            val file = value.containingFile
            val filePath = file.fileInfo?.rootInfo?.castOrNull<ParadoxRootInfo.MetadataBased>()?.rootFile?.path
            if (filePath == null) return PlsBundle.message("inspection.fix.navigate.popup.text.0", key)
            return PlsBundle.message("inspection.fix.navigate.popup.text.1", key, filePath)
        }
    }
}
