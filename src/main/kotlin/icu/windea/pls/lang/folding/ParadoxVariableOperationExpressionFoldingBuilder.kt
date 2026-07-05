package icu.windea.pls.lang.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.settings.ChronicleSettings

class ParadoxVariableOperationExpressionFoldingBuilder : ParadoxExpressionFoldingBuilder() {
    object Constants {
        const val groupName = "variable_operation_expressions"
        val foldingGroup = FoldingGroup.newGroup(groupName)
    }

    override fun getGroupName(): String {
        return Constants.groupName
    }

    override fun getFoldingGroup(): FoldingGroup? {
        return Constants.foldingGroup
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return ""
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return ChronicleSettings.getInstance().state.folding.variableOperationExpressionsByDefault
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (!ChronicleSettings.getInstance().state.folding.variableOperationExpressions) return FoldingDescriptor.EMPTY_ARRAY

        return super.buildFoldRegions(root, document, quick)
    }
}
