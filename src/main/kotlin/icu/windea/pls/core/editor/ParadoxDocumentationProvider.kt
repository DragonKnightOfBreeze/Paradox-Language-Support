@file:Suppress("UnusedReceiverParameter")

package icu.windea.pls.core.editor

import com.intellij.codeInsight.documentation.*
import com.intellij.lang.documentation.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.support.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*
import icu.windea.pls.tool.localisation.*

@Suppress("UNUSED_PARAMETER")
class ParadoxDocumentationProvider : AbstractDocumentationProvider() {
	override fun getDocumentationElementForLookupItem(psiManager: PsiManager?, `object`: Any?, element: PsiElement?): PsiElement? {
		if(`object` is PsiElement) return `object`
		return super.getDocumentationElementForLookupItem(psiManager, `object`, element)
	}
	
	override fun getDocumentationElementForLink(psiManager: PsiManager?, link: String?, context: PsiElement?): PsiElement? {
		if(link == null || context == null) return null
		return resolveLink(link, context)
	}
	
	//这里为RenameableFakePsiElement，也就是那些实际上没有声明处的PsiElement，提供快速文档
	
	override fun getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement?): String? {
		return when(element) {
			is ParadoxParameterElement -> getParameterInfo(element, originalElement)
			//is ParadoxArgument -> getParameterInfo(element, originalElement)
			//is ParadoxParameter -> getParameterInfo(element, originalElement)
			is ParadoxValueSetValueElement -> getValueSetValueInfo(element, originalElement)
			is ParadoxScriptStringExpressionElement -> {
				//TODO 直接基于ParadoxValueSetValueElement进行索引，从而下面的代码不再需要
				val config = ParadoxCwtConfigHandler.resolveConfigs(element).firstOrNull()
				if(config != null && config.expression.type.isValueSetValueType()) {
					return getValueSetValueInfo(element, originalElement)
				}
				null
			}
			is ParadoxModifierElement -> getModifierInfo(element, originalElement)
			else -> null
		}
	}
	
	//private fun getParameterInfo(element: PsiElement, originalElement: PsiElement?): String? {
	//	val resolved = element.references.firstOrNull()?.resolve() as? ParadoxParameterElement ?: return null
	//	return getParameterInfo(resolved, originalElement)
	//}
	
	private fun getParameterInfo(element: ParadoxParameterElement, originalElement: PsiElement?): String {
		return buildString {
			buildParameterDefinition(element)
		}
	}
	
	private fun getValueSetValueInfo(element: PsiElement, originalElement: PsiElement?): String? {
		val resolved = element.references.firstOrNull()?.resolve() as? ParadoxValueSetValueElement ?: return null
		return getValueSetValueInfo(resolved, originalElement)
	}
	
	private fun getValueSetValueInfo(element: ParadoxValueSetValueElement, originalElement: PsiElement?): String {
		val name = element.name
		val valueSetNames = element.valueSetNames
		val configGroup = getCwtConfig(element.project).getValue(element.gameType)
		return buildString {
			buildValueSetValueDefinition(name, valueSetNames, configGroup)
		}
	}
	
	private fun getModifierInfo(element: ParadoxModifierElement, originalElement: PsiElement?): String {
		return buildString {
			buildModifierDefinition(element, null)
		}
	}
	
	override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
		return when(element) {
			is ParadoxParameterElement -> getParameterDoc(element, originalElement)
			//is ParadoxArgument -> getParameterDoc(element, originalElement)
			//is ParadoxParameter -> getParameterDoc(element, originalElement)
			is ParadoxValueSetValueElement -> getValueSetValueDoc(element, originalElement)
			is ParadoxScriptStringExpressionElement -> {
				val config = ParadoxCwtConfigHandler.resolveConfigs(element).firstOrNull()
				if(config != null && config.expression.type.isValueSetValueType()) {
					return getValueSetValueDoc(element, originalElement)
				}
				null
			}
			is ParadoxModifierElement -> getModifierDoc(element, originalElement)
			else -> null
		}
	}
	
	//private fun getParameterDoc(element: PsiElement, originalElement: PsiElement?): String? {
	//	val resolved = element.reference?.resolve() as? ParadoxParameterElement ?: return null
	//	return getParameterInfo(resolved, originalElement)
	//}
	
	private fun getParameterDoc(element: ParadoxParameterElement, originalElement: PsiElement?): String {
		return buildString {
			buildParameterDefinition(element)
		}
	}
	
	private fun getValueSetValueDoc(element: PsiElement, originalElement: PsiElement?): String? {
		val resolved = element.references.firstOrNull()?.resolve() as? ParadoxValueSetValueElement ?: return null
		return getValueSetValueInfo(resolved, originalElement)
	}
	
	private fun getValueSetValueDoc(element: ParadoxValueSetValueElement, originalElement: PsiElement?): String {
		val name = element.name
		val valueSetNames = element.valueSetNames
		val configGroup = getCwtConfig(element.project).getValue(element.gameType)
		return buildString {
			buildValueSetValueDefinition(name, valueSetNames, configGroup)
		}
	}
	
	private fun getModifierDoc(element: ParadoxModifierElement, originalElement: PsiElement?): String {
		return buildString {
			val sectionsList = List(3) { mutableMapOf<String, String>() }
			buildModifierDefinition(element, sectionsList)
			buildSections(sectionsList)
		}
	}
	
	private fun StringBuilder.buildParameterDefinition(element: ParadoxParameterElement) {
		val name = element.name
		definition {
			val r = ParadoxParameterSupport.getDocumentationDefinition(element, this)
			if(!r) {
				//显示默认的快速文档
				append(PlsDocBundle.message("prefix.parameter")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
			}
		}
	}
	
	private fun StringBuilder.buildValueSetValueDefinition(name: String, valueSetNames: List<String>, configGroup: CwtConfigGroup) {
		definition {
			//不加上文件信息
			append(PlsDocBundle.message("prefix.valueSetValue")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
			append(": ")
			var appendSeparator = false
			for(valueSetName in valueSetNames) {
				if(appendSeparator) append(" | ") else appendSeparator = true
				val valueConfig = configGroup.values[valueSetName]
				if(valueConfig != null) {
					val gameType = configGroup.gameType
					val typeLink = "${gameType.id}/values/${valueSetName}"
					appendCwtLink(valueSetName, typeLink)
				} else {
					append(valueSetName)
				}
			}
		}
	}
	
	private fun StringBuilder.buildModifierDefinition(element: ParadoxModifierElement, sectionsList: List<MutableMap<String, String>>?) {
		val name = element.name
		definition {
			val r = ParadoxModifierSupport.getDocumentationDefinition(element, this)
			if(!r) {
				//显示默认的快速文档
				append(PlsDocBundle.message("prefix.modifier")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
			}
			
			val configGroup = getCwtConfig(element.project).getValue(element.gameType)
			addModifierRelatedLocalisations(element, name, configGroup, sectionsList?.get(2))
			
			addModifierIcon(element, name, configGroup, sectionsList?.get(1))
			addModifierScope(element, name, configGroup, sectionsList?.get(0))
			addScopeContext(element, name, configGroup, sectionsList?.get(0))
		}
	}
	
	private fun StringBuilder.addModifierRelatedLocalisations(
		element: ParadoxModifierElement,
		name: String,
		configGroup: CwtConfigGroup,
		sections: MutableMap<String, String>?
	) {
		val render = getSettings().documentation.renderRelatedLocalisationsForModifiers
		ProgressManager.checkCanceled()
		val gameType = element.gameType
		val nameKeys = ParadoxModifierHandler.getModifierNameKeys(name, configGroup)
		val localisation = nameKeys.firstNotNullOfOrNull {
			val selector = localisationSelector().gameType(gameType).preferRootFrom(element).preferLocale(preferredParadoxLocale())
			ParadoxLocalisationSearch.search(it, configGroup.project, selector = selector).find()
		}
		val descKeys = ParadoxModifierHandler.getModifierDescKeys(name, configGroup)
		val descLocalisation = descKeys.firstNotNullOfOrNull {
			val descSelector = localisationSelector().gameType(gameType).preferRootFrom(element).preferLocale(preferredParadoxLocale())
			ParadoxLocalisationSearch.search(it, configGroup.project, selector = descSelector).find()
		}
		//如果没找到的话，不要在文档中显示相关信息
		if(localisation != null) {
			appendBr()
			append(PlsDocBundle.message("prefix.relatedLocalisation")).append(" ")
			append("Name = ").appendLocalisationLink(gameType, localisation.name, element, resolved = true)
		}
		if(descLocalisation != null) {
			appendBr()
			append(PlsDocBundle.message("prefix.relatedLocalisation")).append(" ")
			append("Desc = ").appendLocalisationLink(gameType, descLocalisation.name, element, resolved = true)
		}
		if(sections != null && render) {
			if(localisation != null) {
				val richText = ParadoxLocalisationTextRenderer.render(localisation)
				sections.put("Name", richText)
			}
			if(descLocalisation != null) {
				val richText = ParadoxLocalisationTextRenderer.render(descLocalisation)
				sections.put("Desc", richText)
			}
		}
	}
	
	private fun StringBuilder.addModifierIcon(
		element: ParadoxModifierElement,
		name: String,
		configGroup: CwtConfigGroup,
		sections: MutableMap<String, String>?
	) {
		val render = getSettings().documentation.renderIconForModifiers
		ProgressManager.checkCanceled()
		val gameType = element.gameType
		val iconPaths = ParadoxModifierHandler.getModifierIconPaths(name, configGroup)
		val (iconPath, iconFile) = iconPaths.firstNotNullOfOrNull {
			val iconSelector = fileSelector().gameType(gameType).preferRootFrom(element)
			it to ParadoxFilePathSearch.search(it, configGroup.project, selector = iconSelector).find()
		} ?: (null to null)
		//如果没找到的话，不要在文档中显示相关信息
		if(iconPath != null && iconFile != null) {
			appendBr()
			append(PlsDocBundle.message("prefix.relatedImage")).append(" ")
			append("Icon = ").appendFilePathLink(iconPath, gameType, iconPath, element, resolved = true)
		}
		if(sections != null && render) {
			if(iconFile != null) {
				val url = ParadoxDdsUrlResolver.resolveByFile(iconFile)
				sections.put("Icon", buildString { appendImgTag(url) })
			}
		}
	}
	
	private fun StringBuilder.addModifierScope(
		element: ParadoxModifierElement,
		name: String,
		configGroup: CwtConfigGroup,
		sections: MutableMap<String, String>?
	) {
		//即使是在CWT文件中，如果可以推断得到CWT规则组，也显示作用域信息
		if(!getSettings().documentation.showScopes) return
		
		if(sections != null) {
			val modifierCategories = ParadoxModifierSupport.getModifierCategories(element) ?: return
			val gameType = configGroup.gameType
			val contextElement = element
			val categoryNames = modifierCategories.keys
			if(categoryNames.isNotEmpty()) {
				sections.put(PlsDocBundle.message("sectionTitle.categories"), getCategoriesText(categoryNames, gameType, contextElement))
			}
			
			val supportedScopes = modifierCategories.getSupportedScopes()
			sections.put(PlsDocBundle.message("sectionTitle.supportedScopes"), getScopesText(supportedScopes, gameType, contextElement))
		}
	}
	
	private fun getCategoriesText(categories: Set<String>, gameType: ParadoxGameType?, contextElement: PsiElement): String {
		return buildString {
			var appendSeparator = false
			append("<code>")
			for(category in categories) {
				if(appendSeparator) append(", ") else appendSeparator = true
				appendCwtLink(category, "${gameType.id}/modifier_categories/$category", contextElement)
			}
			append("</code>")
		}
	}
	
	private fun getScopeText(scope: String, gameType: ParadoxGameType?, contextElement: PsiElement): String {
		return buildString {
			append("<code>")
			if(ParadoxScopeHandler.isFakeScopeId(scope)) {
				append(scope)
			} else {
				appendCwtLink(scope, "${gameType.id}/scopes/$scope", contextElement)
			}
			append("</code>")
		}
	}
	
	private fun getScopesText(scopes: Set<String>, gameType: ParadoxGameType?, contextElement: PsiElement): String {
		return buildString {
			var appendSeparator = false
			append("<code>")
			for(scope in scopes) {
				if(appendSeparator) append(", ") else appendSeparator = true
				if(ParadoxScopeHandler.isFakeScopeId(scope)) {
					append(scope)
				} else {
					appendCwtLink(scope, "${gameType.id}/scopes/$scope", contextElement)
				}
			}
			append("</code>")
		}
	}
	
	private fun StringBuilder.addScopeContext(
		element: ParadoxModifierElement,
		name: String,
		configGroup: CwtConfigGroup,
		sections: MutableMap<String, String>?
	) {
		//进行代码提示时不应当显示作用域上下文信息
		@Suppress("DEPRECATION")
		if(DocumentationManager.IS_FROM_LOOKUP.get(element) == true) return
		
		val show = getSettings().documentation.showScopeContext
		if(!show) return
		if(sections == null) return
		val memberElement = element.parentOfType<ParadoxScriptMemberElement>(true) ?: return
		if(!ParadoxScopeHandler.isScopeContextSupported(memberElement)) return
		val scopeContext = ParadoxScopeHandler.getScopeContext(memberElement)
		if(scopeContext == null) return
		//TODO 如果作用域引用位于表达式中，应当使用那个位置的作用域上下文，但是目前实现不了，因为这里的referenceElement是整个scriptProperty
		val scopeContextToUse = scopeContext
		val contextElement = element
		val gameType = configGroup.gameType
		val scopeContextText = buildString {
			var appendSeparator = false
			scopeContextToUse.map.forEach { (systemLink, scope) ->
				if(appendSeparator) appendBr() else appendSeparator = true
				appendCwtLink(systemLink, "${gameType.id}/system_links/$systemLink", contextElement)
				append(" = ")
				if(ParadoxScopeHandler.isFakeScopeId(scope)) {
					append(scope)
				} else {
					appendCwtLink(scope, "${gameType.id}/scopes/$scope", contextElement)
				}
			}
		}
		sections.put(PlsDocBundle.message("sectionTitle.scopeContext"), "<code>$scopeContextText</code>")
	}
	
	private fun StringBuilder.buildSections(sectionsList: List<Map<String, String>>) {
		sections {
			for(sections in sectionsList) {
				for((key, value) in sections) {
					section(key, value)
				}
			}
		}
	}
	
	private fun getReferenceElement(originalElement: PsiElement?): PsiElement? {
		val element = when {
			originalElement == null -> return null
			originalElement.elementType == TokenType.WHITE_SPACE -> originalElement.prevSibling ?: return null
			else -> originalElement
		}
		return when {
			element is LeafPsiElement -> element.parent
			else -> element
		}
	}
}