package icu.windea.pls.ep.presentation

import com.intellij.openapi.diagnostic.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.data.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*
import java.awt.*
import javax.swing.*
import icu.windea.pls.ep.presentation.ParadoxDefinitionPresentationProviderDelegates as Delegates

/**
 * 提供科技的图形表示（科技卡）。
 */
class StellarisTechnologyPresentation(definition: ParadoxScriptDefinitionElement) : ParadoxDefinitionPresentation {
    private val pointer = definition.createPointer()

    override fun createComponent(): JComponent? {
        val builder = runCatchingCancelable { ComponentBuilder(pointer) }.getOrNull() ?: return null
        return runCatchingCancelable { builder.create() }.onFailure { thisLogger().warn(it) }.getOrNull()
    }

    @WithGameType(ParadoxGameType.Stellaris)
    class Provider : ParadoxDefinitionPresentationProvider<ParadoxDefinitionPresentation> by Delegates.create(ParadoxDefinitionTypes.Technology)

    class ComponentBuilder(pointer: SmartPsiElementPointer<ParadoxScriptDefinitionElement>) {
        @Suppress("UseJBColor")
        object Constants {
            val backgroundColor = Gray._34
            val whiteColor = Color.WHITE
        }

        private val definition = pointer.element ?: throw IllegalArgumentException("Definition is null")
        private val definitionInfo = definition.definitionInfo ?: throw IllegalArgumentException("Definition info is null")
        private val definitionData = definition.getData<StellarisTechnologyData>() ?: throw IllegalStateException("Definition data is null")

        fun create(): JComponent? {
            return getPanel()
        }

        private fun getPanel(): JPanel? {
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

            val backgroundIcon = getBackgroundIcon() ?: return null
            val bottomLineIcon = getBottomLineIcon() ?: return null
            val nameLabel = getNameLabel()
            val costLabel = getCostLabel()
            val icon = getIcon()?.resize(52, 52) ?: return null
            val categoryIcon = getCategoryIcon()?.resize(30, 30)
            val panel = object : JPanel() {
                override fun paintComponent(g: Graphics) {
                    super.paintComponent(g)
                    g as Graphics2D
                    g.color = Constants.backgroundColor //设置背景色
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
            if (categoryIcon != null) {
                panel.add(categoryIcon.toLabel().withLocation(452 - 6 - categoryIcon.iconWidth, 26)) //452 - 6 - width, 20 + 6
            }
            return panel
        }

        private fun getNameLabel(): JLabel {
            val nameText = ParadoxPresentationManager.getNameTextOrKey(definition)
            return ParadoxPresentationManager.getLabel(nameText.or.anonymous(), Constants.whiteColor)
        }

        private fun getCostLabel(): JLabel {
            val color = ParadoxTextColorManager.getInfo("G", definitionInfo.project, definition)?.color //Green
            val cost = definition.getData<StellarisTechnologyData>()?.cost ?: 0
            return ParadoxPresentationManager.getLabel(cost.toString(), color)
        }

        private fun getIcon(): Icon? {
            return ParadoxPresentationManager.getIcon(definition) ?: getUnknownIcon()
        }

        private fun getUnknownIcon(): Icon? {
            val sprite = ParadoxDefinitionSearch.search("GFX_technology_unknown", ParadoxDefinitionTypes.Sprite, selector()).find() ?: return null
            return ParadoxPresentationManager.getIcon(sprite)
        }

        private fun getBackgroundIcon(): Icon? {
            val area = definitionData.area ?: return null
            val types = definitionInfo.subtypes
            val spriteName = when {
                types.contains("dangerous") && types.contains("rare") -> "GFX_tech_entry_dangerous_rare_bg"
                types.contains("dangerous") -> "GFX_tech_entry_dangerous_bg"
                types.contains("rare") -> "GFX_tech_entry_rare_bg"
                else -> "GFX_tech_entry_${area}_bg"
            }
            val sprite = ParadoxDefinitionSearch.search(spriteName, ParadoxDefinitionTypes.Sprite, selector()).find() ?: return null
            return ParadoxPresentationManager.getIcon(sprite)
        }

        private fun getBottomLineIcon(): Icon? {
            val area = definitionData.area ?: return null
            val spriteName = "GFX_bottom_line_${area}"
            val sprite = ParadoxDefinitionSearch.search(spriteName, ParadoxDefinitionTypes.Sprite, selector()).find() ?: return null
            return ParadoxPresentationManager.getIcon(sprite)
        }

        private fun getCategoryIcon(): Icon? {
            val category = definitionData.category?.firstOrNull() ?: return null
            val categoryDef = ParadoxDefinitionSearch.search(category, ParadoxDefinitionTypes.TechnologyCategory, selector()).find() ?: return null
            return ParadoxPresentationManager.getIcon(categoryDef)
        }

        @Suppress("unused")
        private fun getGatewayIcon(): Icon? {
            val spriteName = "GFX_tech_gateway"
            val sprite = ParadoxDefinitionSearch.search(spriteName, ParadoxDefinitionTypes.Sprite, selector()).find() ?: return null
            return ParadoxPresentationManager.getIcon(sprite)
        }

        private fun selector(): ChainedParadoxSelector<ParadoxScriptDefinitionElement> {
            return selector(definitionInfo.project, definition).definition().contextSensitive()
        }
    }
}
