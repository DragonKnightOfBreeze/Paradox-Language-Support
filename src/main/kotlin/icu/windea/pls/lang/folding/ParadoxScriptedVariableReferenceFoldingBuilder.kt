package icu.windea.pls.lang.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationPsiUtil
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptPsiUtil

class ParadoxScriptedVariableReferenceFoldingBuilder : FoldingBuilderEx() {
    object Constants {
        const val GROUP_NAME = "scripted_variable_references"
        val FOLDING_GROUP = FoldingGroup.newGroup(GROUP_NAME)
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return ""
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return PlsSettings.getInstance().state.folding.scriptedVariableReferencesByDefault
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (quick) return FoldingDescriptor.EMPTY_ARRAY
        if (!PlsSettings.getInstance().state.folding.scriptedVariableReferences) return FoldingDescriptor.EMPTY_ARRAY
        val foldingGroup = Constants.FOLDING_GROUP
        val allDescriptors = mutableListOf<FoldingDescriptor>()
        root.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            var inInlineMath = false

            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptedVariableReference) visitScriptedVariableReference(element)
                // optimize performance
                if (element is ParadoxScriptInlineMath) {
                    inInlineMath = true
                }
                val r = when (element.language) {
                    ParadoxScriptLanguage -> inInlineMath || ParadoxScriptPsiUtil.isMemberContextElement(element)
                    ParadoxLocalisationLanguage -> ParadoxLocalisationPsiUtil.isRichTextContextElement(element)
                    else -> false
                }
                if (r) super.visitElement(element)
            }

            private fun visitScriptedVariableReference(element: ParadoxScriptedVariableReference) {
                val resolved = element.resolved() ?: return
                val value = resolved.value
                val descriptor = FoldingDescriptor(element.node, element.textRange, foldingGroup, value)
                allDescriptors.add(descriptor)
            }

            override fun elementFinished(element: PsiElement?) {
                if (element is ParadoxScriptInlineMath) {
                    inInlineMath = false
                }
            }
        })
        return allDescriptors.toTypedArray()
    }
}
