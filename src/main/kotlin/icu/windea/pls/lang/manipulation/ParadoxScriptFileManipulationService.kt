package icu.windea.pls.lang.manipulation

import com.intellij.openapi.progress.ProgressManager
import icu.windea.pls.core.children
import icu.windea.pls.core.collections.WalkingContext
import icu.windea.pls.core.collections.WalkingSequence
import icu.windea.pls.core.collections.forward
import icu.windea.pls.core.withContextRecursionGuard
import icu.windea.pls.lang.psi.conditional
import icu.windea.pls.lang.psi.inline
import icu.windea.pls.lang.resolve.ParadoxInlineService
import icu.windea.pls.script.psi.ParadoxScriptConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptMemberContext

object ParadoxScriptFileManipulationService {
    fun members(element: ParadoxScriptMemberContext): WalkingSequence<ParadoxScriptMember> {
        val context = WalkingContext()
        val delegate = with(context) { builderMembers(element) }
        return WalkingSequence(delegate, context)
    }

    context(context: WalkingContext)
    private fun builderMembers(element: ParadoxScriptMemberContext): Sequence<ParadoxScriptMember> {
        val containerElement = element.memberContainer ?: return emptySequence()
        return sequence {
            yieldMembers(containerElement)
        }
    }

    context(context: WalkingContext)
    private suspend fun SequenceScope<ParadoxScriptMember>.yieldMembers(element: ParadoxScriptMemberContext) {
        val containerElement = element.memberContainer ?: return
        containerElement.children(context.forward).forEach { child ->
            when (child) {
                is ParadoxScriptMember -> yieldMember(child)
                is ParadoxScriptConditionalBlock -> if (context.conditional) yieldConditionalMembers(child)
            }
        }
    }

    context(context: WalkingContext)
    private suspend fun SequenceScope<ParadoxScriptMember>.yieldMember(element: ParadoxScriptMember) {
        ProgressManager.checkCanceled()
        yield(element)
        if (context.inline) yieldInlineMember(element)
    }

    context(context: WalkingContext)
    private suspend fun SequenceScope<ParadoxScriptMember>.yieldConditionalMembers(element: ParadoxScriptConditionalBlock) {
        ProgressManager.checkCanceled()
        yieldMembers(element)
    }

    context(context: WalkingContext)
    private suspend fun SequenceScope<ParadoxScriptMember>.yieldInlineMember(element: ParadoxScriptMember) {
        ProgressManager.checkCanceled()
        // NOTE context recursion guard is required here (again)
        val inlinedElement = ParadoxInlineService.getInlinedElement(element) ?: return
        withContextRecursionGuard(context, "ParadoxPsiSequenceBuilder.yieldInlineMember") {
            withRecursionCheck(inlinedElement) {
                if (inlinedElement is ParadoxScriptFile) {
                    yieldMembers(inlinedElement)
                    return
                }
                yieldMember(inlinedElement)
            }
        }
    }
}
