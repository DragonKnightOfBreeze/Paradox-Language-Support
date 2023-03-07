package icu.windea.pls.lang.presentation

import com.intellij.openapi.diagnostic.*
import com.intellij.ui.*
import com.intellij.util.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selectors.chained.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.localisation.*
import java.awt.*
import java.awt.image.*
import javax.swing.*

/**
 * 提供科技的UI表示（科技卡）。
 */
@WithGameType(ParadoxGameType.Stellaris)
@Suppress("UNUSED_PARAMETER")
class StellarisTechnologyPresentationProvider : ParadoxDefinitionPresentationProvider {
    val backgroundColor = Gray._34
    
    //GFX_technology_unknown 52*52
    //GFX_technology_xxx 52*52
    
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
        
        return try {
            doGetPresentation(definition, definitionInfo)
        } catch(e: Exception) {
            thisLogger().warn(e)
            null
        }
    }
    
    private fun doGetPresentation(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): BufferedImage? {
        val name = getName(definition, definitionInfo) ?: return null
        val icon = getIcon(definition, definitionInfo) ?: return null
        val cost = getCost(definition, definitionInfo) ?: return null
        val backgroundIcon = getBackgroundIcon(definition, definitionInfo) ?: return null
        val bottomLineIcon = getBottomLineIcon(definition, definitionInfo) ?: return null
        val categoryIcon = getCategoryIcon(definition, definitionInfo)
        
        val panel = object : JPanel() {
            override fun paint(g: Graphics) {
                super.paint(g)
                
                g as Graphics2D
                g.color = backgroundColor //设置背景色
                g.fillRect(0, 0, 452, 97)//填充背景色
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON) //抗锯齿
                g.drawImage(backgroundIcon.toImage(), 0, 0, 452, 96, this)
                g.drawImage(bottomLineIcon.toImage(), 0, 96, 452, 1, this)
                g.drawImage(name, 6, 2, this)
                g.drawImage(cost, 452 - 6 - cost.getWidth(this), 2, this)
                g.drawImage(icon.toImage(), 4, 32, 52, 52, this) // (60 - 52) / 2, 20 + ((76 - 52) / 2)
                if(categoryIcon != null) {
                    g.drawImage(categoryIcon.toImage(), 452 - 6 - categoryIcon.iconWidth, 26, 30, 30, this) //452 - 6 - width, 20 + 6
                }
            }
        }
        val image = UIUtil.createImage(panel, 452, 97, BufferedImage.TYPE_INT_ARGB_PRE)
        UIUtil.useSafely(image.graphics) { panel.paint(it) }
        return image
    }
    
    private fun getName(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): Image? {
        return ParadoxPresentationHandler.getName(definition)
    }
    
    private fun getIcon(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): Icon? {
        return ParadoxPresentationHandler.getIcon(definition) ?: getUnknownIcon(definition, definitionInfo)
    }
    
    private fun getUnknownIcon(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): Icon? {
        val selector = definitionSelector(definitionInfo.project, definition).contextSensitive()
        val sprite = ParadoxDefinitionSearch.search("GFX_technology_unknown", "sprite", selector).find() ?: return null
        return ParadoxPresentationHandler.getIcon(sprite)
    }
    
    private fun getCost(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): Image? {
        val cost = definition.getData<StellarisTechnologyDataProvider.Data>()?.cost ?: 0
        return ParadoxLocalisationTextUIRender.renderImage(cost.toString(), "G", definition) //Green
    }
    
    private fun getBackgroundIcon(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): Icon? {
        val area = definition.getData<StellarisTechnologyDataProvider.Data>()?.area ?: return null
        val spriteName = "GFX_tech_entry_${area}_bg"
        val selector = definitionSelector(definitionInfo.project, definition).contextSensitive()
        val sprite = ParadoxDefinitionSearch.search(spriteName, "sprite", selector).find() ?: return null
        return ParadoxPresentationHandler.getIcon(sprite)
    }
    
    private fun getBottomLineIcon(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): Icon? {
        val area = definition.getData<StellarisTechnologyDataProvider.Data>()?.area ?: return null
        val spriteName = "GFX_bottom_line_${area}"
        val selector = definitionSelector(definitionInfo.project, definition).contextSensitive()
        val sprite = ParadoxDefinitionSearch.search(spriteName, "sprite", selector).find() ?: return null
        return ParadoxPresentationHandler.getIcon(sprite)
    }
    
    private fun getCategoryIcon(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): Icon? {
        val category = definition.getData<StellarisTechnologyDataProvider.Data>()?.category?.firstOrNull() ?: return null
        val selector = definitionSelector(definitionInfo.project, definition).contextSensitive()
        val categoryDef = ParadoxDefinitionSearch.search(category, "technology_category", selector).find() ?: return null
        return ParadoxPresentationHandler.getIcon(categoryDef)
    }
    
    private fun getGatewayIcon(definition: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): Icon? {
        val spriteName = "GFX_tech_gateway"
        val selector = definitionSelector(definitionInfo.project, definition).contextSensitive()
        val sprite = ParadoxDefinitionSearch.search(spriteName, "sprite", selector).find() ?: return null
        return ParadoxPresentationHandler.getIcon(sprite)
    }
}