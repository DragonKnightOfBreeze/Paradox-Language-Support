package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 缺失的图片的检查。
 * @property checkForDefinitions 是否检查定义。默认为true。
 * @property checkPrimaryForDefinitions 是否同样检查定义的主要的相关图片，默认为true。
 * @property checkOptionalForDefinitions 是否同样检查定义的可选的相关图片，默认为false。
 * @property checkForModifiers 是否检查修饰符（的图标）。默认为false。
 */
class MissingImageInspection : LocalInspectionTool() {
	@JvmField var checkForDefinitions = true
	@JvmField var checkPrimaryForDefinitions = false
	@JvmField var checkOptionalForDefinitions = false
	@JvmField var checkForModifiers = false
	
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(this, holder)
	}
	
	private class Visitor(
		private val inspection: MissingImageInspection,
		private val holder: ProblemsHolder
	) : ParadoxScriptVisitor() {
		override fun visitFile(file: PsiFile) {
			ProgressManager.checkCanceled()
			if(!inspection.checkForDefinitions) return
			val scriptFile = file.castOrNull<ParadoxScriptFile>() ?: return
			val definitionInfo = scriptFile.definitionInfo ?: return
			visitDefinition(scriptFile, definitionInfo)
		}
		
		override fun visitProperty(property: ParadoxScriptProperty) {
			ProgressManager.checkCanceled()
			if(!inspection.checkForDefinitions) return
			val definitionInfo = property.definitionInfo ?: return
			visitDefinition(property, definitionInfo)
		}
		
		private fun visitDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo) {
			val project = definitionInfo.project
			val imageInfos = definitionInfo.images
			if(imageInfos.isEmpty()) return
			val location = if(definition is ParadoxScriptProperty) definition.propertyKey else definition
			ProgressManager.checkCanceled()
			val nameToDistinct = mutableSetOf<String>()
			val infoMap = mutableMapOf<String, Tuple2<ParadoxDefinitionRelatedImageInfo, String?>>()
			//进行代码检查时，规则文件中声明了多个不同名字的primaryLocalisation/primaryImage的场合，只要匹配其中一个名字的即可
			var hasPrimary = false
			runReadAction {
				for(info in imageInfos) {
					if(nameToDistinct.contains(info.name)) continue
					if(info.primary && hasPrimary) continue
					//多个位置表达式无法解析时，使用第一个
					if(info.required || if(info.primary) inspection.checkPrimaryForDefinitions else inspection.checkOptionalForDefinitions) {
						val resolved = info.locationExpression.resolve(definition, definitionInfo, project)
						if(resolved != null) {
							if(resolved.message != null) continue //skip if it's dynamic
							if(resolved.file == null) {
								infoMap.putIfAbsent(info.name, tupleOf(info, resolved.filePath))
							} else {
								infoMap.remove(info.name)
								nameToDistinct.add(info.name)
								if(info.primary) hasPrimary = true
							}
						} else if(info.locationExpression.placeholder == null) {
							//从定义的属性推断，例如，#name
							infoMap.putIfAbsent(info.name, tupleOf(info, null))
						}
					}
				}
			}
			if(infoMap.isNotEmpty()) {
				//显示为WEAK_WARNING，且缺失多个时，每个算作一个问题
				for((info, key) in infoMap.values) {
					val message = getMessage(info, key)
					holder.registerProblem(location, message, ProblemHighlightType.WEAK_WARNING,
						ImportGameOrModDirectoryFix(definition)
					)
				}
			}
		}
		
		private fun getMessage(info: ParadoxDefinitionRelatedImageInfo, key: String?): String {
			val expression = info.locationExpression
			val propertyName = expression.propertyName
			return when {
				info.required -> when {
					key != null -> PlsBundle.message("inspection.script.general.missingImage.description.1.1", key)
					propertyName != null -> PlsBundle.message("inspection.script.general.missingImage.description.1.2", propertyName)
					else -> PlsBundle.message("inspection.script.general.missingImage.description.1.3", expression)
				}
				info.primary -> when {
					key != null -> PlsBundle.message("inspection.script.general.missingImage.description.2.1", key)
					propertyName != null -> PlsBundle.message("inspection.script.general.missingImage.description.2.2", propertyName)
					else -> PlsBundle.message("inspection.script.general.missingImage.description.2.3", expression)
				}
				else -> when {
					key != null -> PlsBundle.message("inspection.script.general.missingImage.description.3.1", key)
					propertyName != null -> PlsBundle.message("inspection.script.general.missingImage.description.3.2", propertyName)
					else -> PlsBundle.message("inspection.script.general.missingImage.description.3.3", expression)
				}
			}
		}
		
		override fun visitPropertyKey(element: ParadoxScriptPropertyKey) {
			visitStringExpressionElement(element)
		}
		
		override fun visitString(element: ParadoxScriptString) {
			visitStringExpressionElement(element)
		}
		
		private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
			ProgressManager.checkCanceled()
			if(!inspection.checkForModifiers) return
			val config = ParadoxCwtConfigHandler.resolveConfigs(element).firstOrNull() ?: return
			val configGroup = config.info.configGroup
			if(config.expression.type != CwtDataType.Modifier) return
			val name = element.value
			val iconPaths = ParadoxModifierHandler.getModifierIconPaths(name, configGroup)
			val iconFile = iconPaths.firstNotNullOfOrNull {
				val iconSelector = fileSelector().gameType(configGroup.gameType)
				ParadoxFilePathSearch.search(it, configGroup.project, selector = iconSelector).find()
			}
			if(iconFile == null) {
				val message = PlsBundle.message("inspection.script.general.missingImage.description.4", name)
				holder.registerProblem(element, message, ProblemHighlightType.WEAK_WARNING,
					ImportGameOrModDirectoryFix(element)
				)
			}
		}
	}
	
	override fun createOptionsPanel(): JComponent {
		return panel {
			lateinit var checkForDefinitionsCb: Cell<JBCheckBox>
			row {
				checkBox(PlsBundle.message("inspection.script.general.missingImage.option.checkForDefinitions"))
					.bindSelected(::checkForDefinitions)
					.actionListener { _, component -> checkForDefinitions = component.isSelected }
					.also { checkForDefinitionsCb = it }
			}
			indent {
				row {
					checkBox(PlsBundle.message("inspection.script.general.missingImage.option.checkPrimaryForDefinitions"))
						.bindSelected(::checkPrimaryForDefinitions)
						.actionListener { _, component -> checkPrimaryForDefinitions = component.isSelected }
						.enabledIf(checkForDefinitionsCb.selected)
				}
				row {
					checkBox(PlsBundle.message("inspection.script.general.missingImage.option.checkOptionalForDefinitions"))
						.bindSelected(::checkOptionalForDefinitions)
						.actionListener { _, component -> checkOptionalForDefinitions = component.isSelected }
						.enabledIf(checkForDefinitionsCb.selected)
				}
			}
			row {
				checkBox(PlsBundle.message("inspection.script.general.missingImage.option.checkForModifiers"))
					.bindSelected(::checkForModifiers)
					.actionListener { _, component -> checkForModifiers = component.isSelected }
			}
		}
	}
}
