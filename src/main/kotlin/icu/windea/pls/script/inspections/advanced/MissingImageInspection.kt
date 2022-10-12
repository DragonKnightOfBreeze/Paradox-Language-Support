package icu.windea.pls.script.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.openapi.application.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 缺失的图片的检查。
 * @property forPrimaryRelated 是否同样检查定义的主要的相关图片，默认为true。
 * @property forOptionalRelated 是否同样检查定义的可选的相关图片，默认为false。
 */
class MissingImageInspection : LocalInspectionTool() {
	@JvmField var forPrimaryRelated = true
	@JvmField var forOptionalRelated = false
	
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(this, holder)
	}
	
	private class Visitor(
		private val inspection: MissingImageInspection,
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
			val imageInfos = definitionInfo.images
			if(imageInfos.isEmpty()) return
			val location = if(definition is ParadoxScriptProperty) definition.propertyKey else definition
			val nameToDistinct = mutableSetOf<String>()
			val infoMap = mutableMapOf<String, Tuple2<ParadoxRelatedImageInfo, String?>>()
			//进行代码检查时，规则文件中声明了多个不同名字的primaryLocalisation/primaryImage的场合，只要匹配其中一个名字的即可
			var hasPrimary = false
			runReadAction {
				for(info in imageInfos) {
					if(nameToDistinct.contains(info.name)) continue
					if(info.primary && hasPrimary) continue
					//多个位置表达式无法解析时，使用第一个
					if(info.required || if(info.primary) inspection.forPrimaryRelated else inspection.forOptionalRelated) {
						val resolved = info.locationExpression.resolve(definition, definitionInfo, project)
						if(resolved != null) {
							val (key, image) = resolved
							if(image == null) {
								infoMap.putIfAbsent(info.name, tupleOf(info, key))
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
		
		private fun getMessage(info: ParadoxRelatedImageInfo, key: String?): String {
			return if(key != null) {
				when {
					info.required -> PlsBundle.message("script.inspection.advanced.missingImage.description.1", key)
					info.primary -> PlsBundle.message("script.inspection.advanced.missingImage.description.2", key)
					else -> PlsBundle.message("script.inspection.advanced.missingImage.description.3", key)
				}
			} else {
				when {
					info.required -> PlsBundle.message("script.inspection.advanced.missingImage.description.1.1", info.locationExpression)
					info.primary -> PlsBundle.message("script.inspection.advanced.missingImage.description.2.1", info.locationExpression)
					else -> PlsBundle.message("script.inspection.advanced.missingImage.description.3.1", info.locationExpression)
				}
			}
		}
	}
	
	override fun createOptionsPanel(): JComponent {
		return panel {
			row {
				checkBox(PlsBundle.message("script.inspection.advanced.missingImage.option.checkPrimaryRelated"))
					.bindSelected(::forPrimaryRelated)
					.actionListener { _, component -> forPrimaryRelated = component.isSelected }
			}
			row {
				checkBox(PlsBundle.message("script.inspection.advanced.missingImage.option.checkOptionalRelated"))
					.bindSelected(::forOptionalRelated)
					.actionListener { _, component -> forOptionalRelated = component.isSelected }
			}
		}
	}
}