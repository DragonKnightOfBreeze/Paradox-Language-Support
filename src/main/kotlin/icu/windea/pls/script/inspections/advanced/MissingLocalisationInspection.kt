package icu.windea.pls.script.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.codeInspection.ui.*
import com.intellij.psi.*
import com.intellij.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.util.*
import com.intellij.util.ui.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.*
import icu.windea.pls.annotations.*
import icu.windea.pls.config.internal.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.core.selector.*
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
			val project = definitionInfo.project
			val localisationInfos = definitionInfo.localisation
			if(localisationInfos.isEmpty()) return
			val localeSet = inspection.localeSet
			if(localeSet.isEmpty()) return
			val location = if(definition is ParadoxScriptProperty) definition.propertyKey else definition
			val nameToDistinct = mutableSetOf<String>()
			val infoMap = mutableMapOf<String, Tuple3<ParadoxRelatedLocalisationInfo, String?, ParadoxLocaleConfig>>()
			//进行代码检查时，规则文件中声明了多个不同名字的primaryLocalisation/primaryImage的场合，只要匹配其中一个名字的即可
			val hasPrimaryLocales = mutableSetOf<ParadoxLocaleConfig>()
			for(info in localisationInfos) {
				if(info.required || if(info.primary) inspection.forPrimaryRelated else inspection.forOptionalRelated) {
					for(locale in localeSet) {
						if(nameToDistinct.contains(info.name + "@" + locale)) continue
						if(info.primary && hasPrimaryLocales.contains(locale)) continue
						//多个位置表达式无法解析时，使用第一个
						val selector = localisationSelector().gameTypeFrom(definition).preferRootFrom(definition).locale(locale)
						val resolved = info.locationExpression.resolve(definition, definitionInfo, project, selector = selector)
						if(resolved != null) {
							val (key, loc) = resolved
							if(loc == null) {
								infoMap.putIfAbsent(info.name + "@" + locale, tupleOf(info, key, locale))
							} else {
								infoMap.remove(info.name + "@" + locale)
								nameToDistinct.add(info.name + "@" + locale)
								if(info.primary) hasPrimaryLocales.add(locale)
							}
						} else if(info.locationExpression.placeholder == null) {
							//从定义的属性推断，例如，#name
							infoMap.putIfAbsent(info.name + "@" + locale, tupleOf(info, null, locale))
						}
					}
				}
			}
			if(infoMap.isNotEmpty()) {
				//显示为WEAK_WARNING，且缺失多个时，每个算作一个问题
				for((info, key, locale) in infoMap.values) {
					val message = getMessage(info, key, locale)
					holder.registerProblem(location, message, ProblemHighlightType.WEAK_WARNING,
						ImportGameOrModDirectoryFix(definition)
					)
				}
			}
		}
		
		private fun getMessage(info: ParadoxRelatedLocalisationInfo, key: String?, locale: ParadoxLocaleConfig): String {
			return if(key != null) {
				when {
					info.required -> PlsBundle.message("script.inspection.advanced.missingLocalisation.description.1", key, locale)
					info.primary -> PlsBundle.message("script.inspection.advanced.missingLocalisation.description.2", key, locale)
					else -> PlsBundle.message("script.inspection.advanced.missingLocalisation.description.3", key, locale)
				}
			} else {
				val expression = info.locationExpression
				when {
					info.required -> PlsBundle.message("script.inspection.advanced.missingLocalisation.description.1.1", expression, locale)
					info.primary -> PlsBundle.message("script.inspection.advanced.missingLocalisation.description.2.1", expression, locale)
					else -> PlsBundle.message("script.inspection.advanced.missingLocalisation.description.3.1", expression, locale)
				}
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
				checkBox(PlsBundle.message("script.inspection.advanced.missingLocalisation.option.forPreferredLocale"))
					.bindSelected(::forPrimaryLocale)
					.applyToComponent { toolTipText = PlsBundle.message("script.inspection.advanced.missingLocalisation.option.forPrimaryLocale.tooltip") }
					.actionListener { _, component -> forPrimaryLocale = component.isSelected }
			}
			row {
				checkBox(PlsBundle.message("script.inspection.advanced.missingLocalisation.option.checkPrimaryRelated"))
					.bindSelected(::forPrimaryRelated)
					.actionListener { _, component -> forPrimaryRelated = component.isSelected }
			}
			row {
				checkBox(PlsBundle.message("script.inspection.advanced.missingLocalisation.option.checkOptionalRelated"))
					.bindSelected(::forOptionalRelated)
					.actionListener { _, component -> forOptionalRelated = component.isSelected }
			}
			//TODO 对于modifier trigger等的本地化（不确定是否总是存在）
		}
	}
	
	//com.intellij.codeInspection.suspiciousNameCombination.SuspiciousNameCombinationInspection.NameGroupsPanel
	
	private inner class LocaleTableModel(
		locales: List<ParadoxLocaleConfig>
	) : AddEditDeleteListPanel<ParadoxLocaleConfig>(PlsBundle.message("script.inspection.advanced.missingLocalisation.option.locales"), locales) {
		init {
			minimumSize = InspectionOptionsPanel.getMinimumListSize()
			preferredSize = JBUI.size(150, 110) //2行选项的高度
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