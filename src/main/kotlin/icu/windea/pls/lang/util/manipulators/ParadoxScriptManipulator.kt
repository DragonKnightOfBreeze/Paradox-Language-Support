package icu.windea.pls.lang.util.manipulators

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.core.children
import icu.windea.pls.ep.resolve.ParadoxInlineSupport
import icu.windea.pls.lang.util.dataFlow.ParadoxMemberSequence
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMemberElement
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition
import icu.windea.pls.lang.util.dataFlow.ParadoxDataFlowOptions.Member as MemberOptions

object ParadoxScriptManipulator {
    fun buildMemberSequence(file: PsiFile): ParadoxMemberSequence {
        val options = MemberOptions()
        val delegate = doBuildMemberSequence(file, options)
        return ParadoxMemberSequence(delegate, options)
    }

    fun buildMemberSequence(blockElement: PsiElement): ParadoxMemberSequence {
        val options = MemberOptions()
        val delegate = doBuilderMemberSequence(blockElement, options)
        return ParadoxMemberSequence(delegate, options)
    }

    private fun doBuildMemberSequence(file: PsiFile, options: MemberOptions): Sequence<ParadoxScriptMemberElement> {
        if (file !is ParadoxScriptFile) return emptySequence()
        return sequence b@{
            val blockElement = file.block ?: return@b
            doYieldMembers(blockElement, options)
        }
    }

    private fun doBuilderMemberSequence(blockElement: PsiElement, options: MemberOptions): Sequence<ParadoxScriptMemberElement> {
        return sequence {
            doYieldMembers(blockElement, options)
        }
    }

    private suspend fun SequenceScope<ParadoxScriptMemberElement>.doYieldMembers(element: PsiElement, options: MemberOptions) {
        element.children(options.forward).forEach {
            if (it is ParadoxScriptMemberElement) doYieldMember(it, options)
            if (options.conditional && it is ParadoxScriptParameterCondition) doYieldMembers(it, options)
        }
    }

    private suspend fun SequenceScope<ParadoxScriptMemberElement>.doYieldMember(element: ParadoxScriptMemberElement, options: MemberOptions) {
        yield(element)
        if (options.inline) {
            val inlined = ParadoxInlineSupport.getInlinedElement(element)
            val finalInlined = when {
                inlined is ParadoxScriptFile -> inlined.block
                else -> inlined
            }
            if (finalInlined != null) doYieldMembers(finalInlined, options)
        }
    }
}
