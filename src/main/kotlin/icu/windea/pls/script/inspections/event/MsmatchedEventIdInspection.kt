package icu.windea.pls.script.inspections.event

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 检查事件脚本文件中的事件ID与事件所属的命名空间是否匹配。
 */
class MsmatchedEventIdInspection : LocalInspectionTool() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        //仅检查事件脚本文件
        if(!isEventScriptFile(file)) return null
        
        file as ParadoxScriptFile
        val rootBlock = file.block ?: return null
        val properties = rootBlock.propertyList
        if(properties.isEmpty()) return null //空文件，不进行检查
        if(properties.find { it.name.equals("namespace", true) } == null) return null //没有事件命名空间，不进行检查
        val eventGroup: MutableMap<String, MutableList<ParadoxScriptProperty>> = mutableMapOf() //namespace - eventDefinitions
        var nextNamespace = ""
        for(property in properties) {
            ProgressManager.checkCanceled()
            if(property.name.equals("namespace", true)) {
                //如果值不是一个字符串，作为空字符串存到缓存中
                val namespace = property.propertyValue?.castOrNull<ParadoxScriptString>()?.stringValue.orEmpty()
                nextNamespace = namespace
                eventGroup.getOrPut(namespace) { mutableListOf() }
            } else {
                val definitionInfo = property.definitionInfo ?: continue //不是定义，跳过
                if(definitionInfo.type != "event") continue //不是事件定义，跳过 
                eventGroup.getOrPut(nextNamespace) { mutableListOf() }.add(property)
            }
        }
        val holder = ProblemsHolder(manager, file, isOnTheFly)
        for((namespace, events) in eventGroup) {
            ProgressManager.checkCanceled()
            if(namespace.isEmpty()) continue
            if(events.isEmpty()) continue
            for(event in events) {
                val definitionInfo = event.definitionInfo ?: continue
                val eventIdField = definitionInfo.typeConfig.nameField
                val eventIdProperty: ParadoxScriptExpressionElement = when(eventIdField) {
                    null -> event.propertyKey
                    else -> event.findProperty(eventIdField)?.propertyValue
                } ?: continue
                val eventId = eventIdProperty.stringValue() ?: continue
                if(!ParadoxEventHandler.isMatchedEventId(eventId, namespace)) {
                    holder.registerProblem(eventIdProperty, PlsBundle.message("inspection.script.event.mismatchedEventId.description", eventId, namespace))
                }
            }
        }
        return holder.resultsArray
    }
    
    private fun isEventScriptFile(file: PsiFile): Boolean {
        if(file !is ParadoxScriptFile) return false
        val fileInfo = file.fileInfo ?: return false
        return "events".matchesPath(fileInfo.pathToEntry.path, acceptSelf = false)
    }
}
