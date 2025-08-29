package icu.windea.pls.lang.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsFacade

class ParadoxVariableOperationExpressionFoldingBuilder : ParadoxExpressionFoldingBuilder() {
    object Constants {
        const val GROUP_NAME = "variable_operation_expressions"
        val FOLDING_GROUP = FoldingGroup.newGroup(GROUP_NAME)
    }

    override fun getGroupName(): String {
        return Constants.GROUP_NAME
    }

    override fun getFoldingGroup(): FoldingGroup? {
        return Constants.FOLDING_GROUP
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return ""
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return PlsFacade.getSettings().folding.variableOperationExpressionsByDefault
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (!PlsFacade.getSettings().folding.variableOperationExpressions) return FoldingDescriptor.EMPTY_ARRAY

        return super.buildFoldRegions(root, document, quick)
    }
}
