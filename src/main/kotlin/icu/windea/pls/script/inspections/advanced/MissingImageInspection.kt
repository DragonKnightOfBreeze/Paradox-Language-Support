package icu.windea.pls.script.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 缺失的图片的检查。
 * @property forPrimaryRelated 是否同样检查定义的主要的相关图片，默认为true。
 * @property forOptionalRelated 是否同样检查定义的可选的相关图片，默认为false。
 */
class MissingImageInspection: LocalInspectionTool() {
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
		
		private fun visitDefinition(definition: ParadoxDefinitionProperty, definitionInfo: ParadoxDefinitionInfo){
			val project = definition.project
			val imageInfos = definitionInfo.images
			if(imageInfos.isEmpty()) return
			val location = if(definition is ParadoxScriptProperty) definition.propertyKey else definition
			val nameToDistinct = mutableSetOf<String>()
			val nameMessageMap = mutableMapOf<String, String>()
			for(info in imageInfos) {
				if(nameToDistinct.contains(info.name)) continue
				if(info.required || (info.primary && inspection.forPrimaryRelated) || (!info.primary && inspection.forOptionalRelated)) {
					val resolved = info.locationExpression.resolve(definitionInfo.name, definition, project)
					if(resolved != null) {
						val (key, image) = resolved
						if(image == null) {
							//显示为WEAK_WARNING
							nameMessageMap.put(info.name, getMessage(info, key))
						} else {
							nameMessageMap.remove(info.name)
							nameToDistinct.add(info.name)
						}
					} else {
						nameMessageMap.put(info.name, getMessageFromExpression(info))
					}
				}
			}
			if(nameMessageMap.isNotEmpty()) {
				//显示为WEAK_WARNING
				holder.registerProblem(location, nameMessageMap.values.joinToString("\n"), ProblemHighlightType.WEAK_WARNING,
					ImportGameOrModDirectoryFix(definition)
				)
			}
		}
		
		private fun getMessage(info: ParadoxRelatedImageInfo, key: String): String {
			return when{
				info.required -> PlsBundle.message("script.inspection.advanced.missingImage.description.1", key)
				info.primary -> PlsBundle.message("script.inspection.advanced.missingImage.description.2", key)
				else -> PlsBundle.message("script.inspection.advanced.missingImage.description.3", key)
			}
		}
		
		private fun getMessageFromExpression(info: ParadoxRelatedImageInfo): String {
			return when{
				info.required -> PlsBundle.message("script.inspection.advanced.missingImage.description.1.1", info.locationExpression)
				info.primary -> PlsBundle.message("script.inspection.advanced.missingImage.description.2.1", info.locationExpression)
				else -> PlsBundle.message("script.inspection.advanced.missingImage.description.3.1", info.locationExpression)
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