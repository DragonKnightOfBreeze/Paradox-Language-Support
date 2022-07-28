package icu.windea.pls.script.inspections.advanced.expression

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.script.expression.*
import icu.windea.pls.script.psi.*

/**
 * 不正确的作用域表达式的检查。
 * 
 * 作用域表达式（scopeExpression）分为连接表达式（scopeLinkExpression）和字段表达式（scopeFieldExpression）。
 */
class IncorrectScopeExpressionInspection :LocalInspectionTool(){
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val project = file.project
		val fileInfo = file.fileInfo ?: return null
		val gameType = fileInfo.gameType
		val configGroup = getCwtConfig(project).getValue(gameType)
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		file.acceptChildren(object : PsiRecursiveElementVisitor() {
			override fun visitElement(e: PsiElement) {
				if(e is ParadoxScriptExpressionElement) {
					visitElementExpression(e)
				}
				super.visitElement(e)
			}
			
			private fun visitElementExpression(element: ParadoxScriptExpressionElement) {
				val config = element.getConfig() ?: return
				val type = config.expression.type
				if(type == CwtDataTypes.Scope || type == CwtDataTypes.ScopeField || type == CwtDataTypes.ScopeGroup){
					if(element.isQuoted()){
						defaultLevel
						holder.registerProblem(element, PlsBundle.message("script.inspection.expression.scope.quoted"), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
					} else {
						val value = element.value
						val expression = ParadoxScriptScopeExpression.resolve(value, configGroup)
						if(expression.isEmpty()){
							//无法解析
							holder.registerProblem(element, PlsBundle.message("script.inspection.expression.scope.malformed", value), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
						} else {
							for(error in expression.errors) {
								holder.registerScriptExpressionError(element, error)
							}
						}
					}
				}
			}
		})
		return holder.resultsArray
	}
}