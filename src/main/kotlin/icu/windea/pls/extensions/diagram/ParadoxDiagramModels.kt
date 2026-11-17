package icu.windea.pls.extensions.diagram

import com.intellij.diagram.DiagramBuilder
import com.intellij.psi.PsiElement
import com.intellij.ui.SimpleColoredText
import icu.windea.pls.ep.util.data.StellarisTechnologyData
import icu.windea.pls.lang.presentation.StellarisTechnologyCardAssets
import icu.windea.pls.lang.presentation.StellarisTechnologyCardPresentationModel
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 图表节点属性的预加载项。
 *
 * [text] 已经在读锁阶段预先计算完成，读取它时无需再访问 PSI，也无需加读锁。
 */
data class ParadoxDiagramPropertyItem(val text: SimpleColoredText)

/**
 * 图表节点的轻量展示模型。
 *
 * 该模型在 `DataModel.updateDataModel()` 的读锁阶段一次性提取所需的全部展示数据，
 * 之后元素管理器与颜色管理器可以直接以零锁、零 IO 的方式读取它。
 *
 * @property title 节点标题（事件名或科技名）。
 * @property typeText 类型文本。
 * @property presentableName 展示名字（通常来自本地化）。
 * @property subtypes 定义子类型，用于计算节点边框颜色。
 * @property properties 预加载的属性列表。
 * @property techData 科技数据（仅科技树节点）。
 * @property cardModel 科技卡呈现模型（仅科技树节点，纯数据，可在读锁阶段预加载）。
 */
class ParadoxDefinitionDiagramNodeModel(
    val title: String,
    val typeText: String?,
    val presentableName: String?,
    val subtypes: Set<String>,
    val properties: List<ParadoxDiagramPropertyItem>,
    val techData: StellarisTechnologyData? = null,
    val cardModel: StellarisTechnologyCardPresentationModel? = null,
) {
    /**
     * 科技卡渲染资产。必须在无锁的后台线程中物化，物化完成后缓存于此。
     */
    @Volatile
    var cardAssets: StellarisTechnologyCardAssets? = null

    /**
     * 是否已经有一个后台任务正在物化科技卡渲染资产。
     */
    val cardAssetsLoading = AtomicBoolean(false)

    /**
     * 科技卡渲染资产是否已经尝试物化但失败。
     */
    @Volatile
    var cardAssetsFailed: Boolean = false
}

/**
 * 以零锁的方式从 [DiagramBuilder] 对应的数据模型中取出指定元素的预加载节点展示模型。
 */
fun DiagramBuilder.getDiagramNodeModel(element: PsiElement): ParadoxDefinitionDiagramNodeModel? {
    return (dataModel as? ParadoxDiagramDataModel)?.getNodeModel(element)
}
