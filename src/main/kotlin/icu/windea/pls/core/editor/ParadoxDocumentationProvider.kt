@file:Suppress("UnusedReceiverParameter")

package icu.windea.pls.core.editor

import com.intellij.lang.documentation.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.documentation.*
import icu.windea.pls.model.*
import icu.windea.pls.lang.modifier.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.model.constraints.*
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
        return DocumentationElementLinkProvider.resolve(link, context)
    }
    
    //这里为RenameableFakePsiElement，也就是那些实际上没有声明处的PsiElement，提供快速文档
    
    override fun getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement?): String? {
        return when(element) {
            is ParadoxParameterElement -> getParameterInfo(element, originalElement)
            is ParadoxLocalisationParameterElement -> getLocalisationParameterInfo(element, originalElement)
            is ParadoxValueSetValueElement -> getValueSetValueInfo(element, originalElement)
            is ParadoxComplexEnumValueElement -> getComplexEnumValueInfo(element, originalElement)
            is ParadoxModifierElement -> getModifierInfo(element, originalElement)
            else -> null
        }
    }
    
    private fun getParameterInfo(element: ParadoxParameterElement, originalElement: PsiElement?): String {
        return buildString {
            buildParameterDefinition(element)
        }
    }
    
    private fun getLocalisationParameterInfo(element: ParadoxLocalisationParameterElement, originalElement: PsiElement?): String {
        return buildString {
            buildLocalisationParameterDefinition(element)
        }
    }
    
    private fun getValueSetValueInfo(element: ParadoxValueSetValueElement, originalElement: PsiElement?): String {
        return buildString {
            buildValueSetValueDefinition(element)
        }
    }
    
    private fun getComplexEnumValueInfo(element: ParadoxComplexEnumValueElement, originalElement: PsiElement?): String {
        return buildString {
            buildComplexEnumValueDefinition(element)
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
            is ParadoxLocalisationParameterElement -> getLocalisationParameterDoc(element, originalElement)
            is ParadoxValueSetValueElement -> getValueSetValueDoc(element, originalElement)
            is ParadoxComplexEnumValueElement -> getComplexEnumValueDoc(element, originalElement)
            is ParadoxModifierElement -> getModifierDoc(element, originalElement)
            else -> null
        }
    }
    
    private fun getParameterDoc(element: ParadoxParameterElement, originalElement: PsiElement?): String {
        return buildString {
            buildParameterDefinition(element)
        }
    }
    
    private fun getLocalisationParameterDoc(element: ParadoxLocalisationParameterElement, originalElement: PsiElement?): String {
        return buildString {
            buildLocalisationParameterDefinition(element)
        }
    }
    
    private fun getValueSetValueDoc(element: PsiElement, originalElement: PsiElement?): String? {
        val resolved = element.references.firstOrNull()?.resolve() as? ParadoxValueSetValueElement ?: return null
        return getValueSetValueInfo(resolved, originalElement)
    }
    
    private fun getValueSetValueDoc(element: ParadoxValueSetValueElement, originalElement: PsiElement?): String {
        return buildString {
            buildValueSetValueDefinition(element)
        }
    }
    
    private fun getComplexEnumValueDoc(element: ParadoxComplexEnumValueElement, originalElement: PsiElement?): String {
        return buildString {
            buildComplexEnumValueDefinition(element)
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
                append(PlsBundle.message("prefix.parameter")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
            }
        }
    }
    
    private fun StringBuilder.buildLocalisationParameterDefinition(element: ParadoxLocalisationParameterElement) {
        val name = element.name
        definition {
            val r = ParadoxLocalisationParameterSupport.getDocumentationDefinition(element, this)
            if(!r) {
                //显示默认的快速文档
                append(PlsBundle.message("prefix.parameter")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
            }
        }
    }
    
    private fun StringBuilder.buildValueSetValueDefinition(element: ParadoxValueSetValueElement) {
        val name = element.name
        val valueSetNames = element.valueSetNames
        val gameType = element.gameType
        val configGroup = getCwtConfig(element.project).get(gameType)
        definition {
            append(PlsBundle.message("prefix.valueSetValue")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
            append(": ")
            var appendSeparator = false
            for(valueSetName in valueSetNames) {
                if(appendSeparator) append(" | ") else appendSeparator = true
                val valueConfig = configGroup.values[valueSetName]
                if(valueConfig != null) {
                    val typeLink = "${gameType.linkToken}values/${valueSetName}"
                    appendCwtLink(typeLink, valueSetName)
                } else {
                    append(valueSetName)
                }
            }
        }
    }
    
    private fun StringBuilder.buildComplexEnumValueDefinition(element: ParadoxComplexEnumValueElement) {
        definition {
            val name = element.name
            val enumName = element.enumName
            val gameType = element.gameType
            val configGroup = getCwtConfig(element.project).get(gameType)
            append(PlsBundle.message("prefix.complexEnumValue")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
            val complexEnumConfig = configGroup.complexEnums[enumName]
            if(complexEnumConfig != null) {
                val typeLink = "${gameType.linkToken}complex_enums/${enumName}"
                append(": ").appendCwtLink(typeLink, enumName)
            } else {
                append(": ").append(enumName)
            }
        }
    }
    
    private fun StringBuilder.buildModifierDefinition(element: ParadoxModifierElement, sectionsList: List<MutableMap<String, String>>?) {
        val name = element.name
        definition {
            val r = ParadoxModifierSupport.getDocumentationDefinition(element, this)
            if(!r) {
                //显示默认的快速文档
                append(PlsBundle.message("prefix.modifier")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
            }
            
            val configGroup = getCwtConfig(element.project).get(element.gameType)
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
        val gameType = configGroup.gameType ?: return
        val project = configGroup.project
        val nameLocalisation = run {
            val key = ParadoxModifierHandler.getModifierNameKey(name)
            val selector = localisationSelector(project, element).contextSensitive()
                .preferLocale(preferredParadoxLocale())
                .withConstraint(ParadoxLocalisationConstraint.Modifier)
            ParadoxLocalisationSearch.search(key, selector).find()
        }
        val descLocalisation = run {
            val key = ParadoxModifierHandler.getModifierDescKey(name)
            val selector = localisationSelector(project, element).contextSensitive()
                .preferLocale(preferredParadoxLocale())
                .withConstraint(ParadoxLocalisationConstraint.Modifier)
            ParadoxLocalisationSearch.search(key, selector).find()
        }
        //如果没找到的话，不要在文档中显示相关信息
        if(nameLocalisation != null) {
            appendBr()
            append(PlsBundle.message("prefix.relatedLocalisation")).append(" ")
            append("name = ").appendLocalisationLink(gameType, nameLocalisation.name, element)
        }
        if(descLocalisation != null) {
            appendBr()
            append(PlsBundle.message("prefix.relatedLocalisation")).append(" ")
            append("desc = ").appendLocalisationLink(gameType, descLocalisation.name, element)
        }
        if(sections != null && render) {
            if(nameLocalisation != null) {
                val richText = ParadoxLocalisationTextHtmlRenderer.render(nameLocalisation, forDoc = true)
                sections.put("<code>name</code>", richText)
            }
            if(descLocalisation != null) {
                val richText = ParadoxLocalisationTextHtmlRenderer.render(descLocalisation, forDoc = true)
                sections.put("<code>desc</code>", richText)
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
        val gameType = configGroup.gameType ?: return
        val project = configGroup.project
        val iconPath = ParadoxModifierHandler.getModifierIconPath(name)
        val iconFile = run {
            val iconSelector = fileSelector(project, element).contextSensitive()
            ParadoxFilePathSearch.search(iconPath, null, iconSelector).find()
        }
        //如果没找到的话，不要在文档中显示相关信息
        if(iconFile != null) {
            appendBr()
            append(PlsBundle.message("prefix.relatedImage")).append(" ")
            append("icon = ").appendFilePathLink(gameType, iconPath, iconPath, element)
        }
        if(sections != null && render) {
            if(iconFile != null) {
                val url = ParadoxImageResolver.resolveUrlByFile(iconFile)
                sections.put("<code>icon</code>", buildString { appendImgTag(url) })
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
        
        if(sections == null) return
        val gameType = configGroup.gameType ?: return
        val modifierCategories = ParadoxModifierHandler.getModifierCategories(element) ?: return
        val contextElement = element
        val categoryNames = modifierCategories.keys
        if(categoryNames.isNotEmpty()) {
            sections.put(PlsBundle.message("sectionTitle.categories"), ParadoxDocumentationBuilder.getModifierCategoriesText(categoryNames, gameType, contextElement))
        }
        
        val supportedScopes = modifierCategories.getSupportedScopes()
        sections.put(PlsBundle.message("sectionTitle.supportedScopes"), ParadoxDocumentationBuilder.getScopesText(supportedScopes, gameType, contextElement))
    }
    
    private fun StringBuilder.addScopeContext(
        element: ParadoxModifierElement,
        name: String,
        configGroup: CwtConfigGroup,
        sections: MutableMap<String, String>?
    ) {
        //进行代码提示时也显示作用域上下文信息
        //@Suppress("DEPRECATION")
        //if(DocumentationManager.IS_FROM_LOOKUP.get(element) == true) return
        
        if(!getSettings().documentation.showScopeContext) return
        
        if(sections == null) return
        val gameType = configGroup.gameType ?: return
        val memberElement = element.parentOfType<ParadoxScriptMemberElement>(true) ?: return
        if(!ParadoxScopeHandler.isScopeContextSupported(memberElement, indirect = true)) return
        val scopeContext = ParadoxScopeHandler.getScopeContext(memberElement)
        if(scopeContext == null) return
        //TODO 如果作用域引用位于脚本表达式中，应当使用那个位置的作用域上下文，但是目前实现不了
        // 因为这里的referenceElement是整个stringExpression，得到的作用域上下文会是脚本表达式最终的作用域上下文
        sections.put(PlsBundle.message("sectionTitle.scopeContext"), ParadoxDocumentationBuilder.getScopeContextText(scopeContext, gameType, element))
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
}