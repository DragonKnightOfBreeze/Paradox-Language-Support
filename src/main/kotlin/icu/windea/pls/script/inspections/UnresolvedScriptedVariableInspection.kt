package icu.windea.pls.script.inspections

import com.intellij.codeInsight.intention.*
import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.quickFix.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.reference.*
import org.jetbrains.kotlin.idea.util.application.*

private const val i = 1

/**
 * 无法解析的封装变量引用的检查。
 *
 * 提供快速修复：
 * * 声明本地变量（在同一文件中）
 * * TODO 声明全局变量（在`common/scripted_variables`目录下的某一文件中）
 * * 导入游戏目录或模组目录
 */
class UnresolvedScriptedVariableInspection : LocalInspectionTool() {
	companion object {
		private val variableNameKey = Key.create<String>("variableName")
		private val variableReferenceKey = Key.create<ParadoxScriptVariableReferenceReference>("variableReference")
		private val parentDefinitionKey = Key.create<ParadoxScriptProperty>("parentDefinition")
	}
	
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
		return Visitor(holder, session)
	}
	
	private class Visitor(private val holder: ProblemsHolder, private val session: LocalInspectionToolSession) : ParadoxScriptVisitor() {
		override fun visitVariableReference(element: ParadoxScriptVariableReference) {
			val reference = element.reference
			if(reference.resolve() != null) return
			val quickFixList = SmartList<LocalQuickFix>()
			if(element.isWritable) {
				element.name.apply { session.putUserData(variableNameKey, this) }
				reference.apply { session.putUserData(variableReferenceKey, this) }
				element.findParentDefinition()
					?.castOrNull<ParadoxScriptProperty>()
					?.apply { session.putUserData(parentDefinitionKey, this) }
					?.apply {
						quickFixList.add(IntroduceLocalVariableFix(element, session))
						quickFixList.add(IntroduceGlobalVariableFix(element, session))
					}
			}
			quickFixList.add(ImportGameOrModDirectoryFix(element))
			val quickFixes = quickFixList.toTypedArray()
			
			val location = element
			holder.registerProblem(location, PlsBundle.message("script.inspection.unresolvedScriptedVariable.description", element.name), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL, *quickFixes)
		}
	}
	
	private class IntroduceLocalVariableFix(
		element: ParadoxScriptVariableReference,
		private val session: LocalInspectionToolSession
	) : LocalQuickFixAndIntentionActionOnPsiElement(element), HighPriorityAction {
		private val variableName get() = session.getUserData(variableNameKey)!!
		private val variableReference get() = session.getUserData(variableReferenceKey)!!
		private val parentDefinition get() = session.getUserData(parentDefinitionKey)!!
		
		override fun getFamilyName() = PlsBundle.message("script.inspection.unresolvedScriptedVariable.fix.1", variableName)
		
		override fun getText() = PlsBundle.message("script.inspection.unresolvedScriptedVariable.fix.1", variableName)
		
		override fun getPriority() = PriorityAction.Priority.TOP
		
		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			//在所属定义之前另起一行（跳过注释和空白），声明对应名字的封装变量，默认值给0并选中
			runWriteAction {
				introduceScriptedVariable(variableName, "0", parentDefinition, project, editor) { newVariable, editor ->
					val textRange = newVariable.variableValue!!.textRange
					editor.selectionModel.setSelection(textRange.startOffset, textRange.endOffset)
					editor.caretModel.moveToOffset(textRange.endOffset)
					editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
				}
			}
		}
	}
	
	private class IntroduceGlobalVariableFix(
		element: ParadoxScriptVariableReference,
		private val session: LocalInspectionToolSession
	) : LocalQuickFixAndIntentionActionOnPsiElement(element), HighPriorityAction {
		private val variableName get() = session.getUserData(variableNameKey)!!
		private val variableReference get() = session.getUserData(variableReferenceKey)!!
		private val parentDefinition get() = session.getUserData(parentDefinitionKey)!!
		
		override fun getFamilyName() = PlsBundle.message("script.inspection.unresolvedScriptedVariable.fix.2", variableName)
		
		override fun getText() = PlsBundle.message("script.inspection.unresolvedScriptedVariable.fix.2", variableName)
		
		override fun getPriority() = PriorityAction.Priority.HIGH
		
		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			//TODO
		}
	}
}

