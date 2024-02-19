package icu.windea.pls.lang.inherit

import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 为切换类型的定义实现定义继承的逻辑。
 *
 * 切换类型一般嵌套在基础类型的定义中，例如，`swapped_civic`。
 */
class ParadoxSwappedTypeInheritSupport : ParadoxDefinitionInheritSupport {
    override fun getSuperDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScriptDefinitionElement? {
        val baseType = definitionInfo.typeConfig.baseType
        if(baseType == null) return null
        val parentDefinition = definition.findParentProperty()
        val parentDefinitionInfo = parentDefinition?.definitionInfo ?: return null
        if(!ParadoxDefinitionTypeExpression.resolve(baseType).matches(parentDefinitionInfo)) return null
        return parentDefinition
    }
}

/**
 * 为Stellaris的事件实现定义继承的逻辑。
 *
 * 子事件将会继承父事件的各项属性。
 */
@WithGameType(ParadoxGameType.Stellaris)
class StellarisEventInheritSupport: ParadoxDefinitionInheritSupport {
    override fun getSuperDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScriptDefinitionElement? {
        //子事件应当有子类型"inherited"，并且父事件应当和子事件有相同的事件类型
        if(definitionInfo.type != "event" || !definitionInfo.subtypes.contains("inherited")) return null
        val data = definition.getData<StellarisEventDataProvider.Data>() ?: return null
        val parentDefinitionName = data.base ?: return null
        val selector = definitionSelector(definitionInfo.project, definition).contextSensitive()
        val parentDefinition = ParadoxDefinitionSearch.search(parentDefinitionName, "event", selector).find() ?: return null
        val parentDefinitionInfo = parentDefinition.definitionInfo ?: return null
        //事件类型不匹配 - 不处理
        if(ParadoxEventHandler.getType(definitionInfo) != ParadoxEventHandler.getType(parentDefinitionInfo)) return null
        return parentDefinition
    }
    
    //（按条件）使用父事件的标题、描述和图片
    //1.2.0 TODO
    
    //（按父事件中已声明的属性）禁用代码检查 ParadoxScriptMissingExpression
    //参见 icu.windea.pls.core.inspections.PlsInspectionsExtensionsKt.isSuppressedForDefinition
}