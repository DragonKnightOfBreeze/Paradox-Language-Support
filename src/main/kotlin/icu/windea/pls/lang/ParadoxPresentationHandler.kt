package icu.windea.pls.lang

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*
import icu.windea.pls.tool.localisation.*
import java.awt.*
import javax.swing.*

object ParadoxPresentationHandler {
    fun getNameText(definition: ParadoxScriptDefinitionElement): String? {
        val localizedName = ParadoxDefinitionHandler.getPrimaryLocalisation(definition)
        if(localizedName == null) {
            val key = ParadoxDefinitionHandler.getPrimaryLocalisationKey(definition) ?: return null
            return key
        }
        return ParadoxLocalisationTextHtmlRenderer.render(localizedName)
    }
    
    fun getText(localisation: ParadoxLocalisationProperty): String {
        return ParadoxLocalisationTextHtmlRenderer.render(localisation)
    }
    
    fun getText(localisationKey: String, project: Project, contextElement: PsiElement? = null): String? {
        val selector = localisationSelector(project, contextElement).contextSensitive().preferLocale(ParadoxLocaleHandler.getPreferredLocale())
        val localisation = ParadoxLocalisationSearch.search(localisationKey, selector).find() ?: return null
        return ParadoxLocalisationTextHtmlRenderer.render(localisation)
    }
    
    fun getNameLabel(definition: ParadoxScriptDefinitionElement, color: Color? = null): JLabel? {
        val localizedName = ParadoxDefinitionHandler.getPrimaryLocalisation(definition)
        if(localizedName == null) {
            val key = ParadoxDefinitionHandler.getPrimaryLocalisationKey(definition) ?: return null
            return ParadoxLocalisationTextUIRenderer.render(key, color)
        }
        return ParadoxLocalisationTextUIRenderer.render(localizedName, color)
    }
    
    fun getLabel(localisation: ParadoxLocalisationProperty, color: Color? = null): JLabel? {
        return ParadoxLocalisationTextUIRenderer.render(localisation, color)
    }
    
    fun getLabel(localisationKey: String, project: Project, contextElement: PsiElement? = null): JLabel? {
        val selector = localisationSelector(project, contextElement).contextSensitive().preferLocale(ParadoxLocaleHandler.getPreferredLocale())
        val localisation = ParadoxLocalisationSearch.search(localisationKey, selector).find() ?: return null
        return ParadoxLocalisationTextUIRenderer.render(localisation)
    }
    
    fun getIcon(ddsFile: PsiFile): Icon? {
        val iconFile = ddsFile.virtualFile ?: return null
        val iconUrl = ParadoxImageResolver.resolveUrlByFile(iconFile) ?: return null
        return iconUrl.toFileUrl().toIconOrNull()
    }
    
    fun getIcon(definition: ParadoxScriptDefinitionElement): Icon? {
        val ddsFile = ParadoxDefinitionHandler.getPrimaryImage(definition) ?: return null
        val iconFile = ddsFile.virtualFile ?: return null
        val iconUrl = ParadoxImageResolver.resolveUrlByFile(iconFile) ?: return null
        return iconUrl.toFileUrl().toIconOrNull()
    }
    
    fun getIcon(definition: ParadoxScriptDefinitionElement, ddsFile: PsiFile): Icon? {
        val iconFile = ddsFile.virtualFile ?: return null
        val frameInfo = definition.getUserData(PlsKeys.frameInfo)
        val iconUrl = ParadoxImageResolver.resolveUrlByFile(iconFile, frameInfo) ?: return null
        return iconUrl.toFileUrl().toIconOrNull()
    }
}