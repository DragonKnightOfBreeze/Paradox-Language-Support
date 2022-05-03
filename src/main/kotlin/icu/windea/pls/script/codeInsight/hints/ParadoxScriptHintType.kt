package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

@Suppress("UnstableApiUsage")
enum class ParadoxScriptHintType(private val showDesc: String, defaultEnabled: Boolean) {
	DEFINITION_HINT(
		PlsBundle.message("script.hints.types.definition"),
		true
	) {
		override fun provideHints(elem: PsiElement): List<InlayInfo> {
			return (elem as? ParadoxScriptProperty)?.let { element ->
				element.definitionInfo?.let { definitionInfo ->
					val result = mutableListOf<InlayInfo>()
					//提示定义的名字类型信息
					val text = buildString {
						val name = definitionInfo.name.ifEmpty { anonymousString }
						val typeText = definitionInfo.typesText
						append(name).append(": ").append(typeText)
					}
					val offset = elem.propertyKey.endOffset
					result.add(InlayInfo(text, offset))
					//提示定义的本地化名字（对应的localisation.name为name或title）
					val primaryLocalisationConfigs = definitionInfo.primaryLocalisationConfig
					for(primaryLocalisationConfig in primaryLocalisationConfigs) {
						val resolved = primaryLocalisationConfig.locationExpression.resolve(definitionInfo.name, inferParadoxLocale(), elem.project)
						val localisation = resolved.second
						if(localisation != null){
							val localizedName = localisation.extractText()
							result.add(InlayInfo(localizedName, offset))
							break
						}
					}
					result
				}
			}.orEmpty()
		}
		
		override fun isApplicable(elem: PsiElement): Boolean {
			return elem is ParadoxScriptProperty && elem.definitionInfo != null
		}
	};
	
	companion object {
		val values = values()
		
		fun resolve(elem: PsiElement): ParadoxScriptHintType? {
			val applicableTypes = values.filter { it.isApplicable(elem) }
			return applicableTypes.firstOrNull()
		}
		
		fun resolveToEnabled(elem: PsiElement?): ParadoxScriptHintType? {
			val resolved = elem?.let { resolve(it) } ?: return null
			return if(resolved.enabled) resolved else null
		}
	}
	
	abstract fun isApplicable(elem: PsiElement): Boolean
	abstract fun provideHints(elem: PsiElement): List<InlayInfo>
	
	val option = Option("SHOW_${this.name}", { this.showDesc }, defaultEnabled)
	val enabled get() = option.get()
}