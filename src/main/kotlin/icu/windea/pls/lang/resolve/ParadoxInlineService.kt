package icu.windea.pls.lang.resolve

import icu.windea.pls.ep.resolve.ParadoxInlineSupport
import icu.windea.pls.script.psi.ParadoxScriptMember

object ParadoxInlineService {
    /**
     * @see ParadoxInlineSupport.getInlinedElement
     */
    fun getInlinedElement(element: ParadoxScriptMember): ParadoxScriptMember? {
        return ParadoxInlineSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            ep.getInlinedElement(element)
        }
    }
}
