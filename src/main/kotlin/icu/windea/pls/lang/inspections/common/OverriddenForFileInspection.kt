package icu.windea.pls.lang.inspections.common

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.roots.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*

/**
 * 检查是否存在对文件的重载
 */
class OverriddenForFileInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        val project = holder.project
        val fileInfo = file.fileInfo ?: return PsiElementVisitor.EMPTY_VISITOR
        val virtualFile = file.virtualFile
        val inProject = virtualFile != null && ProjectFileIndex.getInstance(project).isInContent(virtualFile)
        if(!inProject) return PsiElementVisitor.EMPTY_VISITOR //only for project files
        
        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                ProgressManager.checkCanceled()
                val selector = fileSelector(project, virtualFile)
                val path = fileInfo.path.path
                val results = ParadoxFilePathSearch.search(path, null, selector).findAll().mapNotNull { it.toPsiFile(project) }
                if(results.size < 2) return //no override -> skip
                
                val locationElement = file
                val message = PlsBundle.message("inspection.overriddenForFile.desc", path)
                val fix = NavigateToOverriddenFilesFix(path, file, results)
                holder.registerProblem(locationElement, message, fix)
            }
        }
    }
    
    private class NavigateToOverriddenFilesFix(key: String, element: PsiElement, elements: Collection<PsiElement>) : NavigateToFix(key, element, elements) {
        override fun getText() = PlsBundle.message("inspection.overriddenForFile.fix.1")
        
        override fun getPopupTitle(editor: Editor) =
            PlsBundle.message("inspection.overriddenForFile.fix.1.popup.title", key)
        
        override fun getPopupText(editor: Editor, value: PsiElement) =
            PlsBundle.message("inspection.overriddenForFile.fix.1.popup.text", key, value.containingFile?.fileInfo?.rootInfo?.rootFile?.path.orAnonymous())
    }
}
