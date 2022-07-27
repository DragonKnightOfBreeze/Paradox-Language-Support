package icu.windea.pls.localisation.editor

import com.intellij.lang.documentation.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.util.*

class ParadoxLocalisationDocumentationProvider : AbstractDocumentationProvider() {
	override fun getDocumentationElementForLookupItem(psiManager: PsiManager?, `object`: Any?, element: PsiElement?): PsiElement? {
		if(`object` is PsiElement) return `object`
		return super.getDocumentationElementForLookupItem(psiManager, `object`, element)
	}
	
	override fun getDocumentationElementForLink(psiManager: PsiManager?, link: String?, context: PsiElement?): PsiElement? {
		if(link == null || context == null) return null
		return resolveScope(link, context)
	}
	
	override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
		return when {
			element is ParadoxLocalisationProperty -> getPropertyInfo(element)
			element is ParadoxLocalisationLocale -> getLocaleConfig(element)
			element is ParadoxLocalisationIcon -> getIconInfo(element)
			element is ParadoxLocalisationCommandScope -> getCommandScopeInfo(element)
			element is ParadoxLocalisationCommandField -> getCommandFieldInfo(element)
			element is ParadoxLocalisationColorfulText -> getColorConfig(element)
			else -> null
		}
	}
	
	private fun getPropertyInfo(element: ParadoxLocalisationProperty): String {
		val name = element.name
		val category = element.category
		if(category != null) return getLocalisationInfo(element, name, category)
		return buildString {
			buildPropertyDefinition(element)
		}
	}
	
	private fun getLocalisationInfo(element: ParadoxLocalisationProperty, name: String, category: ParadoxLocalisationCategory): String {
		return buildString {
			buildLocalisationDefinition(element, category, name)
		}
	}
	
	private fun getLocaleConfig(element: ParadoxLocalisationLocale): String {
		val name = element.name
		return buildString {
			buildLocaleDefinition(name)
		}
	}
	
	private fun getIconInfo(element: ParadoxLocalisationIcon): String? {
		val name = element.name ?: return null
		return buildString {
			buildIconDefinition(name)
		}
	}
	
	private fun getCommandScopeInfo(element: ParadoxLocalisationCommandScope): String {
		val name = element.name
		return buildString {
			buildCommandScopeDefinition(name)
		}
	}
	
	private fun getCommandFieldInfo(element: ParadoxLocalisationCommandField): String {
		val name = element.name
		return buildString {
			buildCommandFieldDefinition(name)
		}
	}
	
	private fun getColorConfig(element: ParadoxLocalisationColorfulText): String? {
		val name = element.name ?: return null
		return buildString {
			buildColorDefinition(name)
		}
	}
	
	override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
		return when {
			element is ParadoxLocalisationProperty -> getPropertyDoc(element)
			element is ParadoxLocalisationLocale -> getLocaleDoc(element)
			element is ParadoxLocalisationIcon -> getIconDoc(element)
			element is ParadoxLocalisationCommandScope -> getCommandScopeDoc(element)
			element is ParadoxLocalisationCommandField -> getCommandFieldDoc(element)
			element is ParadoxLocalisationColorfulText -> getColorDoc(element)
			else -> null
		}
	}
	
	private fun getPropertyDoc(element: ParadoxLocalisationProperty): String {
		val name = element.name
		val category = element.category
		if(category != null) return getLocalisationDoc(element, name, category)
		return buildString {
			buildPropertyDefinition(element)
		}
	}
	
	private fun getLocalisationDoc(element: ParadoxLocalisationProperty, name: String, category: ParadoxLocalisationCategory): String {
		return buildString {
			buildLocalisationDefinition(element, category, name)
			buildLineCommentContent(element)
			buildLocalisationSections(element)
		}
	}
	
	private fun getLocaleDoc(element: ParadoxLocalisationLocale): String {
		val name = element.name
		return buildString {
			buildLocaleDefinition(name)
		}
	}
	
	private fun getIconDoc(element: ParadoxLocalisationIcon): String? {
		val name = element.name ?: return null
		return buildString {
			buildIconDefinition(name)
		}
	}
	
	private fun getCommandScopeDoc(element: ParadoxLocalisationCommandScope): String {
		val name = element.name
		return buildString {
			buildCommandScopeDefinition(name)
		}
	}
	
	private fun getCommandFieldDoc(element: ParadoxLocalisationCommandField): String {
		val name = element.name
		return buildString {
			buildCommandFieldDefinition(name)
		}
	}
	
	private fun getColorDoc(element: ParadoxLocalisationColorfulText): String? {
		val name = element.name ?: return null
		return buildString {
			//加上元素定义信息
			buildColorDefinition(name)
		}
	}
	
	private fun StringBuilder.buildPropertyDefinition(element: ParadoxLocalisationProperty) {
		definition {
			//加上文件信息
			appendFileInfoHeader(element.fileInfo)
			//加上元素定义信息
			append(PlsDocBundle.message("name.localisation.property")).append(" <b>").append(element.name).append("</b>")
		}
	}
	
	private fun StringBuilder.buildLocalisationDefinition(element: ParadoxLocalisationProperty, category: ParadoxLocalisationCategory, name: String) {
		definition {
			//加上文件信息
			appendFileInfoHeader(element.fileInfo)
			//加上元素定义信息
			append(category.text).append(" <b>").append(name).append("</b>")
		}
	}
	
	private fun StringBuilder.buildLocalisationSections(element: ParadoxLocalisationProperty) {
		//加上渲染后的本地化文本
		if(getSettings().localisationRenderLocalisation) {
			val richText = ParadoxLocalisationTextRenderer.render(element)
			if(richText.isNotEmpty()) {
				sections {
					section(PlsDocBundle.message("title.text"), richText)
				}
			}
		}
	}
	
	private fun StringBuilder.buildLocaleDefinition(name: String) {
		definition {
			//加上元素定义信息
			append(PlsDocBundle.message("name.localisation.locale")).append(" <b>").append(name).append("</b>")
		}
	}
	
	private fun StringBuilder.buildIconDefinition(name: String) {
		definition {
			//加上元素定义信息
			append(PlsDocBundle.message("name.localisation.icon")).append(" <b>").append(name).append("</b>")
		}
	}
	
	private fun StringBuilder.buildCommandScopeDefinition(name: String) {
		definition {
			//加上元素定义信息
			append(PlsDocBundle.message("name.localisation.commandScope")).append(" <b>").append(name).append("</b>")
		}
	}
	
	private fun StringBuilder.buildCommandFieldDefinition(name: String) {
		definition {
			//加上元素定义信息
			append(PlsDocBundle.message("name.localisation.commandField")).append(" <b>").append(name).append("</b>")
		}
	}
	
	private fun StringBuilder.buildColorDefinition(name: String) {
		definition {
			//加上元素定义信息
			append(PlsDocBundle.message("name.localisation.color")).append(" <b>").append(name).append("</b>")
		}
	}
	
	private fun StringBuilder.buildLineCommentContent(element: PsiElement) {
		//加上单行注释文本
		if(getSettings().localisationRenderLineComment) {
			val docText = getLineCommentDocText(element)
			if(docText != null && docText.isNotEmpty()) {
				content {
					append(docText)
				}
			}
		}
	}
}
