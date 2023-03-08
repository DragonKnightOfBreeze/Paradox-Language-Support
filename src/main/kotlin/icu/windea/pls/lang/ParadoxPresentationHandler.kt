package icu.windea.pls.lang

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*
import icu.windea.pls.tool.localisation.*
import javax.swing.*

object ParadoxPresentationHandler {
    @JvmStatic
    fun getNameLabel(definition: ParadoxScriptDefinitionElement): JLabel? {
        val definitionInfo = definition.definitionInfo ?: return null
        val localizedName = definitionInfo.resolvePrimaryLocalisation(definition)
        if(localizedName == null) {
            val locName = definitionInfo.resolvePrimaryLocalisationName(definition) ?: return null
            return ParadoxLocalisationTextUIRender.render(locName)
        }
        return ParadoxLocalisationTextUIRender.render(localizedName)
    }
    
    @JvmStatic
    fun getIcon(ddsFile: PsiFile): Icon? {
        val iconFile = ddsFile.virtualFile ?: return null
        val iconUrl = ParadoxDdsUrlResolver.resolveByFile(iconFile)
        return IconLoader.findIcon(iconUrl.toFileUrl())
    }
    
    @JvmStatic
    fun getIcon(definition: ParadoxScriptDefinitionElement): Icon? {
        val ddsFile = definition.definitionInfo?.resolvePrimaryImage(definition) ?: return null
        val iconFile = ddsFile.virtualFile ?: return null
        val iconUrl = ParadoxDdsUrlResolver.resolveByFile(iconFile)
        return IconLoader.findIcon(iconUrl.toFileUrl())
    }
    
    @JvmStatic
    fun getIcon(definition: ParadoxScriptDefinitionElement, ddsFile: PsiFile): Icon? {
        val iconFile = ddsFile.virtualFile ?: return null
        val iconUrl = ParadoxDdsUrlResolver.resolveByFile(iconFile, definition.getUserData(PlsKeys.iconFrame) ?: 0)
        return IconLoader.findIcon(iconUrl.toFileUrl())
    }
}