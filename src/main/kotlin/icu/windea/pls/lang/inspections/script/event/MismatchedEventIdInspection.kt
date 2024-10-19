package icu.windea.pls.lang.inspections.script.event

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * 检查事件脚本文件中的事件ID与事件所属的命名空间是否匹配。
 *
 * 注意：这项代码检查不是强制性的，未通过这项代码检查并不意味着脚本文件中存在错误，以至于导致游戏运行时的异常。
 */
class MismatchedEventIdInspection : LocalInspectionTool() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (!shouldCheckFile(file)) return null

        val properties = mutableListOf<ParadoxScriptProperty>()
        file as ParadoxScriptFile
        file.processProperty(inline = true) p@{ property ->
            properties += property
            true
        }
        if (properties.isEmpty()) return null
        val namespace2Events = mutableMapOf<String, MutableList<ParadoxScriptProperty>>()
        var nextNamespace = ""
        for (property in properties) {
            ProgressManager.checkCanceled()
            val definitionInfo = property.definitionInfo ?: continue
            if (definitionInfo.type == "event_namespace") {
                //如果值不是一个字符串，作为空字符串存到缓存中
                val namespace = property.propertyValue?.castOrNull<ParadoxScriptString>()?.stringValue.orEmpty()
                nextNamespace = namespace
                namespace2Events.getOrPut(namespace) { mutableListOf() }
            } else if (definitionInfo.type == "event") {
                namespace2Events.getOrPut(nextNamespace) { mutableListOf() }.add(property)
            }
        }
        if (namespace2Events.isEmpty()) return null
        val holder = ProblemsHolder(manager, file, isOnTheFly)
        for ((namespace, events) in namespace2Events) {
            ProgressManager.checkCanceled()
            if (namespace.isEmpty()) continue
            if (events.isEmpty()) continue
            for (event in events) {
                val definitionInfo = event.definitionInfo ?: continue
                val eventIdField = definitionInfo.typeConfig.nameField
                val eventIdProperty: ParadoxScriptExpressionElement = when (eventIdField) {
                    null -> event.propertyKey
                    else -> event.findProperty(eventIdField)?.propertyValue
                } ?: continue
                val eventId = eventIdProperty.stringValue() ?: continue
                if (!ParadoxEventManager.isMatchedEventId(eventId, namespace)) {
                    holder.registerProblem(eventIdProperty, PlsBundle.message("inspection.script.mismatchedEventId.desc", eventId, namespace))
                }
            }
        }
        return holder.resultsArray
    }

    private fun shouldCheckFile(file: PsiFile): Boolean {
        //仅检查事件脚本文件
        val fileInfo = file.fileInfo ?: return false
        val filePath = fileInfo.path
        return "txt" == filePath.fileExtension && "events".matchesPath(filePath.path)
    }
}
