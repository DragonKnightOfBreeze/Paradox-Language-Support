package icu.windea.pls.script.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.codeInspection.ui.*
import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.util.ui.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.ext.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.handler.ParadoxCwtConfigHandler.resolveConfigs
import icu.windea.pls.core.model.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.core.ui.*
import icu.windea.pls.script.psi.*
import javax.swing.*
import javax.swing.event.*

/**
 * 缺失的本地化的检查。
 * @property locales 要检查的语言区域。默认检查英文。
 * @property checkPrimaryLocale 是否同样检查主要的语言区域。默认为true。
 * @property checkForDefinitions 是否检查定义。默认为true。
 * @property checkPrimaryForDefinitions 是否同样检查定义的主要的相关本地化，默认为true。
 * @property checkOptionalForDefinitions 是否同样检查定义的可选的相关本地化，默认为false。
 * @property checkForModifiers 是否检查修饰符。默认为false。
 */
@CwtInspection("CWT100")
class MissingLocalisationInspection : LocalInspectionTool() {
	@OptionTag(converter = CommaDelimitedStringListConverter::class)
	@JvmField var locales = listOf("l_english")
	@JvmField var checkForDefinitions = true
	@JvmField var checkPrimaryLocale = true
	@JvmField var checkPrimaryForDefinitions = true
	@JvmField var checkOptionalForDefinitions = false
	@JvmField var checkForModifiers = false
	
