package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.truncateAndKeepQuotes
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtValue

class CwtValueTreeElement(
    element: CwtValue
) : CwtTreeElement<CwtValue>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> {
        val element = element ?: return emptyList()
        if (element !is CwtBlock) return emptyList()
        //允许混合value和property
        val result: MutableList<StructureViewTreeElement> = mutableListOf()
        element.forEachChild {
            when {
                it is CwtValue -> result.add(CwtValueTreeElement(it))
                it is CwtProperty -> result.add(CwtPropertyTreeElement(it))
            }
        }
        return result
    }

    override fun getPresentableText(): String? {
        val element = element ?: return null
        val limit = PlsFacade.getInternalSettings().presentableTextLengthLimit
        return element.name.truncateAndKeepQuotes(limit)
    }
}
