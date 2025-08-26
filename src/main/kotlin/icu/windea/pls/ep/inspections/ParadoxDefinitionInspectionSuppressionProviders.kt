package icu.windea.pls.ep.inspections

import icu.windea.pls.core.annotations.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class BaseParadoxDefinitionInspectionSuppressionProvider : ParadoxDefinitionInspectionSuppressionProvider {
    override fun getSuppressedToolIds(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String> {
        //1.1.2 禁用继承自其他事件的事件的某些检查
        if (definitionInfo.type == "event" && definitionInfo.subtypes.contains("inherited")) {
            return setOf("ParadoxScriptMissingExpression", "ParadoxScriptMissingLocalisation", "ParadoxScriptMissingImage")
        }
        return emptySet()
    }
}

@WithGameType(ParadoxGameType.Stellaris)
class BaseStellarisDefinitionInspectionSuppressionProvider : ParadoxDefinitionInspectionSuppressionProvider {
    override fun getSuppressedToolIds(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String> {
        //1.1.2 传统的采纳和完成不需要有对应的图片
        if ((definitionInfo.type == "tradition" || definitionInfo.typeConfig.baseType == "tradition") && definitionInfo.name.let { it.endsWith("_adopt") || it.endsWith("_finish") }) {
            return setOf("ParadoxScriptMissingImage")
        }
        //1.1.2 禁用名字以数字结尾的领袖特质的某些检查
        if (definitionInfo.type == "trait" && definitionInfo.subtypes.contains("leader_trait") && definitionInfo.name.substringAfterLast('_', "").toIntOrNull() != null) {
            return setOf("ParadoxScriptMissingLocalisation", "ParadoxScriptMissingImage")
        }
        return emptySet()
    }
}
