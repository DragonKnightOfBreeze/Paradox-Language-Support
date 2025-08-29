package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.truncateAndKeepQuotes
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

class ParadoxScriptValueTreeElement(
    element: ParadoxScriptValue
) : ParadoxScriptTreeElement<ParadoxScriptValue>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        val parent = element.castOrNull<ParadoxScriptBlock>() ?: return emptyList()
        //允许混合value和property
        val result: MutableList<StructureViewTreeElement> = mutableListOf()
        parent.forEachChild {
            when {
                it is ParadoxScriptValue -> result.add(ParadoxScriptValueTreeElement(it))
                it is ParadoxScriptProperty -> result.add(ParadoxScriptPropertyTreeElement(it))
                it is ParadoxScriptParameterCondition -> result.add(ParadoxScriptParameterConditionTreeElement(it))
            }
        }
        postHandleMemberChildren(result)
        return result
    }

    override fun getPresentableText(): String? {
        val element = element ?: return null
        val limit = PlsFacade.getInternalSettings().presentableTextLengthLimit
        return element.value.truncateAndKeepQuotes(limit)
    }
}
