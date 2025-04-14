package icu.windea.pls.lang.editor.folding

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

class ParadoxScriptedVariableReferenceFoldingBuilder : FoldingBuilderEx() {
    object Constants {
        const val GROUP_NAME = "scripted_variable_references"
        val FOLDING_GROUP = FoldingGroup.newGroup(GROUP_NAME)
    }

    fun getGroupName(): String {
        return Constants.GROUP_NAME
    }

    fun getFoldingGroup(): FoldingGroup? {
        return Constants.FOLDING_GROUP
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return ""
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return getSettings().folding.scriptedVariableReferencesByDefault
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if(!getSettings().folding.scriptedVariableReferences) return FoldingDescriptor.EMPTY_ARRAY

        if (quick) return FoldingDescriptor.EMPTY_ARRAY
        if (!root.language.isParadoxLanguage()) return FoldingDescriptor.EMPTY_ARRAY
        val foldingGroup = getFoldingGroup()
        val allDescriptors = mutableListOf<FoldingDescriptor>()
        root.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptedVariableReference) visitScriptedVariableReference(element)
                //optimize performance
                when (element) {
                    is ParadoxScriptPropertyKey -> return
                    is ParadoxScriptBlock -> pass()
                    is ParadoxScriptValue -> return
                    is ParadoxScriptParameterConditionExpression -> return
                    is ParadoxLocalisationLocale -> return
                    is ParadoxLocalisationIcon -> return
                    is ParadoxLocalisationCommand -> return
                }
                super.visitElement(element)
            }

            private fun visitScriptedVariableReference(element: ParadoxScriptedVariableReference) {
                val referenceValue = element.referenceValue ?: return
                val resolvedValue = when {
                    element is ParadoxScriptScriptedVariableReference -> referenceValue.value
                    else -> referenceValue.value.unquote()
                }
                val descriptor = FoldingDescriptor(element.node, element.textRange, foldingGroup, resolvedValue)
                allDescriptors.add(descriptor)
            }
        })
        return allDescriptors.toTypedArray()
    }
}
