package icu.windea.pls.lang.inspections.overridden

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.roots.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.lang.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import org.jetbrains.annotations.*

/**
 * 检查是否存在对文件的重载
 */
class OverriddenForFileInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        val project = holder.project
        if (!shouldCheckFile(file)) return PsiElementVisitor.EMPTY_VISITOR
        val fileInfo = file.fileInfo ?: return PsiElementVisitor.EMPTY_VISITOR
        if (!shouldCheckFile(file, fileInfo.fileType)) return PsiElementVisitor.EMPTY_VISITOR
        val virtualFile = file.virtualFile
        val inProject = virtualFile != null && ProjectFileIndex.getInstance(project).isInContent(virtualFile)
        if (!inProject) return PsiElementVisitor.EMPTY_VISITOR //only for project files

        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                ProgressManager.checkCanceled()
                val selector = selector(project, virtualFile).file()
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

    private fun shouldCheckFile(file: PsiFile): Boolean {
        if (PlsFileManager.isLightFile(file.virtualFile)) return false //不检查临时文件
        if (selectRootFile(file) == null) return false
        return true
    }

    private fun shouldCheckFile(file: PsiFile, fileType: ParadoxFileType): Boolean {
        return when (fileType) {
            ParadoxFileType.Script -> true
            ParadoxFileType.Localisation -> true
            ParadoxFileType.Csv -> true
            ParadoxFileType.ModDescriptor -> false
            ParadoxFileType.Other -> ParadoxImageManager.isImageFile(file) // currently only accept generic images
        }
    }

    private class NavigateToOverriddenFilesFix(key: String, element: PsiElement, elements: Collection<PsiElement>) : NavigateToFix(key, element, elements) {
        override fun getText() = PlsBundle.message("inspection.overriddenForFile.fix.1")

        override fun getPopupTitle(editor: Editor) = PlsBundle.message("inspection.overriddenForFile.fix.1.popup.title", key)

        override fun getPopupText(editor: Editor, value: PsiElement): @Nls String {
            val file = value.containingFile
            val filePath = file.fileInfo?.rootInfo?.castOrNull<ParadoxRootInfo.MetadataBased>()?.rootFile?.path
            if (filePath == null) return PlsBundle.message("inspection.fix.navigate.popup.text.0", key)
            return PlsBundle.message("inspection.fix.navigate.popup.text.1", key, filePath)
        }
    }
}
