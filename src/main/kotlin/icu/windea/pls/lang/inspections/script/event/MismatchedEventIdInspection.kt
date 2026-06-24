package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.manipulation.ParadoxEventManipulationService
import icu.windea.pls.lang.psi.stringValue
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxEventManager
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 事件脚本文件中的（位于事件声明中的）不匹配事件命名空间的事件ID的代码检查。
 *
 * 说明：
 * - 此代码检查是启发式的，可能存在误报。
 * - 此代码检查未通过时，不一定意味着会引发游戏引擎层面的异常。
 * - 实际上，事件脚本文件中可以不声明或者声明多个事件命名空间，事件ID不需要严格匹配同文件中的先前声明的事件命名空间。
 */
class MismatchedEventIdInspection : EventInspectionBase() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is ParadoxScriptFile) return null
        val map = ParadoxEventManipulationService.getBoundEventDeclarationsInFile(file)
        if (map.isEmpty()) return null
        val holder = ProblemsHolder(manager, file, isOnTheFly)
        for ((namespace, events) in map) {
            ProgressManager.checkCanceled()
            if (events.isEmpty()) continue
            for (event in events) checkEventIdForEventDeclaration(event, namespace, holder)
        }
        return holder.resultsArray
    }

    private fun checkEventIdForEventDeclaration(element: ParadoxScriptProperty, namespace: String, holder: ProblemsHolder) {
        val definitionInfo = element.definitionInfo ?: return
        val nameField = definitionInfo.typeConfig.nameField
        val nameElement = selectScope { element.nameElement(nameField) } ?: return
        val eventId = nameElement.stringValue() ?: return
        if (namespace.isEmpty()) {
            val description = PlsBundle.message("inspection.script.mismatchedEventId.desc.1", eventId)
            holder.registerProblem(nameElement, description)
            // 不存在绑定的命名空间
        } else {
            // 存在绑定的命名空间但不匹配
            if (ParadoxEventManager.isMatchedEventId(eventId, namespace)) return
            val description = PlsBundle.message("inspection.script.mismatchedEventId.desc.2", eventId, namespace)
            holder.registerProblem(nameElement, description)
        }
    }
}
