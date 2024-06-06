@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.documentation

import com.intellij.codeInsight.navigation.*
import com.intellij.ide.util.*
import com.intellij.navigation.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.newvfs.*
import com.intellij.platform.backend.documentation.*
import com.intellij.platform.backend.presentation.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.*
import icu.windea.pls.lang.*

private val logger = Logger.getInstance("#icu.windea.pls.lang.documentation")

fun getTargetPresentation(element: PsiElement):TargetPresentation {
    //similar to [com.intellij.codeInsight.navigation.targetPresentation], with some modifications
    
    val project = element.project
    val file = element.containingFile?.virtualFile
    val itemPresentation = (element as? NavigationItem)?.presentation
    val presentableText: String = itemPresentation?.presentableText
        ?: (element as? PsiNamedElement)?.name
        ?: element.text
    val moduleTextWithIcon = getModuleTextWithIcon(element)
    return TargetPresentation
        .builder(presentableText)
        .backgroundColor(file?.let { VfsPresentationUtil.getFileBackgroundColor(project, file) })
        .icon(element.getIcon(Iconable.ICON_FLAG_VISIBILITY or Iconable.ICON_FLAG_READ_STATUS))
        .presentableTextAttributes(itemPresentation?.getColoredAttributes())
        .containerText(itemPresentation?.getContainerText(), file?.let { fileStatusAttributes(project, file) })
        .locationText(moduleTextWithIcon?.text, moduleTextWithIcon?.icon)
        .presentation()
}

private fun getModuleTextWithIcon(value: Any?): TextWithIcon? {
    //copied from [com.intellij.ide.util.PsiElementListCellRenderer.getModuleTextWithIcon]
    
    val factory = ModuleRendererFactory.findInstance(value)
    if(factory is PlatformModuleRendererFactory) {
        // it won't display any new information
        return null
    }
    return factory.getModuleTextWithIcon(value)
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

val DocumentationTarget.targetElement: PsiElement? 
    get() = when {
        this is CwtDocumentationTarget -> this.element
        this is ParadoxDocumentationTarget -> this.element
        else -> null
    }