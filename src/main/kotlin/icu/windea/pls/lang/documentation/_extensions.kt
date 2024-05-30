@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.documentation

import com.intellij.codeInsight.navigation.*
import com.intellij.ide.util.*
import com.intellij.lang.documentation.impl.*
import com.intellij.navigation.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.newvfs.*
import com.intellij.platform.backend.documentation.*
import com.intellij.platform.backend.presentation.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.*
import icu.windea.pls.lang.util.*

private val logger = Logger.getInstance("#icu.windea.pls.lang.documentation")

fun defaultTargetPresentation(element: PsiElement):TargetPresentation {
    //similar to [com.intellij.codeInsight.navigation.targetPresentation], with some modifications
    
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

fun getDocumentationTargets(element: PsiElement, originalElement: PsiElement?): List<DocumentationTarget> {
    //delegate to [com.intellij.lang.documentation.psi.psiDocumentationTargets] or use fallback logic
    
    //for (ext in PsiDocumentationTargetProvider.EP_NAME.extensionList) {
    //    val targets = ext.documentationTargets(element, originalElement)
    //    if (targets.isNotEmpty()) return targets
    //}
    //return listOf(PsiElementDocumentationTarget(element.project, element, originalElement))
    ////val targets = PsiDocumentationTargetProvider.EP_NAME.extensionList.flatMap { it.documentationTargets(element, originalElement) }
    ////return targets.ifEmpty { listOf(PsiElementDocumentationTarget (element.project, element, originalElement)) }
    
    val targets = psiDocumentationTargets(element, originalElement)
    if(targets.isNotEmpty()) return targets
    
    return getDocumentationTarget(element, originalElement).toSingletonListOrEmpty()
}

@Suppress("UNUSED_PARAMETER")
private fun getDocumentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
    return when {
        element is PsiFileSystemItem -> null
        element.language.isParadoxLanguage() -> ParadoxDocumentationTarget(element, null)
        element.language == CwtLanguage -> CwtDocumentationTarget(element, null)
        else -> null
    }
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
