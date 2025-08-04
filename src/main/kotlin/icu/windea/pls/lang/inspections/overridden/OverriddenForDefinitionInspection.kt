package icu.windea.pls.lang.inspections.overridden

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.roots.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import org.jetbrains.annotations.*

/**
 * （对于脚本文件）检查是否存在对定义的重载。
 */
class OverriddenForDefinitionInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
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
        val fileInfo = file.fileInfo
        if (fileInfo == null) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptProperty) {
                    val definitionInfo = element.definitionInfo
                    if (definitionInfo != null) visitDefinition(element, definitionInfo)
                }
            }

            private fun visitDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) {
                val selector = selector(project, file).definition()
                val name = definitionInfo.name
                val type = definitionInfo.type
                if (name.isEmpty()) return //anonymous -> skipped
                if (name.isParameterized()) return //parameterized -> ignored
                val results = ParadoxDefinitionSearch.search(name, type, selector).findAll()
                if (results.size < 2) return //no override -> skip

                val locationElement = element.propertyKey
                val message = PlsBundle.message("inspection.overriddenForDefinition.desc", name)
                val fix = NavigateToOverriddenDefinitionsFix(name, element, results)
                holder.registerProblem(locationElement, message, fix)
            }
        }
    }

    private class NavigateToOverriddenDefinitionsFix(key: String, element: PsiElement, elements: Collection<PsiElement>) : NavigateToFix(key, element, elements) {
        override fun getText() = PlsBundle.message("inspection.overriddenForDefinition.fix.1")

        override fun getPopupTitle(editor: Editor) = PlsBundle.message("inspection.overriddenForDefinition.fix.1.popup.title", key)

        override fun getPopupText(editor: Editor, value: PsiElement): @Nls String {
            val file = value.containingFile
            val lineNumber = PsiDocumentManager.getInstance(file.project).getDocument(file)?.getLineNumber(value.textOffset) ?: "?"
            val filePath = file.fileInfo?.rootInfo?.castOrNull<ParadoxRootInfo.MetadataBased>()?.rootFile?.path
            if (filePath == null) return PlsBundle.message("inspection.fix.navigate.popup.text.2", key, lineNumber)
            return PlsBundle.message("inspection.fix.navigate.popup.text.3", key, lineNumber, filePath)
        }
    }
}
