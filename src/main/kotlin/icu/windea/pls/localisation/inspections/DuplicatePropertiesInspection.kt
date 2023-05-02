package icu.windea.pls.localisation.inspections

import com.intellij.codeInsight.intention.preview.*
import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * 同一文件中重复的（同一语言区域的）属性声明的检查。
 *
 * 提供快速修复：
 * * 导航到重复项
 */
class DuplicatePropertiesInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxLocalisationPropertyList) visitPropertyList(element)
            }
            
            private fun visitPropertyList(element: ParadoxLocalisationPropertyList) {
                ProgressManager.checkCanceled()
                val propertyGroup = element.propertyList.groupBy { it.name }
                if(propertyGroup.isEmpty()) return
                for((key, values) in propertyGroup) {
                    if(values.size <= 1) continue
                    for(value in values) {
                        //第一个元素指定为file，则是在文档头部弹出，否则从psiElement上通过contextActions显示
                        val location = value.propertyKey
                        holder.registerProblem(
                            location, PlsBundle.message("inspection.localisation.duplicateProperties.description", key),
                            NavigateToDuplicatesFix(key, value, values)
                        )
                    }
                }
            }
        }
    }
    
    private class NavigateToDuplicatesFix(
        private val key: String,
        property: ParadoxLocalisationProperty,
        duplicates: List<ParadoxLocalisationProperty>
    ) : LocalQuickFixAndIntentionActionOnPsiElement(property) {
        private val pointers = duplicates.map { it.createPointer() }
        
        override fun getText() = PlsBundle.message("inspection.localisation.duplicateProperties.quickfix.1")
        
        override fun getFamilyName() = text
        
        override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
            if(editor == null) return
            //当重复的属性只有另外一个时，直接导航即可
            //如果有多个，则需要创建listPopup
            if(pointers.size == 2) {
                val iterator = pointers.iterator()
                val next = iterator.next().element
                val toNavigate = if(next != startElement) next else iterator.next().element
                if(toNavigate != null) navigateTo(editor, toNavigate)
            } else {
                val allElements = pointers.mapNotNull { it.element }.filter { it !== startElement }
                JBPopupFactory.getInstance().createListPopup(object : BaseListPopupStep<ParadoxLocalisationProperty>(PlsBundle.message("inspection.localisation.duplicateProperties.quickFix.1.popup.header", key), allElements) {
                    override fun getIconFor(value: ParadoxLocalisationProperty) = value.icon
                    
                    override fun getTextFor(value: ParadoxLocalisationProperty) =
                        PlsBundle.message("inspection.localisation.duplicateProperties.quickFix.1.popup.text", key, editor.document.getLineNumber(value.textOffset))
                    
                    override fun getDefaultOptionIndex() = 0
                    
                    override fun isSpeedSearchEnabled() = true
                    
                    override fun onChosen(selectedValue: ParadoxLocalisationProperty, finalChoice: Boolean): PopupStep<*>? {
                        navigateTo(editor, selectedValue)
                        return FINAL_CHOICE
                    }
                }).showInBestPositionFor(editor)
            }
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
}
