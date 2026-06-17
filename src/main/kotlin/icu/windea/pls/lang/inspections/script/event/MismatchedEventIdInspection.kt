package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.psi.stringValue
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxEventManager
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.constraints.ParadoxGameTypeConstraint
import icu.windea.pls.model.constraints.matchesBy
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.propertyValue
import icu.windea.pls.script.psi.stringValue

/**
 * 事件脚本文件中的（位于事件声明中的）不匹配事件命名空间的事件ID的代码检查。
 *
 * 说明：
 * - 此代码检查是启发式的，可能存在误报。
 * - 此代码检查未通过时，不一定意味着会引发游戏引擎层面的异常。
 * - 实际上，事件脚本文件中可以不声明或者声明多个事件命名空间，事件ID不需要匹配同文件中的先前最后声明的事件命名空间。
 */
class MismatchedEventIdInspection : EventInspectionBase() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (file !is ParadoxScriptFile) return null
        val context = mutableMapOf<String, MutableList<ParadoxScriptProperty>>()
        collectEventDeclarations(file, context)
        if (context.isEmpty()) return null
        val holder = ProblemsHolder(manager, file, isOnTheFly)
        for ((namespace, events) in context) {
            ProgressManager.checkCanceled()
            if (namespace.isEmpty()) continue
            if (events.isEmpty()) continue
            for (event in events) checkEventIdForEventDeclaration(event, namespace, holder)
        }
        return holder.resultsArray
    }

    private fun collectEventDeclarations(file: ParadoxScriptFile, context: MutableMap<String, MutableList<ParadoxScriptProperty>>) {
        val gameType = selectGameType(file) ?: return
        if (gameType matchesBy ParadoxGameTypeConstraint.JominiBased) {
            // #334
            collectMixedEventDeclarations(file, context)
        } else {
            collectSequentialEventDeclarations(file, context)
        }
    }

    private fun collectMixedEventDeclarations(file: ParadoxScriptFile, context: MutableMap<String, MutableList<ParadoxScriptProperty>>) {
        val properties = file.properties(inline = true)
        var nextNamespace = ""
        for (property in properties) {
            ProgressManager.checkCanceled()
            val definitionInfo = property.definitionInfo ?: continue
            if (definitionInfo.type == ParadoxDefinitionTypes.eventNamespace) {
                // 如果值不是一个字符串，作为空字符串存到缓存中
                val namespace = property.propertyValue<ParadoxScriptString>()?.stringValue.orEmpty()
                nextNamespace = namespace
                context.getOrPut(namespace) { mutableListOf() }
            } else if (definitionInfo.type == ParadoxDefinitionTypes.event) {
                context.getOrPut(nextNamespace) { mutableListOf() }.add(property)
            }
        }
    }

    private fun collectSequentialEventDeclarations(file: ParadoxScriptFile, context: MutableMap<String, MutableList<ParadoxScriptProperty>>) {
        val properties = file.properties(inline = true)
        var nextNamespace = ""
        for (property in properties) {
            ProgressManager.checkCanceled()
            val definitionInfo = property.definitionInfo ?: continue
            if (definitionInfo.type == ParadoxDefinitionTypes.eventNamespace) {
                // 如果值不是一个字符串，作为空字符串存到缓存中
                val namespace = property.propertyValue<ParadoxScriptString>()?.stringValue.orEmpty()
                nextNamespace = namespace
                context.getOrPut(namespace) { mutableListOf() }
            } else if (definitionInfo.type == ParadoxDefinitionTypes.event) {
                context.getOrPut(nextNamespace) { mutableListOf() }.add(property)
            }
        }
    }

    private fun checkEventIdForEventDeclaration(event: ParadoxScriptProperty, namespace: String, holder: ProblemsHolder) {
        val definitionInfo = event.definitionInfo ?: return
        val nameField = definitionInfo.typeConfig.nameField
        val nameElement = selectScope { event.nameElement(nameField) } ?: return
        val eventId = nameElement.stringValue() ?: return
        if (ParadoxEventManager.isMatchedEventId(eventId, namespace)) return
        val description = PlsBundle.message("inspection.script.mismatchedEventId.desc", eventId, namespace)
        holder.registerProblem(nameElement, description)
    }
}
