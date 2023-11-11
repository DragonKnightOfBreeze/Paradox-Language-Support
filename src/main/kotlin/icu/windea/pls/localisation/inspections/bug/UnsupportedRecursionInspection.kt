package icu.windea.pls.localisation.inspections.bug

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*

/**
 * （对于本地化文件）检查是否存在不支持的递归。
 * * 对于每个本地化，检查其本地化文本中是否存在递归的本地化引用。
 */
class UnsupportedRecursionInspection : LocalInspectionTool() {
    //目前仅做检查即可，不需要显示递归的装订线图标
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if(!isFileToInspect(holder.file)) return PsiElementVisitor.EMPTY_VISITOR
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                when(element) {
                    is ParadoxLocalisationProperty -> visitLocalisationProperty(element)
                }
            }
            
            private fun visitLocalisationProperty(element: ParadoxLocalisationProperty) {
                val name = element.name
                if(name.isEmpty()) return
                
                val recursions = mutableSetOf<PsiElement>()
                ParadoxRecursionHandler.isRecursiveLocalisation(element, recursions)
                if(recursions.isEmpty()) return
                val message = PlsBundle.message("inspection.localisation.bug.unsupportedRecursion.description.1")
                val location = element.propertyKey
                holder.registerProblem(location, message, NavigateToRecursionFix(name, element, recursions))
            }
        }
    }
    
    private fun isFileToInspect(file: PsiFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        val filePath = fileInfo.pathToEntry
        return filePath.canBeLocalisationPath()
    }
    
    private class NavigateToRecursionFix(key: String, target: PsiElement, recursions: Collection<PsiElement>) : NavigateToFix(key, target, recursions) {
        override fun getText() = PlsBundle.message("inspection.localisation.bug.unsupportedRecursion.quickFix.1")
        
        override fun getPopupTitle(editor: Editor) =
            PlsBundle.message("inspection.localisation.bug.unsupportedRecursion.quickFix.1.popup.title", key)
        
        override fun getPopupText(editor: Editor, value: PsiElement) =
            PlsBundle.message("inspection.localisation.bug.unsupportedRecursion.quickFix.1.popup.text", key, editor.document.getLineNumber(value.textOffset))
    }
}
