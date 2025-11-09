package icu.windea.pls.ep.resolve.definition

import com.intellij.openapi.util.ModificationTracker
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.ep.data.StellarisEventData
import icu.windea.pls.lang.ParadoxModificationTrackers
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
 * 说明：
 * - 切换类型一般嵌套在基础类型的定义中，例如，`swapped_civic`。
 */
class ParadoxSwappedTypeInheritSupport : ParadoxDefinitionInheritSupport {
    override fun getSuperDefinition(definitionInfo: ParadoxDefinitionInfo): ParadoxScriptDefinitionElement? {
        val baseType = getBaseType(definitionInfo)
        if (baseType == null) return null
        val result = withRecursionGuard {
            withRecursionCheck(baseType) a@{
                val superDefinition = definitionInfo.element.findParentProperty()
                val superDefinitionInfo = superDefinition?.definitionInfo ?: return@a null
                if (!ParadoxDefinitionTypeExpression.resolve(baseType).matches(superDefinitionInfo)) return@a null
                superDefinition
            }
        }
        return result
    }

    private fun getBaseType(definitionInfo: ParadoxDefinitionInfo): String? {
        return definitionInfo.typeConfig.baseType
    }
}

/**
 * 为 Stellaris 的事件实现定义继承的逻辑。
 *
 * 说明：
 * - 子事件会继承父事件的各项属性。
 * - 子事件会继承父事件的部分子类型。
 *   - 目前，认为这包括作为事件特性的子类型，如 `triggered`。
 *   - 如果子事件声明中存在 `trigger_clear = yes`，则排除 `triggerred`。
 */
@WithGameType(ParadoxGameType.Stellaris)
class StellarisEventInheritSupport : ParadoxDefinitionInheritSupport {
    private val tEvent = ParadoxDefinitionTypes.Event

    override fun getSuperDefinition(definitionInfo: ParadoxDefinitionInfo): ParadoxScriptDefinitionElement? {
        val baseName = getBaseName(definitionInfo)
        if (baseName == null) return null
        val result = withRecursionGuard {
            withRecursionCheck(baseName) a@{
                val selector = selector(definitionInfo.project, definitionInfo.element).definition().contextSensitive()
                val superDefinition = ParadoxDefinitionSearch.search(baseName, tEvent, selector).find() ?: return@a null
                val superDefinitionInfo = superDefinition.definitionInfo ?: return@a null
                if (matchesEventType(definitionInfo, superDefinitionInfo)) return@a null // 事件类型不匹配 - 不处理
                superDefinition
            }
        }
        return result
    }

    private fun matchesEventType(definitionInfo: ParadoxDefinitionInfo, superDefinitionInfo: ParadoxDefinitionInfo): Boolean {
        return ParadoxEventManager.getType(definitionInfo) != ParadoxEventManager.getType(superDefinitionInfo)
    }

    override fun getModificationTracker(definitionInfo: ParadoxDefinitionInfo): ModificationTracker? {
        val baseName = getBaseName(definitionInfo)
        if (baseName == null) return null
        return ParadoxModificationTrackers.ScriptFile("events/**/*.txt") // 任意事件脚本文件
    }

    override fun processSubtypeConfigs(definitionInfo: ParadoxDefinitionInfo, subtypeConfigs: MutableList<CwtSubtypeConfig>) {
        val superDefinition = getSuperDefinition(definitionInfo) ?: return
        val superDefinitionInfo = superDefinition.definitionInfo ?: return
        superDefinitionInfo.subtypeConfigs.filterTo(subtypeConfigs) { it.group == "event_attribute" }
        if (clearTrigger(definitionInfo)) {
            subtypeConfigs.removeIf { it.name == "triggered" }
        }
    }

    private fun getBaseName(definitionInfo: ParadoxDefinitionInfo): String? {
        // 子事件应当有子类型 `inherited`，并且父事件应当和子事件有相同的事件类型
        if (definitionInfo.type != tEvent || !definitionInfo.subtypes.contains("inherited")) return null
        val data = definitionInfo.element.getDefinitionData<StellarisEventData>(relax = true) ?: return null
        return data.base
    }

    private fun clearTrigger(definitionInfo: ParadoxDefinitionInfo): Boolean {
        val data = definitionInfo.element.getDefinitionData<StellarisEventData>(relax = true) ?: return false
        return data.triggerClear
    }

    // TODO 2.0.7+ （按条件）使用父事件的标题、描述和图片

    // （按父事件中已声明的属性）禁用代码检查 `ParadoxScriptMissingExpression`
    // 参见：`icu.windea.pls.ep.inspections.ParadoxDefinitionInspectionSuppressionProvider`
}
