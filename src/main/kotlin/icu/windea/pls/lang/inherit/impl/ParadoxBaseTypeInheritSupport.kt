package icu.windea.pls.lang.inherit.impl

import icu.windea.pls.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.inherit.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 为一些`swapped_type`，例如`swapped_civic`，实现定义继承的逻辑。
 */
class ParadoxBaseTypeInheritSupport : ParadoxDefinitionInheritSupport {
    override fun getSuperDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScriptDefinitionElement? {
        val baseType = definitionInfo.typeConfig.baseType
        if(baseType == null) return null
        val parentDefinition = definition.findParentProperty()
        val parentDefinitionInfo = parentDefinition?.definitionInfo ?: return null
        if(!ParadoxDefinitionTypeExpression.resolve(baseType).matches(parentDefinitionInfo)) return null
        return parentDefinition
    }
}

