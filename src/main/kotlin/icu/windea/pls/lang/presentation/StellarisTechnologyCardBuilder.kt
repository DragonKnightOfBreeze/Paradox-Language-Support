package icu.windea.pls.lang.presentation

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.ui.Gray
import icu.windea.pls.core.orAnonymous
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.core.ui.resize
import icu.windea.pls.core.ui.toLabel
import icu.windea.pls.core.ui.withLocation
import icu.windea.pls.ep.util.data.StellarisTechnologyData
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.getDefinitionData
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.resolve.ParadoxImageResolveResult
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.util.ParadoxTextColorManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.RenderingHints
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

// TODO 3.0.x further refactor

@Suppress("unused")
class StellarisTechnologyCardBuilder(
    val element: ParadoxDefinitionElement,
    val project: Project,
) {
    // GFX_technology_unknown 52*52
    // GFX_technology_xxx 52*52

    // GFX_bottom_line_physics 533*1
    // GFX_bottom_line_society 533*1
    // GFX_bottom_line_engineering 533*1

    // 背景
    // GFX_tech_entry_physics_bg 452*96
    // GFX_tech_entry_society_bg 452*96
    // GFX_tech_entry_engineering_bg 452*96
    // GFX_tech_entry_rare_bg 452*96
    // GFX_tech_entry_dangerous_bg 452*96
    // GFX_tech_entry_dangerous_rare_bg 452*96

    // 突破图标
    // GFX_tech_gateway 24*24

    // 标题 - 左上 - 白色
    // 费用 - 右上 - 绿色

    @Suppress("UseJBColor")
    object Constants {
        val backgroundColor = Gray._34
        val whiteColor = Color.WHITE
    }

    private lateinit var definitionInfo: ParadoxDefinitionInfo
    private lateinit var definitionData: StellarisTechnologyData

    private lateinit var nameText: String
    private var costNumber: Int = 0
    private var costColor: Color? = null

    private lateinit var backgroundIconDescriptor: ParadoxImageResolveResult
    private lateinit var bottomLineIconDescriptor: ParadoxImageResolveResult
    private lateinit var iconDescriptor: ParadoxImageResolveResult
    private var categoryIconDescriptor: ParadoxImageResolveResult? = null

    private var icon: Icon? = null
    private var categoryIcon: Icon? = null
    private var backgroundImage: Image? = null
    private var bottomLineImage: Image? = null

    fun build(): JComponent? {
        // NOTE 3.0.5 cannot use `runWithModalProgressBlocking` here or will cause infinite refreshing in EDT (why?), so need some workaround here

        runSmartReadAction {
            loadData()
        }

        return render()
    }

    private fun loadData() {
        definitionInfo = element.definitionInfo ?: throw IllegalArgumentException("Cannot get definition info")
        definitionData = element.getDefinitionData<StellarisTechnologyData>() ?: throw IllegalStateException("Cannot get definition data")

        nameText = getNameText()
        costNumber = getCostNumber()
        costColor = getCostColor()

        iconDescriptor = getIcon() ?: throw IllegalArgumentException("Cannot resolve icon")
        backgroundIconDescriptor = getBackgroundIcon() ?: throw IllegalArgumentException("Cannot resolve background icon")
        bottomLineIconDescriptor = getBottomLineIcon() ?: throw IllegalArgumentException("Cannot resolve bottom line icon")
        categoryIconDescriptor = getCategoryIcon()
    }

    private fun render(): JPanel? {
        val r = ApplicationManager.getApplication().executeOnPooledThread<Boolean> {
            ProgressManager.getInstance().runProcess<Boolean>({
                runBlockingCancellable {
                    icon = iconDescriptor.createImageIcon()?.resize(52, 52) ?: return@runBlockingCancellable false
                    categoryIcon = categoryIconDescriptor?.createImageIcon()?.resize(30, 30)
                    backgroundImage = backgroundIconDescriptor.createImage()
                    bottomLineImage = bottomLineIconDescriptor.createImage()
                    true
                }
            }, EmptyProgressIndicator())
        }.get()
        if (!r) return null
        val icon = icon ?: return null

        val nameLabel = nameText.createLabel()
        val costLabel = costNumber.toString().createLabel(costColor)

        val panel = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                g as Graphics2D
                g.color = Constants.backgroundColor // 设置背景色
                g.fillRect(0, 0, 452, 97)// 填充背景色
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON) // 抗锯齿
                g.drawImage(backgroundImage, 0, 0, 452, 96, this)
                g.drawImage(bottomLineImage, 0, 96, 452, 1, this)
            }
        }
        panel.size = Dimension(452, 97)
        panel.preferredSize = panel.size
        panel.add(nameLabel.withLocation(6, 2)) // 6, 2
        panel.add(costLabel.withLocation(452 - 6 - costLabel.width, 2)) // 452 - 6 - width, 2
        panel.add(icon.toLabel().withLocation(4, 32)) // (60 - 52) / 2, 20 + ((76 - 52) / 2)
        categoryIcon?.let { categoryIcon ->
            panel.add(categoryIcon.toLabel().withLocation(452 - 6 - categoryIcon.iconWidth, 26)) // 452 - 6 - width, 20 + 6
        }

        return panel
    }

    private fun getNameText(): String {
        return ParadoxDefinitionPresentationUtil.getNameTextOrKey(element).orAnonymous()
    }

    private fun getCostNumber(): Int {
        return element.getDefinitionData<StellarisTechnologyData>()?.cost ?: 0
    }

    private fun getCostColor(): Color? {
        return ParadoxTextColorManager.getInfo("G", definitionInfo.project, element)?.color // Green
    }

    private fun getIcon(): ParadoxImageResolveResult? {
        return ParadoxDefinitionPresentationUtil.resolveIcon(element) ?: getUnknownIcon()
    }

    private fun getUnknownIcon(): ParadoxImageResolveResult? {
        val selector = ParadoxDefinitionSearch.selector(definitionInfo.project, element).contextSensitive()
        val sprite = ParadoxDefinitionSearch.searchProperty("GFX_technology_unknown", ParadoxDefinitionTypes.sprite, selector).find() ?: return null
        return ParadoxDefinitionPresentationUtil.resolveIcon(sprite)
    }

    private fun getBackgroundIcon(): ParadoxImageResolveResult? {
        val area = definitionData.area ?: return null
        val types = definitionInfo.subtypes
        val spriteName = when {
            types.contains("dangerous") && types.contains("rare") -> "GFX_tech_entry_dangerous_rare_bg"
            types.contains("dangerous") -> "GFX_tech_entry_dangerous_bg"
            types.contains("rare") -> "GFX_tech_entry_rare_bg"
            else -> "GFX_tech_entry_${area}_bg"
        }
        val selector = ParadoxDefinitionSearch.selector(definitionInfo.project, element).contextSensitive()
        val sprite = ParadoxDefinitionSearch.searchProperty(spriteName, ParadoxDefinitionTypes.sprite, selector).find() ?: return null
        return ParadoxDefinitionPresentationUtil.resolveIcon(sprite)
    }

    private fun getBottomLineIcon(): ParadoxImageResolveResult? {
        val area = definitionData.area ?: return null
        val spriteName = "GFX_bottom_line_${area}"
        val selector = ParadoxDefinitionSearch.selector(definitionInfo.project, element).contextSensitive()
        val sprite = ParadoxDefinitionSearch.searchProperty(spriteName, ParadoxDefinitionTypes.sprite, selector).find() ?: return null
        return ParadoxDefinitionPresentationUtil.resolveIcon(sprite)
    }

    private fun getCategoryIcon(): ParadoxImageResolveResult? {
        val category = definitionData.category?.firstOrNull() ?: return null
        val selector = ParadoxDefinitionSearch.selector(definitionInfo.project, element).contextSensitive()
        val categoryDef = ParadoxDefinitionSearch.searchProperty(category, ParadoxDefinitionTypes.technologyCategory, selector).find() ?: return null
        return ParadoxDefinitionPresentationUtil.resolveIcon(categoryDef)
    }

    private fun getGatewayIcon(): ParadoxImageResolveResult? {
        val spriteName = "GFX_tech_gateway"
        val selector = ParadoxDefinitionSearch.selector(definitionInfo.project, element).contextSensitive()
        val sprite = ParadoxDefinitionSearch.searchProperty(spriteName, ParadoxDefinitionTypes.sprite, selector).find()
        if (sprite == null) return null
        return ParadoxDefinitionPresentationUtil.resolveIcon(sprite)
    }

    private fun String.createLabel(color: Color? = null): JLabel {
        return ParadoxDefinitionPresentationUtil.getLabel(this, color)
    }
}
