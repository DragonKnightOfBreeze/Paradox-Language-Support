package icu.windea.pls.script.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.psi.*
import com.intellij.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.util.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.*
import icu.windea.pls.annotation.*
import icu.windea.pls.config.internal.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.core.ui.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*
import javax.swing.event.*

/**
 * 缺失的本地化的检查。
 *
 * @property locales 要检查的语言区域。默认检查英文。
 * @property forPrimaryLocale 是否同样检查主要的语言区域。默认为true。
 * @property forPrimaryRelated 是否同样检查定义的主要的相关本地化，默认为true。
 * @property forOptionalRelated 是否同样检查定义的可选的相关本地化，默认为false。
 */
@CwtInspection("CWT100")
class MissingLocalisationInspection : LocalInspectionTool() {
	@OptionTag(converter = CommaDelimitedStringListConverter::class)
	@JvmField var locales = listOf("l_english")
	@JvmField var forPrimaryLocale = true
	@JvmField var forPrimaryRelated = true
	@JvmField var forOptionalRelated = false
	
	private val localeList by lazy { locales.mapNotNullTo(SmartList()) { InternalConfigHandler.getLocale(it) } }
	private val localeSet by lazy { InternalConfigHandler.getLocales().filterTo(mutableSetOf()) { it.id in locales } }
	
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(this, holder)
	}
	
	private class Visitor(
		private val inspection: MissingLocalisationInspection,
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
		
		private fun visitDefinition(definition: ParadoxDefinitionProperty, definitionInfo: ParadoxDefinitionInfo) {
			val project = definition.project
			val localisationInfos = definitionInfo.localisation
			if(localisationInfos.isEmpty()) return
			val localeSet = inspection.localeSet
			if(localeSet.isEmpty()) return
			val location = if(definition is ParadoxScriptProperty) definition.propertyKey else definition
			val nameToDistinct = mutableSetOf<String>()
			for(info in localisationInfos) {
				if(info.required || (info.primary && inspection.forPrimaryRelated) || (!info.primary && inspection.forOptionalRelated)) {
					for(locale in localeSet) {
						if(nameToDistinct.contains(info.name + "@" + locale)) continue
						val resolved = info.locationExpression.resolve(definition.name, definition, locale, project)
						if(resolved != null) {
							val (key, loc) = resolved
							if(loc == null) {
								//显示为WEAK_WARNING
								holder.registerProblem(location, getMessage(info, key, locale), ProblemHighlightType.WEAK_WARNING)
							} else {
								nameToDistinct.add(info.name + "@" + locale)
							}
						} else {
							holder.registerProblem(location, getMessageFromExpression(info, locale), ProblemHighlightType.WEAK_WARNING)
						}
					}
				}
			}
		}
		
		private fun getMessage(info: ParadoxRelatedLocalisationInfo, key: String, locale: ParadoxLocaleConfig): String {
			return when {
				info.required -> PlsBundle.message("script.inspection.advanced.inspection.missingLocalisation.description.1", key, locale)
				info.primary -> PlsBundle.message("script.inspection.advanced.inspection.missingLocalisation.description.2", key, locale)
				else -> PlsBundle.message("script.inspection.advanced.inspection.missingLocalisation.description.3", key, locale)
			}
		}
		
		private fun getMessageFromExpression(info: ParadoxRelatedLocalisationInfo, locale: ParadoxLocaleConfig): String {
			return when {
				info.required -> PlsBundle.message("script.inspection.advanced.inspection.missingLocalisation.description.1", info.locationExpression, locale)
				info.primary -> PlsBundle.message("script.inspection.advanced.inspection.missingLocalisation.description.2", info.locationExpression, locale)
				else -> PlsBundle.message("script.inspection.advanced.inspection.missingLocalisation.description.3", info.locationExpression, locale)
			}
		}
	}
	
	override fun createOptionsPanel(): JComponent {
		return panel {
			row {
				cell(LocaleTableModel(localeList))
					.horizontalAlign(HorizontalAlign.FILL)
					.resizableColumn()
			}
			row {
				checkBox(PlsBundle.message("script.inspection.advanced.inspection.missingLocalisation.option.forPreferredLocale"))
					.bindSelected(::forPrimaryLocale)
					.applyToComponent { toolTipText = PlsBundle.message("script.inspection.advanced.inspection.missingLocalisation.option.forPrimaryLocale.tooltip") }
					.actionListener { _, component -> forPrimaryLocale = component.isSelected }
			}
			row {
				checkBox(PlsBundle.message("script.inspection.advanced.inspection.missingLocalisation.option.checkPrimaryRelated"))
					.bindSelected(::forPrimaryRelated)
					.actionListener { _, component -> forPrimaryRelated = component.isSelected }
			}
			row {
				checkBox(PlsBundle.message("script.inspection.advanced.inspection.missingLocalisation.option.checkOptionalRelated"))
					.bindSelected(::forOptionalRelated)
					.actionListener { _, component -> forOptionalRelated = component.isSelected }
			}
			//TODO 对于modifier trigger等的本地化（不确定是否总是存在）
		}
	}
	
	private inner class LocaleTableModel(
		locales: List<ParadoxLocaleConfig>
	) : AddEditDeleteListPanel<ParadoxLocaleConfig>(PlsBundle.message("script.inspection.advanced.inspection.missingLocalisation.option.locales"), locales) {
		init {
			myListModel.addListDataListener(object : ListDataListener {
				override fun intervalAdded(e: ListDataEvent?) {
					saveChanges()
				}
				
				override fun intervalRemoved(e: ListDataEvent?) {
					saveChanges()
				}
				
				override fun contentsChanged(e: ListDataEvent?) {
					saveChanges()
				}
			})
		}
		
		private fun saveChanges() {
			val newLocales = mutableListOf<String>()
			localeList.clear()
			localeSet.clear()
			for(i in 0 until myListModel.size) {
				myListModel.getElementAt(i)?.let {
					newLocales.add(it.id)
					localeList.add(it)
				}
			}
			locales = newLocales
			InternalConfigHandler.getLocales().filterTo(localeSet) { it.id in locales }
		}
		
		override fun findItemToAdd(): ParadoxLocaleConfig? {
			val dialog = SelectParadoxLocaleDialog(null, localeList)
			if(dialog.showAndGet()) return dialog.locale
			return null
		}
		
		override fun editSelectedItem(item: ParadoxLocaleConfig?): ParadoxLocaleConfig? {
			val dialog = SelectParadoxLocaleDialog(item, localeList)
			if(dialog.showAndGet()) return dialog.locale
			return item
		}
	}
	
	//private class GenerateMissingLocalisationFix(
	//	private val keys: Set<String>,
	//	element: ParadoxDefinitionProperty
	//): LocalQuickFixAndIntentionActionOnPsiElement(element), HighPriorityAction{
	//	override fun getText() = PlsBundle.message("script.inspection.definition.inspection.missingRelatedLocalisation.quickfix.1")
	//	
	//	override fun getFamilyName() = text
	//	
	//	override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
	//		//TODO
	//	}
	//	
	//	override fun availableInBatchMode() = false
	//}
}