package icu.windea.pls.lang.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationPsiUtil
import icu.windea.pls.script.ParadoxScriptLanguage
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
        return PlsFacade.getSettings().folding.scriptedVariableReferencesByDefault
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (quick) return FoldingDescriptor.EMPTY_ARRAY
        if (!PlsFacade.getSettings().folding.scriptedVariableReferences) return FoldingDescriptor.EMPTY_ARRAY
        val foldingGroup = Constants.FOLDING_GROUP
        val allDescriptors = mutableListOf<FoldingDescriptor>()
        root.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptedVariableReference) visitScriptedVariableReference(element)
                //optimize performance
                val r = when (element.language) {
                    ParadoxScriptLanguage -> ParadoxScriptPsiUtil.isMemberContextElement(element)
                    ParadoxLocalisationLanguage -> ParadoxLocalisationPsiUtil.isRichTextContextElement(element)
                    else -> false
                }
                if (r) super.visitElement(element)
            }

            private fun visitScriptedVariableReference(element: ParadoxScriptedVariableReference) {
                val referenceValue = element.resolved()?.scriptedVariableValue ?: return
                val resolvedValue = referenceValue.value
                val descriptor = FoldingDescriptor(element.node, element.textRange, foldingGroup, resolvedValue)
                allDescriptors.add(descriptor)
            }
        })
        return allDescriptors.toTypedArray()
    }
}
