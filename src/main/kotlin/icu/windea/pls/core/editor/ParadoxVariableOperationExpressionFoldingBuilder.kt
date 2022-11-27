package icu.windea.pls.core.editor

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.components.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.core.settings.*

class ParadoxVariableOperationExpressionFoldingBuilder: FoldingBuilderEx() {
	override fun getPlaceholderText(node: ASTNode): String? {
		return null
	}
	
	override fun isCollapsedByDefault(node: ASTNode): Boolean {
		return service<ParadoxFoldingSettings>().collapseVariableOperationExpressions
	}
	
	override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
		TODO("Not yet implemented")
	}
}
