package icu.windea.pls.lang.inspections.overridden

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.ep.priority.ParadoxPriority
import icu.windea.pls.ep.priority.ParadoxPriorityProvider
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.quickfix.NavigateToFix
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.vfs.PlsVfsManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * （对于脚本文件）检查是否存在不正确的对定义的重载。
 */
class IncorrectOverriddenForDefinitionInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (PlsVfsManager.isLightFile(file.virtualFile)) return false // skip for in-memory files
        if (selectRootFile(file) == null) return false
        if (!inProject(file)) return false // only for project files
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
                val priority = ParadoxPriorityProvider.getPriority(element)
                if (priority == ParadoxPriority.ORDERED) return // only for FIOS and LIOS
                val selector = selector(project, file).definition()
                val name = definitionInfo.name
                val type = definitionInfo.type
                if (name.isEmpty()) return // anonymous -> skipped
                if (name.isParameterized()) return // parameterized -> ignored
                val results = ParadoxDefinitionSearch.search(name, type, selector).findAll()
                if (results.size < 2) return // no override -> skip
                val firstResult = results.first()
                val firstRootInfo = firstResult.fileInfo?.rootInfo
                if (firstRootInfo !is ParadoxRootInfo.MetadataBased) return
                val rootInfo = fileInfo.rootInfo
                if (rootInfo !is ParadoxRootInfo.MetadataBased) return
                if (firstRootInfo.rootFile == rootInfo.rootFile) return

                // different root file -> incorrect override
                val locationElement = element.propertyKey
                val message = PlsBundle.message("inspection.incorrectOverriddenForDefinition.desc", name, priority)
                val fix = NavigateToOverriddenDefinitionsFix(name, element, results)
                holder.registerProblem(locationElement, message, fix)
            }
        }
    }

    private class NavigateToOverriddenDefinitionsFix(key: String, element: PsiElement, definitions: Collection<PsiElement>) : NavigateToFix(key, element, definitions) {
        override fun getText() = PlsBundle.message("inspection.incorrectOverriddenForDefinition.fix.1")

        override fun getPopupTitle(editor: Editor) = PlsBundle.message("inspection.incorrectOverriddenForDefinition.fix.1.popup.title", key)

        override fun getPopupText(editor: Editor, value: PsiElement): String {
            val file = value.containingFile
            val lineNumber = PsiDocumentManager.getInstance(file.project).getDocument(file)?.getLineNumber(value.textOffset) ?: "?"
            val filePath = file.fileInfo?.rootInfo?.castOrNull<ParadoxRootInfo.MetadataBased>()?.rootFile?.path
            if (filePath == null) return PlsBundle.message("fix.navigate.popup.text.2", key, lineNumber)
            return PlsBundle.message("fix.navigate.popup.text.3", key, lineNumber, filePath)
        }
    }
}
