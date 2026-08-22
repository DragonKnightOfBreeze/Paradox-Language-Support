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
 * 检查事件脚本文件中的（位于事件声明中的）事件ID是否不正确。
 */
class IncorrectEventIdInspection : EventInspectionBase() {
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
        for (element in elements) checkEventIdForEventDeclaration(element, holder)
    }

    private fun checkEventIdForEventDeclaration(element: ParadoxScriptProperty, holder: ProblemsHolder) {
        ProgressManager.checkCanceled()
        val definitionInfo = element.definitionInfo ?: return
        if (definitionInfo.type != ParadoxDefinitionTypes.event) return
        val nameField = definitionInfo.typeConfig.nameField
        val eventId = definitionInfo.name
        if (ParadoxEventManager.isValidEventId(eventId)) return
        val nameElement = selectScope { element.nameElement(nameField) } ?: return
        val description = ChronicleInspectionBundle.message("script.incorrectEventId.desc", eventId)
        holder.registerProblem(nameElement, description)
    }
}
