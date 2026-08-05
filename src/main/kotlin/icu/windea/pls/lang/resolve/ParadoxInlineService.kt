package icu.windea.pls.lang.resolve

import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.ep.resolve.ParadoxInlineSupport
import icu.windea.pls.script.psi.ParadoxScriptMember

@Optimized
object ParadoxInlineService {
    /**
     * @see ParadoxInlineSupport.getInlinedElement
     */
    fun getInlinedElement(element: ParadoxScriptMember): ParadoxScriptMember? {
        // NOTE recursion guard is required here
        val eps = ParadoxInlineSupport.EP_NAME.extensionList
        withRecursionGuard({}.javaClass.name) {
            eps.forEachFast { ep ->
                ep.getInlinedElement(element)?.also { recursionCheck(it) }?.let { return it }
            }
        }
        return null
    }
}
