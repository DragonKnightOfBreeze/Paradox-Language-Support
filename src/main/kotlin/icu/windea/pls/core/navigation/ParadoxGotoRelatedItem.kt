package icu.windea.pls.core.navigation

import com.intellij.navigation.*
import com.intellij.openapi.util.NlsContexts.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

@Suppress("UnstableApiUsage")
class ParadoxGotoRelatedItem(element: PsiElement, @Separator group: String) : GotoRelatedItem(element, group) {
	override fun getCustomName(): String? {
		val element = element
		if(element is ParadoxScriptProperty) {
			val definitionInfo = element.definitionInfo
			if(definitionInfo != null) return definitionInfo.name
		}
		return null
	}
	
	override fun getCustomContainerName(): String? {
		//所属文件的绝对路径+模组名（如果有）+模组版本（如果有）
		val element = element
		val file = element?.containingFile?.virtualFile ?: return null
		return buildString {
			val rootInfo = file.fileInfo?.rootInfo ?: return null
			append(file.path)
			when(rootInfo) {
				is ParadoxGameRootInfo -> {
					val launcherSettingsInfo = rootInfo.launcherSettingsInfo
					append(rootInfo.rootType.description).append("@").append(launcherSettingsInfo.version)
				}
				is ParadoxModRootInfo -> {
					val descriptorInfo = rootInfo.descriptorInfo
					append(descriptorInfo.name).append("@").append(descriptorInfo.version)
				}
			}
		}
	}
	
	companion object {
		fun createItems(elements: Collection<PsiElement>, @Separator group: String): List<ParadoxGotoRelatedItem> {
			return elements.mapTo(SmartList()) { ParadoxGotoRelatedItem(it, group) }
		}
	}
}