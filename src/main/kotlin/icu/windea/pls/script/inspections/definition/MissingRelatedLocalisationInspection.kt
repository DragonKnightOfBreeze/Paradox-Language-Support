package icu.windea.pls.script.inspections.definition

import com.intellij.codeInsight.intention.HighPriorityAction
import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.inspections.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 缺失的相关本地化的检查。
 *
 * @property inspectOptional 是否同样检查可选的相关本地化，默认为false。
 */
class MissingRelatedLocalisationInspection : LocalInspectionTool(){
	@JvmField var inspectOptional = false
	
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(this, holder)
	}
	
	private class Visitor(
		private val inspection: MissingRelatedLocalisationInspection,
		private val holder: ProblemsHolder
	) : ParadoxScriptVisitor() {
		override fun visitFile(file: PsiFile) {
			val scriptFile = file.castOrNull<ParadoxScriptFile>() ?: return
			val definitionInfo = scriptFile.definitionInfo ?: return
			visitDefinition(scriptFile, definitionInfo)
		}
		
		override fun visitProperty(property: ParadoxScriptProperty) {
			val definitionInfo = property.definitionInfo ?: return
			visitDefinition(property, definitionInfo)
		}
		
		private fun visitDefinition(definition: ParadoxDefinitionProperty, definitionInfo: ParadoxDefinitionInfo){
			//TODO
		}
	}
	
	override fun createOptionsPanel(): JComponent {
		return panel { 
			row {
				checkBox(PlsBundle.message("script.inspection.definition.inspection.missingRelatedLocalisation.option.inspectOptional"))
					.bindSelected(::inspectOptional)
			}
		}
	}
	
	private class GenerateMissingLocalisationsFix(
		private val keys: Set<String>,
		element: ParadoxDefinitionProperty
	): LocalQuickFixAndIntentionActionOnPsiElement(element), HighPriorityAction{
		override fun getText() = PlsBundle.message("script.inspection.definition.inspection.missingRelatedLocalisation.quickfix.1")
		
		override fun getFamilyName() = text
		
		override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
			//TODO
		}
		
		override fun availableInBatchMode() = false
	}
}