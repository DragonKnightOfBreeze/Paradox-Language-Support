package icu.windea.pls.lang.util.manipulators

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.inline.*
import icu.windea.pls.script.psi.*

object ParadoxScriptManipulator {
    private val defaultMemberSequenceOptions = ParadoxScriptMemberSequenceOptions()

    fun buildMemberSequence(file: PsiFile, options: ParadoxScriptMemberSequenceOptions = defaultMemberSequenceOptions): Sequence<ParadoxScriptMemberElement> {
        if (file !is ParadoxScriptFile) return emptySequence()
        val blockElement = file.block ?: return emptySequence()
        return buildMemberSequence(blockElement, options)
    }

    fun buildMemberSequence(blockElement: PsiElement, options: ParadoxScriptMemberSequenceOptions = defaultMemberSequenceOptions): Sequence<ParadoxScriptMemberElement> {
        return sequence {
            doYieldMembers(blockElement, options)
        }
    }

    private suspend fun SequenceScope<ParadoxScriptMemberElement>.doYieldMembers(element: PsiElement, options: ParadoxScriptMemberSequenceOptions) {
        element.children().forEach {
            if (it is ParadoxScriptMemberElement) doYieldMember(it, options)
            if (options.conditional && it is ParadoxScriptParameterCondition) doYieldMembers(it, options)
        }
    }

    private suspend fun SequenceScope<ParadoxScriptMemberElement>.doYieldMember(element: ParadoxScriptMemberElement, options: ParadoxScriptMemberSequenceOptions) {
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
