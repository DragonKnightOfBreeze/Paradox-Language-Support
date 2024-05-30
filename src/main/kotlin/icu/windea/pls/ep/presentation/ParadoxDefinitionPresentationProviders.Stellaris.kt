package icu.windea.pls.ep.presentation

import com.intellij.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.ep.data.*
import icu.windea.pls.ep.data.StellarisTechnologyDataProvider.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderer.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import java.awt.*
import javax.swing.*

/**
 * 提供科技的UI表示（科技卡）。
 */
@Suppress("UNUSED_PARAMETER")
@WithGameType(ParadoxGameType.Stellaris)
class StellarisTechnologyPresentationProvider : ParadoxDefinitionPresentationProvider {
    val backgroundColor = Gray._34
    
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == "technology"
    }
    
    override fun getPresentation(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): JComponent? {
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
        
        val data = definition.getData<Data>() ?: return null
        val backgroundIcon = getBackgroundIcon(definition, definitionInfo, data) ?: return null
        val bottomLineIcon = getBottomLineIcon(definition, definitionInfo, data) ?: return null
        val nameLabel = getNameLabel(definition, definitionInfo, data) ?: return null
        val costLabel = getCostLabel(definition, definitionInfo, data) ?: return null
        val icon = getIcon(definition, definitionInfo, data)?.resize(52, 52) ?: return null
        val categoryIcon = getCategoryIcon(definition, definitionInfo, data)?.resize(30, 30)
        val panel = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                g as Graphics2D
                g.color = backgroundColor //设置背景色
                g.fillRect(0, 0, 452, 97)//填充背景色
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON) //抗锯齿
                g.drawImage(backgroundIcon.toImage(), 0, 0, 452, 96, this)
                g.drawImage(bottomLineIcon.toImage(), 0, 96, 452, 1, this)
            }
        }
        panel.size = Dimension(452, 97)
        panel.preferredSize = panel.size
        panel.add(nameLabel.withLocation(6, 2)) //6, 2
        panel.add(costLabel.withLocation(452 - 6 - costLabel.width, 2)) //452 - 6 - width, 2
        panel.add(icon.toLabel().withLocation(4, 32)) // (60 - 52) / 2, 20 + ((76 - 52) / 2)
        if(categoryIcon != null) {
            panel.add(categoryIcon.toLabel().withLocation(452 - 6 - categoryIcon.iconWidth, 26)) //452 - 6 - width, 20 + 6
        }
        return panel
    }
    
    private fun getNameLabel(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, data: Data): JLabel? {
        return ParadoxPresentationHandler.getNameLabel(definition, Color.WHITE)
    }
    
    private fun getCostLabel(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, data: Data): JLabel? {
        val cost = definition.getData<Data>()?.cost ?: 0
        val color = ParadoxTextColorHandler.getInfo("G", definitionInfo.project, definition)?.color //Green
        return ParadoxLocalisationTextUIRenderer.render(cost.toString(), color)
    }
    
    private fun getIcon(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, data: Data): Icon? {
        return ParadoxPresentationHandler.getIcon(definition) ?: getUnknownIcon(definition, definitionInfo)
    }
    
    private fun getUnknownIcon(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Icon? {
        val selector = definitionSelector(definitionInfo.project, definition).contextSensitive()
        val sprite = ParadoxDefinitionSearch.search("GFX_technology_unknown", "sprite", selector).find() ?: return null
        return ParadoxPresentationHandler.getIcon(sprite)
    }
    
    private fun getBackgroundIcon(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, data: Data): Icon? {
        val area = data.area ?: return null
        val types = definitionInfo.subtypes
        val spriteName = when {
            types.contains("dangerous") && types.contains("rare") -> "GFX_tech_entry_dangerous_rare_bg"
            types.contains("dangerous") -> "GFX_tech_entry_dangerous_bg"
            types.contains("rare") -> "GFX_tech_entry_rare_bg"
            else -> "GFX_tech_entry_${area}_bg"
        }
        val selector = definitionSelector(definitionInfo.project, definition).contextSensitive()
        val sprite = ParadoxDefinitionSearch.search(spriteName, "sprite", selector).find() ?: return null
        return ParadoxPresentationHandler.getIcon(sprite)
    }
    
    private fun getBottomLineIcon(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, data: Data): Icon? {
        val area = data.area ?: return null
        val spriteName = "GFX_bottom_line_${area}"
        val selector = definitionSelector(definitionInfo.project, definition).contextSensitive()
        val sprite = ParadoxDefinitionSearch.search(spriteName, "sprite", selector).find() ?: return null
        return ParadoxPresentationHandler.getIcon(sprite)
    }
    
    private fun getCategoryIcon(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, data: Data): Icon? {
        val category = data.category?.firstOrNull() ?: return null
        val selector = definitionSelector(definitionInfo.project, definition).contextSensitive()
        val categoryDef = ParadoxDefinitionSearch.search(category, "technology_category", selector).find() ?: return null
        return ParadoxPresentationHandler.getIcon(categoryDef)
    }
    
    private fun getGatewayIcon(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Icon? {
        val spriteName = "GFX_tech_gateway"
        val selector = definitionSelector(definitionInfo.project, definition).contextSensitive()
        val sprite = ParadoxDefinitionSearch.search(spriteName, "sprite", selector).find() ?: return null
        return ParadoxPresentationHandler.getIcon(sprite)
    }
}