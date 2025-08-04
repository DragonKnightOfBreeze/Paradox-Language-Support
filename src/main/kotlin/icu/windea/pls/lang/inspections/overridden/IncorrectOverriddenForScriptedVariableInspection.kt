package icu.windea.pls.lang.inspections.overridden

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.roots.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.priority.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import org.jetbrains.annotations.*

/**
 * （对于脚本文件）检查是否存在不正确的对（全局）封装的重载。
 */
class IncorrectOverriddenForScriptedVariableInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (PlsFileManager.isLightFile(file.virtualFile)) return false //不检查临时文件
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

        val isGlobal = "common/scripted_variables".matchesPath(fileInfo.path.path)
        if (!isGlobal) return PsiElementVisitor.EMPTY_VISITOR //only for global scripted variables
        val virtualFile = file.virtualFile
        val inProject = virtualFile != null && ProjectFileIndex.getInstance(project).isInContent(virtualFile)
        if (!inProject) return PsiElementVisitor.EMPTY_VISITOR //only for project files

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptScriptedVariable) visitScriptedVariable(element)
            }

            private fun visitScriptedVariable(element: ParadoxScriptScriptedVariable) {
                val priority = ParadoxPriorityProvider.getPriority(element)
                if (priority == ParadoxPriority.ORDERED) return //only for FIOS and LIOS
                val selector = selector(project, file).scriptedVariable()
                val name = element.name
                if (name.isNullOrEmpty()) return //anonymous -> skipped
                if (name.isParameterized()) return //parameterized -> ignored
                val results = ParadoxGlobalScriptedVariableSearch.search(name, selector).findAll()
                if (results.size < 2) return //no override -> skip
                val firstResult = results.first()
                val firstRootInfo = firstResult.fileInfo?.rootInfo
                if (firstRootInfo !is ParadoxRootInfo.MetadataBased) return
                val rootInfo = fileInfo.rootInfo
                if (rootInfo !is ParadoxRootInfo.MetadataBased) return
                if (firstRootInfo.rootFile == rootInfo.rootFile) return

                //different root file -> incorrect override
                val locationElement = element.scriptedVariableName
                val message = PlsBundle.message("inspection.incorrectOverriddenForScriptedVariable.desc", name, priority)
                val fix = NavigateToOverriddenScriptedVariablesFix(name, element, results)
                holder.registerProblem(locationElement, message, fix)
            }
        }
    }

    private class NavigateToOverriddenScriptedVariablesFix(key: String, element: PsiElement, scriptedVariables: Collection<PsiElement>) : NavigateToFix(key, element, scriptedVariables) {
        override fun getText() = PlsBundle.message("inspection.incorrectOverriddenForScriptedVariable.fix.1")

        override fun getPopupTitle(editor: Editor) = PlsBundle.message("inspection.incorrectOverriddenForScriptedVariable.fix.1.popup.title", key)

        override fun getPopupText(editor: Editor, value: PsiElement): @Nls String {
            val file = value.containingFile
            val lineNumber = PsiDocumentManager.getInstance(file.project).getDocument(file)?.getLineNumber(value.textOffset) ?: "?"
            val filePath = file.fileInfo?.rootInfo?.castOrNull<ParadoxRootInfo.MetadataBased>()?.rootFile?.path.orAnonymous()
            return PlsBundle.message("inspection.fix.navigate.popup.text.3", key, lineNumber, filePath)
        }
    }
}
