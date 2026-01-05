package icu.windea.pls.ep.resolve.definition

import com.intellij.openapi.util.ModificationTracker
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeGroup
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.ep.util.data.StellarisEventData
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.getDefinitionData
import icu.windea.pls.lang.psi.findParentProperty
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.model.constants.ParadoxDefinitionTypes as T

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
        return getSuperDefinition(definitionInfo, baseType)
    }

    private fun getBaseType(definitionInfo: ParadoxDefinitionInfo): String? {
        return definitionInfo.typeConfig.baseType
    }

    private fun getSuperDefinition(definitionInfo: ParadoxDefinitionInfo, baseType: String): ParadoxScriptDefinitionElement? {
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
}

/**
 * 为 Stellaris 的事件实现定义继承的逻辑。
 *
 * 说明：
 * - 子事件会继承父事件的各项属性。
 * - 子事件会继承父事件的部分子类型。
 *   - 目前，认为这包括作为事件特性的子类型，如 `triggered`。
 *   - 如果子事件声明中存在 `trigger_clear = yes`，则排除 `triggerred`。
 *
 * @see CwtSubtypeGroup.EventAttribute
 */
@WithGameType(ParadoxGameType.Stellaris)
class StellarisEventInheritSupport : ParadoxDefinitionInheritSupport {
    // 子事件应当有子类型 `inherited`，并且父事件应当和子事件有相同的事件类型

    override fun getSuperDefinition(definitionInfo: ParadoxDefinitionInfo): ParadoxScriptDefinitionElement? {
        val baseName = getBaseName(definitionInfo, definitionInfo.subtypeConfigs) ?: return null
        return getSuperDefinition(definitionInfo, baseName, definitionInfo.subtypeConfigs)
    }

    override fun getModificationTracker(definitionInfo: ParadoxDefinitionInfo): ModificationTracker? {
        val baseName = getBaseName(definitionInfo, definitionInfo.subtypeConfigs)
        if (baseName == null) return null
        return ParadoxModificationTrackers.ScriptFile("events/**/*.txt") // 任意事件脚本文件
    }

    override fun processSubtypeConfigs(definitionInfo: ParadoxDefinitionInfo, subtypeConfigs: MutableList<CwtSubtypeConfig>): Boolean {
        val baseName = getBaseName(definitionInfo, subtypeConfigs) ?: return true
        val superDefinition = getSuperDefinition(definitionInfo, baseName, subtypeConfigs) ?: return true
        val superDefinitionInfo = superDefinition.definitionInfo ?: return true
        superDefinitionInfo.subtypeConfigs.filterTo(subtypeConfigs) { it.inGroup(CwtSubtypeGroup.EventAttribute) }
        val clearData = getData(definitionInfo)?.triggerClear ?: false
        if (clearData) {
            subtypeConfigs.removeIf { it.name == "triggered" }
        }
        return false
    }

    private fun getBaseName(definitionInfo: ParadoxDefinitionInfo, subtypeConfigs: List<CwtSubtypeConfig>): String? {
        if (definitionInfo.type != T.event) return null
        if (subtypeConfigs.none { it.name == "inherited" }) return null
        val data = getData(definitionInfo) ?: return null
        return data.base
    }

    private fun getSuperDefinition(definitionInfo: ParadoxDefinitionInfo, baseName: String, subtypeConfigs: List<CwtSubtypeConfig>): ParadoxScriptDefinitionElement? {
        val result = withRecursionGuard {
            withRecursionCheck(baseName) a@{
                val selector = selector(definitionInfo.project, definitionInfo.element).definition().contextSensitive()
                val superDefinition = ParadoxDefinitionSearch.search(baseName, T.event, selector).find() ?: return@a null
                val superDefinitionInfo = superDefinition.definitionInfo ?: return@a null

                // 事件类型不匹配 - 不处理
                val eventType = subtypeConfigs.find { it.inGroup(CwtSubtypeGroup.EventType) }?.name
                val superEventType = superDefinitionInfo.subtypeConfigs.find { it.inGroup(CwtSubtypeGroup.EventType) }?.name
                if (eventType != superEventType) return@a null

                superDefinition
            }
        }
        return result
    }

    private fun getData(definitionInfo: ParadoxDefinitionInfo) = definitionInfo.element.getDefinitionData<StellarisEventData>(relax = true)

    // TODO 2.0.7+ （按条件）使用父事件的标题、描述和图片

    // （按父事件中已声明的属性）禁用代码检查 `ParadoxScriptMissingExpression`
    // 参见：`icu.windea.pls.ep.inspections.ParadoxDefinitionInspectionSuppressionProvider`
}
