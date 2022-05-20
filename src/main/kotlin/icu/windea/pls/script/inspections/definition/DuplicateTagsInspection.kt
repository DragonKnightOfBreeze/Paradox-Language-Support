package icu.windea.pls.script.inspections.definition

import com.intellij.codeInspection.*
import com.intellij.psi.*
import com.intellij.util.SmartList
import icu.windea.pls.*
import icu.windea.pls.annotation.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

/**
 * 同一定义中重复的标签的检查。
 * @since stellaris v3.4
 */
@NearlyUselessInspection
class DuplicateTagsInspection : LocalInspectionTool() {
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
		return Visitor(holder)
	}
	
	private class Visitor(private val holder: ProblemsHolder) : ParadoxScriptVisitor() {
		override fun visitProperty(element: ParadoxScriptProperty) {
			val tagConfigs = element.resolveTagConfigs() ?: return
			if(tagConfigs.isEmpty()) return
			val blockElement = element.propertyValue?.value.castOrNull<ParadoxScriptBlock>() ?: return
			val tagGroup: MutableMap<String, MutableList<ParadoxScriptString>> = mutableMapOf()
			run {
				blockElement.forEachChild {
					if(it is ParadoxScriptString) {
						val tagConfig = it.doResolveTagConfig(tagConfigs)
						if(tagConfig != null) {
							tagGroup.getOrPut(tagConfig.name){ SmartList() }.add(it)
						}
					}
					if(it is ParadoxScriptVariable || it is ParadoxScriptProperty || it is ParadoxScriptValue) return@run
				}
			}
			if(tagGroup.isEmpty()) return
			for((name, tags) in tagGroup){
				if(tags.size <= 1) continue
				for(tag in tags) {
					val location = tag
					holder.registerProblem(location, PlsBundle.message("script.inspection.definition.duplicateTags.description", name))
				}
			}
		}
	}
}