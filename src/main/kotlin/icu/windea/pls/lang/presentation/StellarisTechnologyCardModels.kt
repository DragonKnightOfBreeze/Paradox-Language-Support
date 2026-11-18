package icu.windea.pls.lang.presentation

import icu.windea.pls.images.ImageDescriptor
import java.awt.Color
import java.awt.Image
import javax.swing.Icon

/**
 * 科技卡呈现模型（纯数据）。
 *
 * 只包含可在读锁阶段安全预加载的数据，不持有任何 Swing 渲染资产。
 * 渲染资产由 [StellarisTechnologyCardAssets] 承载，并应在无锁的后台线程中物化。
 *
 * @property nameText 标题文本。
 * @property costNumber 费用数值。
 * @property costColor 费用颜色。
 * @property iconDescriptor 图标描述符。
 * @property categoryIconDescriptor 分类图标描述符。
 * @property backgroundIconDescriptor 背景图标描述符。
 * @property bottomLineIconDescriptor 底部横线图标描述符。
 */
data class StellarisTechnologyCardPresentationModel(
    val nameText: String,
    val costNumber: Int,
    val costColor: Color?,
    val iconDescriptor: ImageDescriptor,
    val categoryIconDescriptor: ImageDescriptor?,
    val backgroundIconDescriptor: ImageDescriptor,
    val bottomLineIconDescriptor: ImageDescriptor,
)

/**
 * 科技卡渲染资产（纯 Swing）。
 *
 * 必须在无锁的后台线程（通常是 `Dispatchers.IO`）中物化，之后才能用于组装组件。
 *
 * @property icon 图标（必须存在）。
 * @property categoryIcon 分类图标。
 * @property backgroundImage 背景图片。
 * @property bottomLineImage 底部横线图片。
 */
data class StellarisTechnologyCardAssets(
    val icon: Icon,
    val categoryIcon: Icon?,
    val backgroundImage: Image?,
    val bottomLineImage: Image?,
)
