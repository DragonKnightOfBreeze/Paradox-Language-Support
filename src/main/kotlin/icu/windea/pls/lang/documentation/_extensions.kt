@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.documentation

import com.intellij.codeInsight.navigation.*
import com.intellij.ide.util.*
import com.intellij.navigation.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.newvfs.*
import com.intellij.platform.backend.presentation.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

fun defaultTargetPresentation(element: PsiElement):TargetPresentation {
    //this implementation is similar to [com.intellij.codeInsight.navigation.targetPresentation], with some modifications
    
    val project = element.project
    val file = element.containingFile?.virtualFile
    val itemPresentation = (element as? NavigationItem)?.presentation
    val presentableText: String = itemPresentation?.presentableText
        ?: (element as? PsiNamedElement)?.name
        ?: element.text
    val moduleTextWithIcon = PsiElementListCellRenderer.getModuleTextWithIcon(element)
    return TargetPresentation
        .builder(presentableText)
        .backgroundColor(file?.let { VfsPresentationUtil.getFileBackgroundColor(project, file) })
        .icon(element.getIcon(Iconable.ICON_FLAG_VISIBILITY or Iconable.ICON_FLAG_READ_STATUS))
        .presentableTextAttributes(itemPresentation?.getColoredAttributes())
        .containerText(itemPresentation?.getContainerText(), file?.let { fileStatusAttributes(project, file) })
        .locationText(moduleTextWithIcon?.text, moduleTextWithIcon?.icon)
        .presentation()
}

private fun ItemPresentation.getColoredAttributes(): TextAttributes? {
    val coloredPresentation = this as? ColoredItemPresentation
    val textAttributesKey = coloredPresentation?.textAttributesKey ?: return null
    return EditorColorsManager.getInstance().schemeForCurrentUITheme.getAttributes(textAttributesKey)
}

private fun ItemPresentation.getContainerText(): String? {
    return locationString
}

fun getModifierCategoriesText(categories: Set<String>, gameType: ParadoxGameType, contextElement: PsiElement): String {
    if(categories.isEmpty()) return ""
    return buildString {
        append("<code>")
        var appendSeparator = false
        for(category in categories) {
            if(appendSeparator) append(", ") else appendSeparator = true
            appendCwtLink("${gameType.prefix}modifier_categories/$category", category, contextElement)
        }
        append("</code>")
    }
}

fun getScopeText(scopeId: String, gameType: ParadoxGameType, contextElement: PsiElement): String {
    return buildString {
        append("<code>")
        ParadoxScopeHandler.buildScopeDoc(scopeId, gameType, contextElement, this)
        append("</code>")
    }
}

fun getScopesText(scopeIds: Set<String>, gameType: ParadoxGameType, contextElement: PsiElement): String {
    if(scopeIds.isEmpty()) return ""
    return buildString {
        append("<code>")
        var appendSeparator = false
        for(scopeId in scopeIds) {
            if(appendSeparator) append(", ") else appendSeparator = true
            ParadoxScopeHandler.buildScopeDoc(scopeId, gameType, contextElement, this)
        }
        append("</code>")
    }
}

fun getScopeContextText(scopeContext: ParadoxScopeContext, gameType: ParadoxGameType, contextElement: PsiElement) : String{
    return buildString {
        append("<code>")
        ParadoxScopeHandler.buildScopeContextDoc(scopeContext, gameType, contextElement, this)
        append("</code>")
    }
}