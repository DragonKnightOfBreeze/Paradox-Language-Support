package icu.windea.pls.lang.inherit.impl

import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.inherit.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 为切换类型的定义（嵌套在基础类型的定义中），例如`swapped_civic`，实现定义继承的逻辑。
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

