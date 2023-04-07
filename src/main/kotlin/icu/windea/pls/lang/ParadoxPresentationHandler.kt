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
import javax.swing.*

object ParadoxPresentationHandler {
    @JvmStatic
    fun getNameText(definition: ParadoxScriptDefinitionElement): String? {
        val definitionInfo = definition.definitionInfo ?: return null
        val localizedName = definitionInfo.resolvePrimaryLocalisation()
        if(localizedName == null) {
            val locName = definitionInfo.resolvePrimaryLocalisationName() ?: return null
            return locName
        }
        return ParadoxLocalisationTextRenderer.render(localizedName)
    }
    
    @JvmStatic
    fun getText(localisation: ParadoxLocalisationProperty): String {
        return ParadoxLocalisationTextRenderer.render(localisation)
    }
    
    @JvmStatic
    fun getText(localisationKey: String, project: Project, contextElement: PsiElement? = null): String? {
        val selector = localisationSelector(project, contextElement).contextSensitive()
        val localisation = ParadoxLocalisationSearch.search(localisationKey, selector).find() ?: return null
        return ParadoxLocalisationTextRenderer.render(localisation)
    }
    
    @JvmStatic
    fun getNameLabel(definition: ParadoxScriptDefinitionElement, colorHex: String? = null): JLabel? {
        val definitionInfo = definition.definitionInfo ?: return null
        val localizedName = definitionInfo.resolvePrimaryLocalisation()
        if(localizedName == null) {
            val locName = definitionInfo.resolvePrimaryLocalisationName() ?: return null
            return ParadoxLocalisationTextUIRenderer.render(locName, colorHex)
        }
        return ParadoxLocalisationTextUIRenderer.render(localizedName, colorHex)
    }
    
    @JvmStatic
    fun getLabel(localisation: ParadoxLocalisationProperty, colorHex: String? = null): JLabel? {
        return ParadoxLocalisationTextUIRenderer.render(localisation, colorHex)
    }
    
    @JvmStatic
    fun getLabel(localisationKey: String, project: Project, contextElement: PsiElement? = null): JLabel? {
        val selector = localisationSelector(project, contextElement).contextSensitive()
        val localisation = ParadoxLocalisationSearch.search(localisationKey, selector).find() ?: return null
        return ParadoxLocalisationTextUIRenderer.render(localisation)
    }
    
    @JvmStatic
    fun getIcon(ddsFile: PsiFile): Icon? {
        val iconFile = ddsFile.virtualFile ?: return null
        val iconUrl = ParadoxDdsUrlResolver.resolveByFile(iconFile)
        return IconLoader.findIcon(iconUrl.toFileUrl())
    }
    
    @JvmStatic
    fun getIcon(definition: ParadoxScriptDefinitionElement): Icon? {
        val ddsFile = definition.definitionInfo?.resolvePrimaryImage() ?: return null
        val iconFile = ddsFile.virtualFile ?: return null
        val iconUrl = ParadoxDdsUrlResolver.resolveByFile(iconFile)
        return IconLoader.findIcon(iconUrl.toFileUrl())
    }
    
    @JvmStatic
    fun getIcon(definition: ParadoxScriptDefinitionElement, ddsFile: PsiFile): Icon? {
        val iconFile = ddsFile.virtualFile ?: return null
        val iconUrl = ParadoxDdsUrlResolver.resolveByFile(iconFile, definition.getUserData(PlsKeys.iconFrameKey) ?: 0)
        return IconLoader.findIcon(iconUrl.toFileUrl())
    }
}