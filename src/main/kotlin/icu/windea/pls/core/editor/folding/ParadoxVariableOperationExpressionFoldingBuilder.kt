package icu.windea.pls.core.editor.folding

import com.intellij.lang.*
import com.intellij.openapi.components.*
import com.intellij.openapi.editor.*
import icu.windea.pls.core.settings.*

class ParadoxVariableOperationExpressionFoldingBuilder : ParadoxExpressionFoldingBuilder() {
    object Data {
        const val GROUP_NAME = "variable_operation_expressions"
        val FOLDING_GROUP = FoldingGroup.newGroup(GROUP_NAME)
    }
    
    override fun getGroupName(): String {
        return Data.GROUP_NAME
    }
    
    override fun getFoldingGroup(): FoldingGroup? {
        return Data.FOLDING_GROUP
    }
    
    override fun getPlaceholderText(node: ASTNode): String {
        return ""
    }
    
    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return service<ParadoxFoldingSettings>().collapseVariableOperationExpressions
    }
}
