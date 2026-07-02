package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isBlockMember
import icu.windea.pls.script.psi.isBlockValue

class ParadoxScriptValueRemover : ParadoxScriptUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        return when (e) {
            is ParadoxScriptBlock -> ChronicleBundle.message("script.remove.block")
            is ParadoxScriptValue -> ChronicleBundle.message("script.remove.value", e.name)
            else -> throw IllegalStateException()
        }
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxScriptValue && e.isBlockValue()
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.delete(element)
    }
}
