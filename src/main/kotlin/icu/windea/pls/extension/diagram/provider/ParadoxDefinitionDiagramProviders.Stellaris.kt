package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.DiagramBuilder
import com.intellij.diagram.DiagramColorManagerBase
import com.intellij.diagram.DiagramNode
import com.intellij.diagram.DiagramPresentationModel
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.ui.ColorUtil
import icu.windea.pls.core.annotations.WithGameType
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.ep.data.StellarisTechnologyData
import icu.windea.pls.extension.diagram.PlsDiagramBundle
import icu.windea.pls.extension.diagram.settings.ParadoxDiagramSettings
import icu.windea.pls.extension.diagram.settings.StellarisEventTreeDiagramSettings
import icu.windea.pls.extension.diagram.settings.StellarisTechTreeDiagramSettings
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.getData
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import java.awt.Color

@WithGameType(ParadoxGameType.Stellaris)
class StellarisEventTreeDiagramProvider : ParadoxEventTreeDiagramProvider(ParadoxGameType.Stellaris) {
    object Constants {
        const val ID = "Stellaris.EventTree"
        val ITEM_PROPERTY_KEYS = listOf("picture")
    }

    override fun getID() = Constants.ID

    override fun getPresentableName() = PlsDiagramBundle.message("eventTree.name.stellaris")

    override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel) = DataModel(project, file, this)

    override fun getDiagramSettings(project: Project) = project.service<StellarisEventTreeDiagramSettings>()

    override fun getItemPropertyKeys() = Constants.ITEM_PROPERTY_KEYS

    class DataModel(
        project: Project,
        file: VirtualFile?, //umlFile
        provider: ParadoxDefinitionDiagramProvider
    ) : ParadoxEventTreeDiagramProvider.DataModel(project, file, provider) {
        override fun showNode(definition: ParadoxScriptDefinitionElement, settings: ParadoxDiagramSettings.State): Boolean {
            if (provider !is StellarisEventTreeDiagramProvider) return true
            if (settings !is StellarisEventTreeDiagramSettings.State) return true
            val definitionInfo = definition.definitionInfo ?: return false

            //对于每组配置，只要其中任意一个配置匹配即可
            with(settings.attributeSettings) {
                val v = definitionInfo.subtypes.orNull() ?: return@with
                var enabled = false
                if (v.contains("hidden")) enabled = enabled || this.hidden
                if (v.contains("triggered")) enabled = enabled || this.triggered
                if (v.contains("major")) enabled = enabled || this.major
                if (v.contains("diplomatic")) enabled = enabled || this.diplomatic
                if (!enabled) return false
            }
            with(settings.type) {
                val v = definitionInfo.subtypes.orNull() ?: return@with
                val enabled = v.mapNotNull { this[it] }.none { !it }
                if (!enabled) return false
            }
            return true
        }
    }
}

@WithGameType(ParadoxGameType.Stellaris)
class StellarisTechTreeDiagramProvider : ParadoxTechTreeDiagramProvider(ParadoxGameType.Stellaris) {
    object Constants {
        const val ID = "Stellaris.TechTree"
        val ITEM_PROPERTY_KEYS = listOf("icon", "tier", "area", "category", "cost", "cost_per_level", "levels")
        val ITEM_PROPERTY_KEYS_IN_DETAIL = listOf("category")
    }

    private val _colorManager = ColorManager()

    override fun getID() = Constants.ID

    override fun getPresentableName() = PlsDiagramBundle.message("techTree.name.stellaris")

    override fun getColorManager() = _colorManager

    override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel) = DataModel(project, file, this)

    override fun getDiagramSettings(project: Project) = project.service<StellarisTechTreeDiagramSettings>()

    override fun getItemPropertyKeys() = Constants.ITEM_PROPERTY_KEYS

    override fun getItemPropertyKeysInDetail() = Constants.ITEM_PROPERTY_KEYS_IN_DETAIL

    class ColorManager : DiagramColorManagerBase() {
        override fun getNodeBorderColor(builder: DiagramBuilder, node: DiagramNode<*>?, isSelected: Boolean): Color {
            //基于科技领域和类型
            if (node !is Node) return super.getNodeBorderColor(builder, node, isSelected)
            return doGetNodeBorderColor(node) ?: super.getNodeBorderColor(builder, node, isSelected)
        }

        private fun doGetNodeBorderColor(node: Node): Color? {
            //这里使用的颜色是来自灰机wiki的特殊字体颜色
            //https://qunxing.huijiwiki.com/wiki/%E7%A7%91%E6%8A%80
            val data = node.getUserData(Keys.nodeData) ?: return null
            val definitionInfo = runReadAction { node.definitionInfo } ?: return null
            val types = definitionInfo.subtypes
            return when {
                types.contains("dangerous") && types.contains("rare") -> ColorUtil.fromHex("#e8514f")
                types.contains("dangerous") -> ColorUtil.fromHex("#e8514f")
                types.contains("rare") -> ColorUtil.fromHex("#9743c4")
                data.area == "physics" -> ColorUtil.fromHex("#2370af")
                data.area == "society" -> ColorUtil.fromHex("#47a05f")
                data.area == "engineering" -> ColorUtil.fromHex("#fbaa29")
                else -> null
            }
        }
    }

    class DataModel(
        project: Project,
        file: VirtualFile?, //umlFile
        provider: ParadoxDefinitionDiagramProvider
    ) : ParadoxTechTreeDiagramProvider.DataModel(project, file, provider) {
        override fun showNode(definition: ParadoxScriptDefinitionElement, settings: ParadoxDiagramSettings.State): Boolean {
            if (provider !is StellarisTechTreeDiagramProvider) return true
            if (settings !is StellarisTechTreeDiagramSettings.State) return true
            val definitionInfo = definition.definitionInfo ?: return false
            val data = definition.getData<StellarisTechnologyData>() ?: return false

            //对于每组配置，只要其中任意一个配置匹配即可
            with(settings.attributeSettings) {
                val v = definitionInfo.subtypes.orNull() ?: return@with
                var enabled = false
                if (v.contains("start")) enabled = enabled || this.start
                if (v.contains("rare")) enabled = enabled || this.rare
                if (v.contains("dangerous")) enabled = enabled || this.dangerous
                if (v.contains("insight")) enabled = enabled || this.insight
                if (v.contains("repeatable")) enabled = enabled || this.repeatable
                if (!enabled) return false
            }
            with(settings.tier) {
                val v = data.tier ?: return@with
                val enabled = this[v] ?: true
                if (!enabled) return false
            }
            with(settings.area) {
                val v = data.area ?: return@with
                val enabled = this[v] ?: true
                if (!enabled) return false
            }
            with(settings.category) {
                val v = data.category.orNull() ?: return@with
                val enabled = v.mapNotNull { this[it] }.none { !it }
                if (!enabled) return false
            }
            return true
        }
    }
}
