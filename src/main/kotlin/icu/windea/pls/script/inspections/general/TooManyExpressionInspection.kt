package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.linker.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 定义声明中过多的表达式的检查。
 * @property firstOnly 是否仅标出第一个错误。
 * @property firstOnlyOnFile 在文件级别上，是否仅标出第一个错误。（默认为true，否则文件顶部的错误列可能会过多）
 */
class TooManyExpressionInspection: LocalInspectionTool() {
	@JvmField var firstOnly = false
	@JvmField var firstOnlyOnFile = true
	
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		file.accept(object : PsiRecursiveElementWalkingVisitor() {
			override fun visitElement(element: PsiElement) {
				if(element is ParadoxScriptBlock) visitBlock(element)
				if(element.isExpressionOrMemberContext()) super.visitElement(element)
			}
			
			override fun visitFile(file: PsiFile) {
				
				if(file !is ParadoxScriptFile) return
				//忽略可能的脚本片段入口
				if(!ParadoxScriptMemberElementLinker.canLink(file)) {
					doCheck(file, file)
				}
				super.visitFile(file)
			}
			
			private fun visitBlock(element: ParadoxScriptBlock) {
				ProgressManager.checkCanceled()
				//skip checking property if its property key may contain parameters
				//position: (in property) property key / (standalone) left curly brace
				val property = element.parent
					?.castOrNull<ParadoxScriptProperty>()
				//忽略可能的脚本片段入口
				if(property != null && ParadoxScriptMemberElementLinker.canLink(property)) return
				val position = property?.propertyKey
					?.also { if(it.isParameterAwareExpression()) return }
					?: element.findChild(ParadoxScriptElementTypes.LEFT_BRACE)
					?: return
				doCheck(element, position)
			}
			
			private fun doCheck(element: ParadoxScriptMemberElement, position: PsiElement) {
				val configs = ParadoxCwtConfigHandler.resolveConfigs(element, allowDefinitionSelf = true)
				if(skipCheck(configs)) return
				val occurrenceMap = ParadoxCwtConfigHandler.getChildOccurrenceMap(element, configs)
				if(occurrenceMap.isEmpty()) return
				occurrenceMap.forEach { (configExpression, occurrence) ->
					val r = doCheckOccurrence(element, position, occurrence, configExpression)
					if(!r) return
				}
			}
			
			private fun skipCheck(configs: List<CwtDataConfig<*>>): Boolean {
				if(configs.size <= 1) return true
				//可以精确匹配多个子句规则时，适用此检查
				if(configs.count { it is CwtPropertyConfig && it.valueExpression.type == CwtDataType.Block } > 1) return true
				if(configs.count { it is CwtValueConfig && it.valueExpression.type == CwtDataType.Block } > 1) return true
				return false
			}
			
			private fun doCheckOccurrence(element: ParadoxScriptMemberElement, position: PsiElement, occurrence: Occurrence, configExpression: CwtDataExpression): Boolean {
				val (actual, _, max) = occurrence
				if(max != null && actual > max) {
                    val isKey = configExpression is CwtKeyExpression
                    val isConst = configExpression.type == CwtDataType.Constant
                    val description = if(isKey) {
                        when {
                            isConst -> PlsBundle.message("inspection.script.general.tooManyExpression.description.1.1", configExpression)
                            else -> PlsBundle.message("inspection.script.general.tooManyExpression.description.1.2", configExpression)
                        }
                    } else {
                        when {
                            isConst -> PlsBundle.message("inspection.script.general.tooManyExpression.description.2.1", configExpression)
                            else -> PlsBundle.message("inspection.script.general.tooManyExpression.description.2.2", configExpression)
                        }
                    }
					val maxDefine = occurrence.maxDefine
					val detail = when {
						maxDefine == null -> PlsBundle.message("inspection.script.general.tooManyExpression.description.detail.1", max, actual)
						else -> PlsBundle.message("inspection.script.general.tooManyExpression.description.detail.2", max, actual, maxDefine)
					}
                    val highlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING
					val fileLevel = element is PsiFile
					if(!fileLevel && firstOnly && holder.hasResults()) return false
					if(fileLevel && firstOnlyOnFile && holder.hasResults()) return false
                    holder.registerProblem(position, "$description $detail", highlightType)
                }
				return true
			}
		})
		return holder.resultsArray
	}
	
	
	override fun createOptionsPanel(): JComponent {
		return panel {
			//firstOnly
			row {
				checkBox(PlsBundle.message("inspection.script.general.tooManyExpression.option.firstOnly"))
					.bindSelected(::firstOnly)
					.actionListener { _, component -> firstOnly = component.isSelected }
			}
			//firstOnlyOnFile
			row {
				checkBox(PlsBundle.message("inspection.script.general.tooManyExpression.option.firstOnlyOnFile"))
					.bindSelected(::firstOnlyOnFile)
					.actionListener { _, component -> firstOnlyOnFile = component.isSelected }
			}
		}
	}
}