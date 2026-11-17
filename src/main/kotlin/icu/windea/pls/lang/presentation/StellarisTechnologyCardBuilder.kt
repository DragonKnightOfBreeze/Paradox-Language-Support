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
import icu.windea.pls.images.ImageDescriptor
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.getDefinitionData
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.util.ParadoxTextColorManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

/**
 * 群星科技卡构建器。
 *
 * 为了消除 UI 冻结与读锁死锁，构建过程被拆分为三个互相隔离的阶段：
 * - [loadModel]：在读锁阶段加载纯数据呈现模型（[StellarisTechnologyCardPresentationModel]）。
 * - [materializeAssets]：在**无锁**的 `Dispatchers.IO` 阶段物化渲染资产（[StellarisTechnologyCardAssets]）。
 * - [build]：在纯内存、零锁、零 IO 的情况下组装 Swing 组件。
 *
 * @see StellarisTechnologyCardPresentationModel
 * @see StellarisTechnologyCardAssets
 */
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

    /**
     * 在读锁阶段加载科技卡的纯数据呈现模型。
     *
     * 如果关键图标无法解析，返回 `null`（安全降级，而非抛出异常）。
     */
    fun loadModel(): StellarisTechnologyCardPresentationModel? {
        val definitionInfo = element.definitionInfo ?: return null
        val definitionData = element.getDefinitionData<StellarisTechnologyData>() ?: return null

        return StellarisTechnologyCardPresentationModel(
            nameText = ParadoxDefinitionPresentationUtil.getNameTextOrKey(element).orAnonymous(),
            costNumber = definitionData.cost ?: 0,
            costColor = ParadoxTextColorManager.getInfo("G", definitionInfo.project, element)?.color, // Green
            iconDescriptor = getIcon(definitionInfo) ?: return null,
            categoryIconDescriptor = getCategoryIcon(definitionInfo, definitionData),
            backgroundIconDescriptor = getBackgroundIcon(definitionInfo, definitionData) ?: return null,
            bottomLineIconDescriptor = getBottomLineIcon(definitionInfo, definitionData) ?: return null,
        )
    }

    /**
     * 在**无锁**的后台线程中物化科技卡渲染资产。
     *
     * 调用方必须确保当前不在 EDT 且不在读锁中，通常应当是 `Dispatchers.IO`。
     * 如果关键图标（[StellarisTechnologyCardAssets.icon]）无法物化，返回 `null`。
     */
    suspend fun materializeAssets(model: StellarisTechnologyCardPresentationModel): StellarisTechnologyCardAssets? {
        return withContext(Dispatchers.IO) {
            val indicator = ProgressManager.getInstance().progressIndicator ?: EmptyProgressIndicator()
            ProgressManager.getInstance().runProcess<StellarisTechnologyCardAssets?>({
                val icon = model.iconDescriptor.createImageIcon()?.resize(52, 52) ?: return@runProcess null
                val categoryIcon = model.categoryIconDescriptor?.createImageIcon()?.resize(30, 30)
                val backgroundImage = model.backgroundIconDescriptor.createImage()
                val bottomLineImage = model.bottomLineIconDescriptor.createImage()
                StellarisTechnologyCardAssets(icon, categoryIcon, backgroundImage, bottomLineImage)
            }, indicator)
        }
    }

    /**
     * 在非 EDT 且非读锁的后台线程中同步物化渲染资产。
     *
     * 该方法会阻塞当前（后台）线程，因此只应在确定安全的线程上调用。
     */
    fun materializeAssetsBlocking(model: StellarisTechnologyCardPresentationModel): StellarisTechnologyCardAssets? {
        val indicator = ProgressManager.getInstance().progressIndicator ?: EmptyProgressIndicator()
        return ProgressManager.getInstance().runProcess<StellarisTechnologyCardAssets?>({
            runBlockingCancellable { materializeAssets(model) }
        }, indicator)
    }

    /**
     * 纯内存、零锁、零 IO 地组装科技卡组件。
     */
    fun build(
        model: StellarisTechnologyCardPresentationModel,
        assets: StellarisTechnologyCardAssets,
    ): JPanel {
        val nameLabel = model.nameText.createLabel()
        val costLabel = model.costNumber.toString().createLabel(model.costColor)

        val panel = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                g as Graphics2D
                g.color = Constants.backgroundColor // 设置背景色
                g.fillRect(0, 0, 452, 97)// 填充背景色
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON) // 抗锯齿
                assets.backgroundImage?.let { g.drawImage(it, 0, 0, 452, 96, this) }
                assets.bottomLineImage?.let { g.drawImage(it, 0, 96, 452, 1, this) }
            }
        }
        panel.size = Dimension(452, 97)
        panel.preferredSize = panel.size
        panel.add(nameLabel.withLocation(6, 2)) // 6, 2
        panel.add(costLabel.withLocation(452 - 6 - costLabel.width, 2)) // 452 - 6 - width, 2
        panel.add(assets.icon.toLabel().withLocation(4, 32)) // (60 - 52) / 2, 20 + ((76 - 52) / 2)
        assets.categoryIcon?.let { categoryIcon ->
            panel.add(categoryIcon.toLabel().withLocation(452 - 6 - categoryIcon.iconWidth, 26)) // 452 - 6 - width, 20 + 6
        }

        return panel
    }

    /**
     * 为独立场景（[icu.windea.pls.ep.util.presentation.StellarisTechnologyCardPresentation]）保留的无参入口。
     *
     * 内部先以 [runSmartReadAction] 安全地加载呈现模型，再在后台线程中物化渲染资产，最后组装组件。
     */
    fun build(): JComponent? {
        val model = runSmartReadAction { loadModel() } ?: return null
        val assets = ApplicationManager.getApplication().executeOnPooledThread<StellarisTechnologyCardAssets?> {
            val indicator = ProgressManager.getInstance().progressIndicator ?: EmptyProgressIndicator()
            ProgressManager.getInstance().runProcess<StellarisTechnologyCardAssets?>({
                runBlockingCancellable { materializeAssets(model) }
            }, indicator)
        }.get() ?: return null
        return build(model, assets)
    }

    private fun getIcon(definitionInfo: ParadoxDefinitionInfo): ImageDescriptor? {
        return ParadoxDefinitionPresentationUtil.resolveIcon(element) ?: getUnknownIcon(definitionInfo)
    }

    private fun getUnknownIcon(definitionInfo: ParadoxDefinitionInfo): ImageDescriptor? {
        val selector = ParadoxDefinitionSearch.selector(definitionInfo.project, element).contextSensitive()
        val sprite = ParadoxDefinitionSearch.searchProperty("GFX_technology_unknown", ParadoxDefinitionTypes.sprite, selector).find() ?: return null
        return ParadoxDefinitionPresentationUtil.resolveIcon(sprite)
    }

    private fun getBackgroundIcon(definitionInfo: ParadoxDefinitionInfo, definitionData: StellarisTechnologyData): ImageDescriptor? {
        val area = definitionData.area ?: return null
        val types = definitionInfo.subtypes
        val spriteName = when {
            "dangerous" in types && "rare" in types -> "GFX_tech_entry_dangerous_rare_bg"
            "dangerous" in types -> "GFX_tech_entry_dangerous_bg"
            "rare" in types -> "GFX_tech_entry_rare_bg"
            else -> "GFX_tech_entry_${area}_bg"
        }
        val selector = ParadoxDefinitionSearch.selector(definitionInfo.project, element).contextSensitive()
        val sprite = ParadoxDefinitionSearch.searchProperty(spriteName, ParadoxDefinitionTypes.sprite, selector).find() ?: return null
        return ParadoxDefinitionPresentationUtil.resolveIcon(sprite)
    }

    private fun getBottomLineIcon(definitionInfo: ParadoxDefinitionInfo, definitionData: StellarisTechnologyData): ImageDescriptor? {
        val area = definitionData.area ?: return null
        val spriteName = "GFX_bottom_line_${area}"
        val selector = ParadoxDefinitionSearch.selector(definitionInfo.project, element).contextSensitive()
        val sprite = ParadoxDefinitionSearch.searchProperty(spriteName, ParadoxDefinitionTypes.sprite, selector).find() ?: return null
        return ParadoxDefinitionPresentationUtil.resolveIcon(sprite)
    }

    private fun getCategoryIcon(definitionInfo: ParadoxDefinitionInfo, definitionData: StellarisTechnologyData): ImageDescriptor? {
        val category = definitionData.category?.firstOrNull() ?: return null
        val selector = ParadoxDefinitionSearch.selector(definitionInfo.project, element).contextSensitive()
        val categoryDef = ParadoxDefinitionSearch.searchProperty(category, ParadoxDefinitionTypes.technologyCategory, selector).find() ?: return null
        return ParadoxDefinitionPresentationUtil.resolveIcon(categoryDef)
    }

    private fun String.createLabel(color: Color? = null): JLabel {
        return ParadoxDefinitionPresentationUtil.getLabel(this, color)
    }
}
