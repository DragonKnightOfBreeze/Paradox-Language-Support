package icu.windea.pls.lang.resolve

import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.ep.resolve.ParadoxInlineSupport
import icu.windea.pls.script.psi.ParadoxScriptMember

object ParadoxInlineService {
    /**
     * @see ParadoxInlineSupport.getInlinedElement
     */
    fun getInlinedElement(element: ParadoxScriptMember): ParadoxScriptMember? {
        // NOTE recursion guard is required here
        return withRecursionGuard {
            ParadoxInlineSupport.EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.getInlinedElement(element)
            }?.also { recursionCheck(it) }
        }
    }
}
