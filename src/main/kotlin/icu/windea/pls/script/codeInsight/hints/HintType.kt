package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

@Suppress("UnstableApiUsage")
enum class HintType(private val showDesc: String, defaultEnabled: Boolean) {
	DEFINITION_NAME_TYPE_HINT(
		message("paradox.script.hints.definition.definitionNameType"),
		true
	) {
		override fun provideHints(elem: PsiElement): List<InlayInfo> {
			return (elem as? ParadoxScriptProperty)?.let { element ->
				element.paradoxDefinitionInfo?.let { definitionInfo ->
					val text = buildString {
						val name = definitionInfo.name.ifEmpty { anonymousString }
						val typeText = definitionInfo.typeText
						append("(definition) <b>").append(name.escapeXml()).append("</b>: ").append(typeText)
					}
					val offset = elem.propertyKey.endOffset
					val inlayInfo = InlayInfo(text, offset)
					listOf(inlayInfo)
				}
			} ?: emptyList()
		}
		
		override fun isApplicable(elem: PsiElement): Boolean {
			return elem is ParadoxScriptProperty && elem.paradoxDefinitionInfo != null
		}
	},
	
	/**
	 * 定义的本地化的名字的提示。在cwt配置文件中对应的`localisation`的key为`name`（不区分大小写）的本地化文本。
	 */
	DEFINITION_LOCALIZED_NAME_HINT(
		message("paradox.script.hints.definition.definitionLocalizedName"),
		true
	) {
		override fun provideHints(elem: PsiElement): List<InlayInfo> {
			return (elem as? ParadoxScriptProperty)?.let { element ->
				element.paradoxDefinitionInfo?.let { definitionInfo ->
					definitionInfo.localisation.find { it.name.toLowerCase() == "name" }?.keyName?.let { keyName ->
						findLocalisation(keyName, inferParadoxLocale(), elem.project, hasDefault = true)?.let { locProp ->
							val text = locProp.extractText()
							val offset = elem.propertyKey.endOffset
							val inlayInfo = InlayInfo(text, offset)
							listOf(inlayInfo)
						}
					}
				}
			} ?: emptyList()
		}
		
		override fun isApplicable(elem: PsiElement): Boolean {
			return elem is ParadoxScriptProperty && elem.paradoxDefinitionInfo != null
		}
	};
	
	companion object {
		val values = values()
		
		fun resolve(elem: PsiElement): HintType? {
			val applicableTypes = values.filter { it.isApplicable(elem) }
			return applicableTypes.firstOrNull()
		}
		
		fun resolveToEnabled(elem: PsiElement?): HintType? {
			val resolved = elem?.let { resolve(it) } ?: return null
			return if(resolved.enabled) resolved else null
		}
	}
	
	abstract fun isApplicable(elem: PsiElement): Boolean
	abstract fun provideHints(elem: PsiElement): List<InlayInfo>
	val option = Option("SHOW_${this.name}", { this.showDesc }, defaultEnabled)
	val enabled get() = option.get()
}