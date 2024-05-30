@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.documentation

import com.intellij.codeInsight.navigation.*
import com.intellij.ide.util.*
import com.intellij.navigation.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.newvfs.*
import com.intellij.platform.backend.documentation.*
import com.intellij.platform.backend.presentation.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.util.*

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

interface LocaleAwareDocumentationTarget: DocumentationTarget {
    var targetLocale: String?
    
}

fun LocaleAwareDocumentationTarget.getTargetLocaleConfig(): CwtLocalisationLocaleConfig? {
    val targetLocale = targetLocale
    if(targetLocale == null) return null
    if(targetLocale == "auto") return CwtLocalisationLocaleConfig.AUTO
    return ParadoxLocaleHandler.getLocaleConfigById(targetLocale)
}

fun LocaleAwareDocumentationTarget.setTargetLocaleConfig(localeConfig: CwtLocalisationLocaleConfig?) {
    targetLocale = localeConfig?.id
}
