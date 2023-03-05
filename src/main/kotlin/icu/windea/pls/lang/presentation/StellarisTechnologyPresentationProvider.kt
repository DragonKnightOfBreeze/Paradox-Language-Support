package icu.windea.pls.lang.presentation

import com.intellij.ui.*
import com.intellij.util.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selectors.chained.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.localisation.*
import java.awt.*
import java.awt.image.*
import javax.swing.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisTechnologyPresentationProvider : ParadoxDefinitionPresentationProvider {
    //GFX_technology_unknown 52*52
    
    //GFX_bottom_line_physics 533*1
    //GFX_bottom_line_society 533*1
    //GFX_bottom_line_engineering 533*1
    
    //背景
    //GFX_tech_entry_physics_bg 452*96
    //GFX_tech_entry_society_bg 452*96
    //GFX_tech_entry_engineering_bg 452*96
    //GFX_tech_entry_rare_bg 452*96
    //GFX_tech_entry_dangerous_bg 452*96
    //GFX_tech_entry_dangerous_rare_bg 452*96
    
    //突破图标
    //GFX_tech_gateway 24*24
    
    //标题 - 左上 - 白色
    //费用 - 右上 - 绿色
    
    override fun getPresentation(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Image? {
        if(definition !is ParadoxScriptProperty) return null
        val name = getName(definition, definitionInfo)
        val cost = getCost(definition, definitionInfo)
        val icon = getIcon(definition, definitionInfo)?.resize(52, 52)
        val panel = JPanel()
        val image = UIUtil.createImage(panel, 452, 96, BufferedImage.TYPE_INT_ARGB)
        return image
    }
    
    private fun getName(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): SimpleColoredText? {
        val localizedName = StellarisTechnologyHandler.getLocalizedName(definition) ?: return null
        return ParadoxLocalisationTextUIRender.render(localizedName)
    }
    
    private fun getCost(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): SimpleColoredText? {
        return null
    }
    
    private fun getIcon(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): Icon? {
        val iconFile = StellarisTechnologyHandler.getIconFile(definition)
        if(iconFile == null) return getUnknownIcon(definition, definitionInfo)
        return ParadoxPresentationHandler.getIcon(iconFile)
    }
    
    private fun getUnknownIcon(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): Icon? {
        val selector = definitionSelector(definitionInfo.project, definition).contextSensitive()
        val sprite = ParadoxDefinitionSearch.search("GFX_technology_unknown", "sprite", selector).find() ?: return null
        return ParadoxPresentationHandler.getIcon(sprite)
    }
    
    private fun getBackgroundIcon(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) : Icon? {
        val selector = definitionSelector(definitionInfo.project, definition).contextSensitive()
        val sprite = ParadoxDefinitionSearch.search("GFX_tech_entry_physics_bg", "sprite", selector).find() ?: return null
        return ParadoxPresentationHandler.getIcon(sprite)
    }
    
    private fun getAreaIcon(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) : Icon? {
        return null
    }
    
    private fun getGatewayIcon(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): Icon? {
        return null
    }
}