package icu.windea.pls.lang.editor.folding

import com.intellij.lang.*
import com.intellij.openapi.editor.*

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
        return ParadoxFoldingSettings.getInstance().variableOperationExpressions
    }
}
