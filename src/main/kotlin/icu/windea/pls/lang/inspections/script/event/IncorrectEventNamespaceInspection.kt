package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.core.psi.PsiFileOnlyVisitor
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxEventManager
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 检查事件脚本文件中的事件命名空间声明是否不正确。
 */
class IncorrectEventNamespaceInspection : EventInspectionBase() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiFileOnlyVisitor() {
            override fun visitFile(file: PsiFile) {
                ProgressManager.checkCanceled()
                check(file, holder)
            }
        }
    }

    private fun check(file: PsiFile, holder: ProblemsHolder) {
        if (file !is ParadoxScriptFile) return
        val elements = file.properties(inline = true)
        for (element in elements) checkEventNamespace(element, holder)
    }

    private fun checkEventNamespace(element: ParadoxScriptProperty, holder: ProblemsHolder) {
        ProgressManager.checkCanceled()
        val definitionInfo = element.definitionInfo ?: return
        if (definitionInfo.type != ParadoxDefinitionTypes.eventNamespace) return
        val nameField = definitionInfo.typeConfig.nameField
        val eventNamespace = definitionInfo.name
        if (ParadoxEventManager.isValidEventNamespace(eventNamespace)) return
        val nameElement = selectScope { element.nameElement(nameField) } ?: return
        val description = ChronicleInspectionBundle.message("script.incorrectEventNamespace.desc", eventNamespace)
        holder.registerProblem(nameElement, description)
    }
}