	private val localeList by lazy {
		val allLocales = getCwtConfig().core.localisationLocales
		locales.mapNotNullTo(mutableListOf()) { allLocales.get(it) }
	}
	private val localeSet by lazy {
		localeList.toMutableSet()
	}
	
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(this, holder)
	}
	
	private class Visitor(
		private val inspection: MissingLocalisationInspection,
		private val holder: ProblemsHolder
	) : ParadoxScriptVisitor() {
		override fun visitFile(file: PsiFile) {
			ProgressManager.checkCanceled()
			if(inspection.localeSet.isEmpty()) return
			if(!inspection.checkForDefinitions) return
			val scriptFile = file.castOrNull<ParadoxScriptFile>() ?: return
			val definitionInfo = scriptFile.definitionInfo ?: return
			visitDefinition(scriptFile, definitionInfo)
		}
		
		override fun visitProperty(property: ParadoxScriptProperty) {
			ProgressManager.checkCanceled()
			if(inspection.localeSet.isEmpty()) return
			if(!inspection.checkForDefinitions) return
			val definitionInfo = property.definitionInfo ?: return
			visitDefinition(property, definitionInfo)
		}
		
		private fun visitDefinition(definition: ParadoxDefinitionProperty, definitionInfo: ParadoxDefinitionInfo) {
			val project = definitionInfo.project
			val localisationInfos = definitionInfo.localisation
			if(localisationInfos.isEmpty()) return
			val location = if(definition is ParadoxScriptProperty) definition.propertyKey else definition
			ProgressManager.checkCanceled()
			val nameToDistinct = mutableSetOf<String>()
			val infoMap = mutableMapOf<String, Tuple3<ParadoxRelatedLocalisationInfo, String?, CwtLocalisationLocaleConfig>>()
			//进行代码检查时，规则文件中声明了多个不同名字的primaryLocalisation/primaryImage的场合，只要匹配其中一个名字的即可
			val hasPrimaryLocales = mutableSetOf<CwtLocalisationLocaleConfig>()
			runReadAction {
				for(info in localisationInfos) {
					if(info.required || if(info.primary) inspection.checkPrimaryForDefinitions else inspection.checkOptionalForDefinitions) {
						for(locale in inspection.localeSet) {
							if(nameToDistinct.contains(info.name + "@" + locale)) continue
							if(info.primary && hasPrimaryLocales.contains(locale)) continue
							//多个位置表达式无法解析时，使用第一个
							val selector = localisationSelector().gameTypeFrom(definition).locale(locale)
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
		
		private fun getMessage(info: ParadoxRelatedLocalisationInfo, key: String?, locale: CwtLocalisationLocaleConfig): String {
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
		
		override fun visitPropertyKey(element: ParadoxScriptPropertyKey) {
			visitExpressionElement(element)
		}
		
		override fun visitString(element: ParadoxScriptString) {
			visitExpressionElement(element)
		}
		
		override fun visitExpressionElement(element: ParadoxScriptExpressionElement) {
			ProgressManager.checkCanceled()
			if(inspection.localeSet.isEmpty()) return
			if(!inspection.checkForModifiers) return
			val config = resolveConfigs(element).firstOrNull() ?: return
			val configGroup = config.info.configGroup
			if(config.expression.type != CwtDataTypes.Modifier) return
			val name = element.value
			val keys = CwtConfigHandler.getModifierLocalisationNameKeys(name, configGroup) ?: return
			val missingLocales = mutableSetOf<CwtLocalisationLocaleConfig>()
			for(locale in inspection.localeSet) {
				val selector = localisationSelector().gameType(configGroup.gameType).preferRootFrom(element).locale(locale)
				//可以为全大写/全小写
				val localisation = keys.firstNotNullOfOrNull {
					findLocalisation(it, configGroup.project, selector = selector) ?: findLocalisation(it.uppercase(), configGroup.project, selector = selector)
				}
				if(localisation == null) missingLocales.add(locale)
			}
			if(missingLocales.isNotEmpty()) {
				for(locale in missingLocales) {
					val message = PlsBundle.message("script.inspection.advanced.missingLocalisation.description.4", name, locale)
					holder.registerProblem(element, message, ProblemHighlightType.WEAK_WARNING,
						ImportGameOrModDirectoryFix(element)
					)
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
					.bindSelected(::checkPrimaryLocale)
					.applyToComponent { toolTipText = PlsBundle.message("script.inspection.advanced.missingLocalisation.option.forPrimaryLocale.tooltip") }
					.actionListener { _, component -> checkPrimaryLocale = component.isSelected }
			}
			lateinit var checkForDefinitionsCb: Cell<JBCheckBox>
			row {
				checkBox(PlsBundle.message("script.inspection.advanced.missingLocalisation.option.checkForDefinitions"))
					.bindSelected(::checkForDefinitions)
					.actionListener { _, component -> checkForDefinitions = component.isSelected }
					.also { checkForDefinitionsCb = it }
			}
			indent {
				row {
					checkBox(PlsBundle.message("script.inspection.advanced.missingLocalisation.option.checkPrimaryForDefinitions"))
						.bindSelected(::checkPrimaryForDefinitions)
						.actionListener { _, component -> checkPrimaryForDefinitions = component.isSelected }
						.enabledIf(checkForDefinitionsCb.selected)
				}
				row {
					checkBox(PlsBundle.message("script.inspection.advanced.missingLocalisation.option.checkOptionalForDefinitions"))
						.bindSelected(::checkOptionalForDefinitions)
						.actionListener { _, component -> checkOptionalForDefinitions = component.isSelected }
						.enabledIf(checkForDefinitionsCb.selected)
				}
			}
			row {
				checkBox(PlsBundle.message("script.inspection.advanced.missingLocalisation.option.checkForModifiers"))
					.bindSelected(::checkForModifiers)
					.actionListener { _, component -> checkForModifiers = component.isSelected }
			}
		}
	}
	
	//com.intellij.codeInspection.suspiciousNameCombination.SuspiciousNameCombinationInspection.NameGroupsPanel
	
	private inner class LocaleTableModel(
		locales: List<CwtLocalisationLocaleConfig>
	) : AddEditDeleteListPanel<CwtLocalisationLocaleConfig>(PlsBundle.message("script.inspection.advanced.missingLocalisation.option.locales"), locales) {
		init {
			minimumSize = InspectionOptionsPanel.getMinimumListSize()
			preferredSize = JBUI.size(150, 110) //3行选项的高度
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
					localeSet.add(it)
				}
			}
			locales = newLocales
		}
		
		override fun findItemToAdd(): CwtLocalisationLocaleConfig? {
			val dialog = SelectParadoxLocaleDialog(null, localeList)
			if(dialog.showAndGet()) return dialog.locale
			return null
		}
		
		override fun editSelectedItem(item: CwtLocalisationLocaleConfig?): CwtLocalisationLocaleConfig? {
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
