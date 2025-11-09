package icu.windea.pls.lang.resolve

import icu.windea.pls.ep.resolve.ParadoxInlineSupport
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.script.psi.ParadoxScriptMember

object ParadoxInlineService {
    /**
     * @see ParadoxInlineSupport.getInlinedElement
     */
    fun getInlinedElement(element: ParadoxScriptMember): ParadoxScriptMember? {
        val gameType = selectGameType(element)
        return ParadoxInlineSupport.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getInlinedElement(element)
        }
    }
}
