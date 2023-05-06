package icu.windea.pls.lang

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*
import icu.windea.pls.tool.localisation.*
import java.awt.*
import javax.swing.*

object ParadoxPresentationHandler {
    fun getNameText(definition: ParadoxScriptDefinitionElement): String? {
        val definitionInfo = definition.definitionInfo ?: return null
        val localizedName = definitionInfo.resolvePrimaryLocalisation()
        if(localizedName == null) {
            val locName = definitionInfo.resolvePrimaryLocalisationName() ?: return null
            return locName
        }
        return ParadoxLocalisationTextHtmlRenderer.render(localizedName)
    }
    
    fun getText(localisation: ParadoxLocalisationProperty): String {
        return ParadoxLocalisationTextHtmlRenderer.render(localisation)
    }
    
    fun getText(localisationKey: String, project: Project, contextElement: PsiElement? = null): String? {
        val selector = localisationSelector(project, contextElement).contextSensitive().preferLocale(preferredParadoxLocale())
        val localisation = ParadoxLocalisationSearch.search(localisationKey, selector).find() ?: return null
        return ParadoxLocalisationTextHtmlRenderer.render(localisation)
    }
    
    fun getNameLabel(definition: ParadoxScriptDefinitionElement, color: Color? = null): JLabel? {
        val definitionInfo = definition.definitionInfo ?: return null
        val localizedName = definitionInfo.resolvePrimaryLocalisation()
        if(localizedName == null) {
            val locName = definitionInfo.resolvePrimaryLocalisationName() ?: return null
            return ParadoxLocalisationTextUIRenderer.render(locName, color)
        }
        return ParadoxLocalisationTextUIRenderer.render(localizedName, color)
    }
    
    fun getLabel(localisation: ParadoxLocalisationProperty, color: Color? = null): JLabel? {
        return ParadoxLocalisationTextUIRenderer.render(localisation, color)
    }
    
    fun getLabel(localisationKey: String, project: Project, contextElement: PsiElement? = null): JLabel? {
        val selector = localisationSelector(project, contextElement).contextSensitive().preferLocale(preferredParadoxLocale())
        val localisation = ParadoxLocalisationSearch.search(localisationKey, selector).find() ?: return null
        return ParadoxLocalisationTextUIRenderer.render(localisation)
    }
    
    fun getIcon(ddsFile: PsiFile): Icon? {
        val iconFile = ddsFile.virtualFile ?: return null
        val iconUrl = ParadoxDdsUrlResolver.resolveByFile(iconFile)
        return IconLoader.findIcon(iconUrl.toFileUrl())
    }
    
    fun getIcon(definition: ParadoxScriptDefinitionElement): Icon? {
        val ddsFile = definition.definitionInfo?.resolvePrimaryImage() ?: return null
        val iconFile = ddsFile.virtualFile ?: return null
        val iconUrl = ParadoxDdsUrlResolver.resolveByFile(iconFile)
        return IconLoader.findIcon(iconUrl.toFileUrl())
    }
    
    fun getIcon(definition: ParadoxScriptDefinitionElement, ddsFile: PsiFile): Icon? {
        val iconFile = ddsFile.virtualFile ?: return null
        val iconUrl = ParadoxDdsUrlResolver.resolveByFile(iconFile, definition.getUserData(PlsKeys.iconFrameKey) ?: 0)
        return IconLoader.findIcon(iconUrl.toFileUrl())
    }
}