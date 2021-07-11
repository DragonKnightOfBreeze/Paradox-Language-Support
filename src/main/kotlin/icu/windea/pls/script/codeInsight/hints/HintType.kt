package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

@Suppress("UnstableApiUsage")
enum class HintType(private val showDesc: String, defaultEnabled: Boolean) {
	DEFINITION_NAME_TYPE_HINT(
		message("paradox.script.hints.types.definition"),
		true
	) {
		override fun provideHints(elem: PsiElement): List<InlayInfo> {
			return (elem as? ParadoxScriptProperty)?.let { element ->
				element.definitionInfo?.let { definitionInfo ->
					val result = mutableListOf<InlayInfo>()
					//提示定义的名字类型信息
					val text1 = buildString {
						val name = definitionInfo.name.ifEmpty { anonymousString }
						val typeText = definitionInfo.typeText
						append(name).append(": ").append(typeText)
					}
					val offset1 = elem.propertyKey.endOffset
					result.add(InlayInfo(text1, offset1))
					//提示定义的本地化名字（对应的localisation.name为name或title）
					definitionInfo.localisation.find { 
						val name = it.name.lowercase()
						name == "name" || name == "title"
					}?.keyName?.let { keyName ->
						findLocalisation(keyName, inferParadoxLocale(), elem.project, hasDefault = true)?.let { locProp ->
							val text2 = locProp.extractText()
							val offset2 = elem.propertyKey.endOffset
							result.add(InlayInfo(text2, offset2))
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