package icu.windea.pls.localisation.inspections.bug

import com.intellij.codeInsight.intention.preview.*
import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.psi.*
import java.util.*

/**
 * （对于本地化文件）检查是否存在不支持的递归。例如，递归使用本地化引用。
 */
class UnsupportedRecursionInspection : LocalInspectionTool() {
    //目前仅做检查即可，不需要显示递归的装订线图标
    //在本地化级别进行此项检查
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if(!isFileToInspect(holder.file)) return PsiElementVisitor.EMPTY_VISITOR
        
        val guardStack = LinkedList<String>()
        
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxLocalisationProperty) {
                    withMeasureMillis({ element.fileInfo?.path.toString() + "." + element.name }) {
                        visitLocalisation(element, element.name)
                    }
                }
            }
            
            private fun visitLocalisation(element: ParadoxLocalisationProperty, name: String) {
                guardStack.clear()
                guardStack.add(name)
                try {
                    doRecursiveVisit(element)
                } catch(e: RecursionException) {
                    if(e.resolvedName == name) {
                        registerProblem(element, e.recursion)
                    }
                }
            }
            
            private fun doRecursiveVisit(element: ParadoxLocalisationProperty) {
                ProgressManager.checkCanceled()
                
                val resolvedMap = mutableSetOf<String>()
                element.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                    override fun visitElement(e: PsiElement) {
                        if(e is ParadoxLocalisationPropertyReference) visitPropertyReference(e)
                        if(e.isRichTextContext()) super.visitElement(e)
                    }
                    
                    private fun visitPropertyReference(e: ParadoxLocalisationPropertyReference) {
                        //对于相同的语言区域
                        ProgressManager.checkCanceled()
                        if(resolvedMap.contains(e.name)) return //不需要重复解析引用
                        val resolved = e.reference?.resolve()
                        if(resolved !is ParadoxLocalisationProperty) return
                        val resolvedName = resolved.name
                        resolvedMap.add(resolvedName)
                        if(guardStack.contains(resolvedName)) throw RecursionException(e, resolved, resolvedName)
                        guardStack.addLast(resolvedName)
                        doRecursiveVisit(resolved)
                        guardStack.removeLast()
                    }
                })
            }
            
            private fun registerProblem(element: ParadoxLocalisationProperty, recursion: PsiElement) {
                val message = PlsBundle.message("inspection.localisation.bug.unsupportedRecursion.description.1")
                val location = element.propertyKey 
                holder.registerProblem(location, message, NavigateToRecursionFix(recursion))
            }
        }
    }
    
    private fun isFileToInspect(file: PsiFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        val filePath = fileInfo.pathToEntry
        return filePath.canBeLocalisationPath()
    }
    
    private class NavigateToRecursionFix(
        target: PsiElement,
    ) : LocalQuickFixAndIntentionActionOnPsiElement(target) {
        override fun getText() = PlsBundle.message("inspection.localisation.bug.unsupportedRecursion.quickFix.1")
        
        override fun getFamilyName() = text
        
        override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
            if(editor == null) return
            navigateTo(editor, startElement)
        }
        
        override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor) = IntentionPreviewInfo.EMPTY
        
        override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY
        
        override fun startInWriteAction() = false
        
        override fun availableInBatchMode() = false
        
        private fun navigateTo(editor: Editor, toNavigate: PsiElement) {
            editor.caretModel.moveToOffset(toNavigate.textOffset)
            editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
        }
    }
    
    private class RecursionException(
        val recursion: PsiElement,
        val resolved: ParadoxLocalisationProperty,
        val resolvedName: String,
    ): RuntimeException()
}