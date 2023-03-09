@file:Suppress("UNUSED_PARAMETER", "UnusedReceiverParameter")

package icu.windea.pls.cwt.editor

import com.intellij.lang.documentation.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selectors.chained.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*
import icu.windea.pls.tool.localisation.*
import java.util.*

class CwtDocumentationProvider : AbstractDocumentationProvider() {
    override fun getDocumentationElementForLookupItem(psiManager: PsiManager?, `object`: Any?, element: PsiElement?): PsiElement? {
        if(`object` is PsiElement) return `object`
        return super.getDocumentationElementForLookupItem(psiManager, `object`, element)
    }
    
    override fun getDocumentationElementForLink(psiManager: PsiManager?, link: String?, context: PsiElement?): PsiElement? {
        if(link == null || context == null) return null
        return resolveLink(link, context)
    }
    
    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        return when(element) {
            is CwtProperty -> getPropertyInfo(element, originalElement)
            is CwtString -> getStringInfo(element, originalElement)
            else -> null
        }
    }
    
    private fun getPropertyInfo(element: CwtProperty, originalElement: PsiElement?): String {
        return buildString {
            val name = element.name
            val configType = element.configType
            val project = element.project
            val configGroup = getConfigGroup(element, originalElement, project)
            buildPropertyOrStringDefinition(element, originalElement, name, configType, configGroup, false, null)
        }
    }
    
    private fun getStringInfo(element: CwtString, originalElement: PsiElement?): String? {
        //only for property value or block value
        if(!element.isPropertyValue() && !element.isBlockValue()) return null
        
        return buildString {
            val name = element.name
            val configType = element.configType
            val project = element.project
            val configGroup = getConfigGroup(element, originalElement, project)
            buildPropertyOrStringDefinition(element, originalElement, name, configType, configGroup, false, null)
        }
    }
    
    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        return when(element) {
            is CwtProperty -> getPropertyDoc(element, originalElement)
            is CwtString -> getStringDoc(element, originalElement)
            else -> null
        }
    }
    
    private fun getPropertyDoc(element: CwtProperty, originalElement: PsiElement?): String {
        return buildString {
            val name = element.name
            val configType = element.configType
            val project = element.project
            val configGroup = getConfigGroup(element, originalElement, project)
            //images, localisations, scope infos
            val sectionsList = List(3) { mutableMapOf<String, String>() }
            buildPropertyOrStringDefinition(element, originalElement, name, configType, configGroup, true, sectionsList)
            buildDocumentationContent(element)
            buildSections(sectionsList)
        }
    }
    
    private fun getStringDoc(element: CwtString, originalElement: PsiElement?): String? {
        //only for property value or block value
        if(!element.isPropertyValue() && !element.isBlockValue()) return null
        
        return buildString {
            val name = element.name
            val configType = element.configType
            val project = element.project
            val configGroup = getConfigGroup(element, originalElement, project)
            val sectionsList = List(2) { mutableMapOf<String, String>() }
            buildPropertyOrStringDefinition(element, originalElement, name, configType, configGroup, true, sectionsList)
            buildDocumentationContent(element)
            buildSections(sectionsList)
        }
    }
    
    private fun StringBuilder.buildPropertyOrStringDefinition(
        element: PsiElement,
        originalElement: PsiElement?,
        name: String,
        configType: CwtConfigType?,
        configGroup: CwtConfigGroup?,
        showDetail: Boolean,
        sectionsList: List<MutableMap<String, String>>?
    ) {
        definition {
            val referenceElement = getReferenceElement(originalElement)
            val shortName = configType?.getShortName(name) ?: name
            val byName = if(shortName == name) null else name
            val prefix = when {
                configType?.isReference == true -> configType.prefix
                referenceElement is ParadoxScriptPropertyKey -> PlsDocBundle.message("prefix.definitionProperty")
                referenceElement is ParadoxScriptValue -> PlsDocBundle.message("prefix.definitionValue")
                else -> configType?.prefix
            }
            val typeCategory = configType?.category
            
            if(prefix != null) {
                append(prefix).append(" ")
            }
            append("<b>").append(shortName.escapeXml().orAnonymous()).append("</b>")
            if(typeCategory != null) {
                val typeElement = element.parentOfType<CwtProperty>()
                val typeName = typeElement?.name?.substringIn('[', ']')?.takeIfNotEmpty()
                if(typeName != null && typeName.isNotEmpty()) {
                    //在脚本文件中显示为链接
                    if(configGroup != null) {
                        val gameType = configGroup.gameType
                        val typeLink = "${gameType.id}/${typeCategory}/${typeName}"
                        append(": ").appendCwtLink(typeName, typeLink, typeElement)
                    } else {
                        append(": ").append(typeName)
                    }
                }
            }
            if(byName != null) {
                grayed {
                    append(" by ").append(byName.escapeXml().orAnonymous())
                }
            }
            
            if(configGroup != null) {
                if(referenceElement != null && configType == CwtConfigType.Modifier) {
                    addModifierRelatedLocalisations(element, referenceElement, name, configGroup, sectionsList?.get(2))
                    addModifierIcon(element, referenceElement, name, configGroup, sectionsList?.get(1))
                }
                if(element is CwtProperty) {
                    addScope(element, name, configType, configGroup, sectionsList?.get(0))
                }
                if(referenceElement != null) {
                    addScopeContext(element, referenceElement, configGroup, sectionsList?.get(0))
                }
            }
        }
    }
    
    private fun StringBuilder.addModifierRelatedLocalisations(element: PsiElement, referenceElement: PsiElement, name: String, configGroup: CwtConfigGroup, sections: MutableMap<String, String>?) {
        val render = getSettings().documentation.renderRelatedLocalisationsForModifiers
        ProgressManager.checkCanceled()
        val contextElement = referenceElement
        val gameType = configGroup.gameType ?: return
        val project = configGroup.project
        val nameKeys = ParadoxModifierHandler.getModifierNameKeys(name)
        val localisation = nameKeys.firstNotNullOfOrNull {
            val selector = localisationSelector(project, contextElement).contextSensitive().preferLocale(preferredParadoxLocale())
            ParadoxLocalisationSearch.search(it, selector).find()
        }
        val descKeys = ParadoxModifierHandler.getModifierDescKeys(name)
        val descLocalisation = descKeys.firstNotNullOfOrNull {
            val descSelector = localisationSelector(project, contextElement).contextSensitive().preferLocale(preferredParadoxLocale())
            ParadoxLocalisationSearch.search(it, descSelector).find()
        }
        //如果没找到的话，不要在文档中显示相关信息
        if(localisation != null) {
            appendBr()
            append(PlsDocBundle.message("prefix.relatedLocalisation")).append(" ")
            append("Name = ").appendLocalisationLink(gameType, localisation.name, contextElement, resolved = true)
        }
        if(descLocalisation != null) {
            appendBr()
            append(PlsDocBundle.message("prefix.relatedLocalisation")).append(" ")
            append("Desc = ").appendLocalisationLink(gameType, descLocalisation.name, contextElement, resolved = true)
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
    
    private fun StringBuilder.addModifierIcon(element: PsiElement, referenceElement: PsiElement, name: String, configGroup: CwtConfigGroup, sections: MutableMap<String, String>?) {
        val render = getSettings().documentation.renderIconForModifiers
        ProgressManager.checkCanceled()
        val contextElement = referenceElement
        val gameType = configGroup.gameType ?: return
        val project = configGroup.project
        val iconPaths = ParadoxModifierHandler.getModifierIconPaths(name)
        val (iconPath, iconFile) = iconPaths.firstNotNullOfOrNull {
            val iconSelector = fileSelector(project, contextElement).contextSensitive()
            it to ParadoxFilePathSearch.search(it, null, iconSelector).find()
        } ?: (null to null)
        //如果没找到的话，不要在文档中显示相关信息
        if(iconPath != null && iconFile != null) {
            appendBr()
            append(PlsDocBundle.message("prefix.relatedImage")).append(" ")
            append("Icon = ").appendFilePathLink(iconPath, gameType, iconPath, contextElement, resolved = true)
        }
        if(sections != null && render) {
            if(iconFile != null) {
                val url = ParadoxDdsUrlResolver.resolveByFile(iconFile)
                sections.put("Icon", buildString { appendImgTag(url) })
            }
        }
    }
    
    private fun StringBuilder.addScope(element: CwtProperty, name: String, configType: CwtConfigType?, configGroup: CwtConfigGroup, sections: MutableMap<String, String>?) {
        //即使是在CWT文件中，如果可以推断得到CWT规则组，也显示作用域信息
        
        if(!getSettings().documentation.showScopes) return
        //为link提示名字、描述、输入作用域、输出作用域的文档注释
        //为alias modifier localisation_command等提供分类、支持的作用域的文档注释
        //仅为脚本文件和本地化文件中的引用提供
        val contextElement = element
        val gameType = configGroup.gameType
        when(configType) {
            CwtConfigType.Link -> {
                val linkConfig = configGroup.links[name] ?: return
                val descToUse = linkConfig.desc?.takeIfNotEmpty()
                if(descToUse != null) {
                    content { appendBr().append(descToUse) }
                }
                if(sections != null) {
                    val inputScopes = linkConfig.inputScopes
                    sections.put(PlsDocBundle.message("sectionTitle.inputScopes"), getScopesText(inputScopes, gameType, contextElement))
                    
                    val outputScope = linkConfig.outputScope
                    if(outputScope != null) sections.put(PlsDocBundle.message("sectionTitle.outputScopes"), getScopeText(outputScope, gameType, contextElement))
                }
            }
            CwtConfigType.LocalisationLink -> {
                val linkConfig = configGroup.localisationLinks[name] ?: return
                val descToUse = linkConfig.desc?.takeIfNotEmpty()
                if(descToUse != null) {
                    content { appendBr().append(descToUse) }
                }
                if(sections != null) {
                    val inputScopes = linkConfig.inputScopes
                    sections.put(PlsDocBundle.message("sectionTitle.inputScopes"), getScopesText(inputScopes, gameType, contextElement))
                    
                    val outputScope = linkConfig.outputScope
                    if(outputScope != null) sections.put(PlsDocBundle.message("sectionTitle.outputScopes"), getScopeText(outputScope, gameType, contextElement))
                }
            }
            CwtConfigType.Modifier -> {
                val modifierConfig = configGroup.modifiers[name] ?: return
                if(sections != null) {
                    val categoryNames = modifierConfig.categoryConfigMap.keys
                    if(categoryNames.isNotEmpty()) {
                        sections.put(PlsDocBundle.message("sectionTitle.categories"), getCategoriesText(categoryNames, gameType, contextElement))
                    }
                    
                    val supportedScopes = modifierConfig.supportedScopes
                    sections.put(PlsDocBundle.message("sectionTitle.supportedScopes"), getScopesText(supportedScopes, gameType, contextElement))
                }
            }
            CwtConfigType.ModifierCategory -> {
                val modifierCategoryConfig = configGroup.modifierCategories[name] ?: return
                if(sections != null) {
                    val supportedScopes = modifierCategoryConfig.supportedScopes
                    sections.put(PlsDocBundle.message("sectionTitle.supportedScopes"), getScopesText(supportedScopes, gameType, contextElement))
                }
            }
            CwtConfigType.LocalisationCommand -> {
                val localisationCommandConfig = configGroup.localisationCommands[name] ?: return
                if(sections != null) {
                    val supportedScopes = localisationCommandConfig.supportedScopes
                    sections.put(PlsDocBundle.message("sectionTitle.supportedScopes"), getScopesText(supportedScopes, gameType, contextElement))
                }
            }
            CwtConfigType.Alias, CwtConfigType.Trigger, CwtConfigType.Effect -> {
                val (aliasName, aliasSubName) = name.removeSurroundingOrNull("alias[", "]")?.split(':') ?: return
                val aliasConfigGroup = configGroup.aliasGroups[aliasName] ?: return
                val aliasConfigs = aliasConfigGroup[aliasSubName] ?: return
                val aliasConfig = aliasConfigs.singleOrNull()
                    ?: aliasConfigs.find { element.isSamePosition(it.pointer.element) }
                    ?: return
                if(aliasConfig.name !in configGroup.aliasNamesSupportScope) return
                if(sections != null) {
                    val supportedScopes = aliasConfig.supportedScopes
                    sections.put(PlsDocBundle.message("sectionTitle.supportedScopes"), getScopesText(supportedScopes, gameType, contextElement))
                    
                    val outputScope = aliasConfig.outputScope
                    if(outputScope != null) sections.put(PlsDocBundle.message("sectionTitle.outputScopes"), getScopeText(outputScope, gameType, contextElement))
                }
            }
            else -> pass()
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
    
    private fun getScopeText(scopeId: String, gameType: ParadoxGameType?, contextElement: PsiElement): String {
        return buildString {
            append("<code>")
            ParadoxScopeHandler.buildScopeDoc(scopeId, gameType, contextElement, this)
            append("</code>")
        }
    }
    
    private fun getScopesText(scopeIds: Set<String>, gameType: ParadoxGameType?, contextElement: PsiElement): String {
        return buildString {
            var appendSeparator = false
            append("<code>")
            for(scopeId in scopeIds) {
                if(appendSeparator) append(", ") else appendSeparator = true
                ParadoxScopeHandler.buildScopeDoc(scopeId, gameType, contextElement, this)
            }
            append("</code>")
        }
    }
    
    private fun StringBuilder.addScopeContext(element: PsiElement, referenceElement: PsiElement, configGroup: CwtConfigGroup, sections: MutableMap<String, String>?) {
        //进行代码提示时也显示作用域上下文信息
        //@Suppress("DEPRECATION")
        //if(DocumentationManager.IS_FROM_LOOKUP.get(element) == true) return
        
        val show = getSettings().documentation.showScopeContext
        if(!show) return
        if(sections == null) return
        val memberElement = referenceElement.parentOfType<ParadoxScriptMemberElement>(true) ?: return
        if(!ParadoxScopeHandler.isScopeContextSupported(memberElement)) return
        val scopeContext = ParadoxScopeHandler.getScopeContext(memberElement)
        if(scopeContext == null) return
        //TODO 如果作用域引用位于脚本表达式中，应当使用那个位置的作用域上下文，但是目前实现不了
        // 因为这里的referenceElement是整个stringExpression，因此这里得到的作用域上下文是脚本表达式最终的作用域上下文
        val contextElement = element
        val gameType = configGroup.gameType.orDefault()
        val scopeContextText = buildString {
            append("<code>")
            ParadoxScopeHandler.buildScopeContextDoc(scopeContext, gameType, contextElement, this)
            append("</code>")
        }
        sections.put(PlsDocBundle.message("sectionTitle.scopeContext"), scopeContextText)
    }
    
    private fun StringBuilder.buildDocumentationContent(element: PsiElement) {
        //渲染文档注释（可能需要作为HTML）
        var current: PsiElement = element
        var documentationLines: LinkedList<String>? = null
        var html = false
        while(true) {
            current = current.prevSibling ?: break
            when {
                current is CwtDocumentationComment -> {
                    val documentationText = current.documentationText
                    if(documentationText != null) {
                        if(documentationLines == null) documentationLines = LinkedList()
                        val docText = documentationText.text.trimStart('#').trim() //这里接受HTML
                        documentationLines.addFirst(docText)
                    }
                }
                current is CwtOptionComment -> {
                    val option = current.option
                    if(option != null) {
                        when {
                            option.name == "format" && option.value == "html" -> html = true
                        }
                    }
                }
                current is PsiWhiteSpace || current is PsiComment -> continue
                else -> break
            }
        }
        if(documentationLines.isNullOrEmpty()) return
        //如果CWT规则文件中的一行文档注释以`\`结束，则解析时不在这里换行
        val documentation = getDocumentation(documentationLines, html)
        content {
            append(documentation)
        }
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
    
    private fun getConfigGroup(element: PsiElement, originalElement: PsiElement?, project: Project): CwtConfigGroup? {
        val gameType = selectGameType(originalElement?.takeIf { it.language.isParadoxLanguage() })
        val configGroup = gameType?.let { getCwtConfig(project).getValue(it) }
            ?: ParadoxConfigHandler.getConfigGroupFromCwt(element, project)
        return configGroup
    }
}
