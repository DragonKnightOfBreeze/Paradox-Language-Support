package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.core.psi.PsiFileOnlyVisitor
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.manipulation.ParadoxEventManipulationService
import icu.windea.pls.lang.psi.stringValue
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxEventManager
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 检查事件脚本文件中的（位于事件声明中的）事件ID是否不匹配事件命名空间。
 *
 * 说明：
 * - 此代码检查是启发式的，可能存在误报。
 * - 此代码检查未通过时，不一定意味着会引发游戏引擎层面的异常。
 * - 实际上，事件脚本文件中可以不声明或者声明多个事件命名空间，事件ID不需要严格匹配同文件中的先前声明的事件命名空间。
 */
class MismatchedEventIdInspection : EventInspectionBase() {
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
        val map = ParadoxEventManipulationService.getBoundEventDeclarationsInFile(file)
        if (map.isEmpty()) return
        for ((namespace, events) in map) {
            ProgressManager.checkCanceled()
            if (events.isEmpty()) continue
            for (event in events) checkEventIdForEventDeclaration(event, namespace, holder)
        }
    }

    private fun checkEventIdForEventDeclaration(element: ParadoxScriptProperty, namespace: String, holder: ProblemsHolder) {
        val definitionInfo = element.definitionInfo ?: return
        val nameField = definitionInfo.typeConfig.nameField
        val nameElement = selectScope { element.nameElement(nameField) } ?: return
        val eventId = nameElement.stringValue() ?: return
        if (namespace.isEmpty()) {
            val description = ChronicleInspectionBundle.message("script.mismatchedEventId.desc.1", eventId)
            holder.registerProblem(nameElement, description)
            // 不存在绑定的命名空间
        } else {
            // 存在绑定的命名空间但不匹配
            if (ParadoxEventManager.isMatchedEventId(eventId, namespace)) return
            val description = ChronicleInspectionBundle.message("script.mismatchedEventId.desc.2", eventId, namespace)
            holder.registerProblem(nameElement, description)
        }
    }
}
