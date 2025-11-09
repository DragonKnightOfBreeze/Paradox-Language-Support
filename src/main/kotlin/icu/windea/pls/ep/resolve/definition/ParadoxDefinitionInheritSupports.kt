package icu.windea.pls.ep.resolve.definition

import icu.windea.pls.ep.data.StellarisEventData
import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.getDefinitionData
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.ParadoxEventManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.findParentProperty

/**
 * 为切换类型的定义实现定义继承的逻辑。
 *
 * 切换类型一般嵌套在基础类型的定义中，例如，`swapped_civic`。
 */
class ParadoxSwappedTypeInheritSupport : ParadoxDefinitionInheritSupport {
    override fun getSuperDefinition(definitionInfo: ParadoxDefinitionInfo): ParadoxScriptDefinitionElement? {
        val definition = definitionInfo.element
        val baseType = definitionInfo.typeConfig.baseType
        if (baseType == null) return null
        val superDefinition = definition.findParentProperty()
        val superDefinitionInfo = superDefinition?.definitionInfo ?: return null
        if (!ParadoxDefinitionTypeExpression.resolve(baseType).matches(superDefinitionInfo)) return null
        return superDefinition
    }
}

/**
 * 为 Stellaris 的事件实现定义继承的逻辑。
 *
 * 子事件将会继承父事件的各项属性。
 */
@WithGameType(ParadoxGameType.Stellaris)
class StellarisEventInheritSupport : ParadoxDefinitionInheritSupport {
    private val tEvent = ParadoxDefinitionTypes.Event
    override fun getSuperDefinition(definitionInfo: ParadoxDefinitionInfo): ParadoxScriptDefinitionElement? {
        // 子事件应当有子类型 `inherited`，并且父事件应当和子事件有相同的事件类型
        if (definitionInfo.type != tEvent || !definitionInfo.subtypes.contains("inherited")) return null
        val definition = definitionInfo.element
        val data = definition.getDefinitionData<StellarisEventData>() ?: return null
        val baseName = data.base ?: return null
        val selector = selector(definitionInfo.project, definition).definition().contextSensitive()
        val superDefinition = ParadoxDefinitionSearch.search(baseName, tEvent, selector).find() ?: return null
        val superDefinitionInfo = superDefinition.definitionInfo ?: return null
        // 事件类型不匹配 - 不处理
        if (ParadoxEventManager.getType(definitionInfo) != ParadoxEventManager.getType(superDefinitionInfo)) return null
        return superDefinition
    }

    // TODO 2.0.7+ （按条件）使用父事件的标题、描述和图片

    // （按父事件中已声明的属性）禁用代码检查 `ParadoxScriptMissingExpression`
    // 参见：`icu.windea.pls.ep.inspections.ParadoxDefinitionInspectionSuppressionProvider`
}
