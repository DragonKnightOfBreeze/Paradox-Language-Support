package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.*
import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.data.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import java.awt.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisEventTreeDiagramProvider : ParadoxEventTreeDiagramProvider(ParadoxGameType.Stellaris) {
    object Constants {
        const val ID = "Stellaris.EventTree"
        val ITEM_PROPERTY_KEYS = arrayOf("picture")
    }

    override fun getID() = Constants.ID

    override fun getPresentableName() = PlsDiagramBundle.message("eventTree.name.stellaris")

    override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel) = DataModel(project, file, this)

    override fun getItemPropertyKeys() = Constants.ITEM_PROPERTY_KEYS

    override fun getDiagramSettings(project: Project) = project.service<StellarisEventTreeDiagramSettings>()

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
        val ITEM_PROPERTY_KEYS = arrayOf("icon", "tier", "area", "category", "cost", "cost_per_level", "levels")
    }

    object Keys : KeyRegistry() {
        val nodeData by createKey<StellarisTechnologyData>(Keys)
    }

    private val _colorManager = ColorManager()

    override fun getID() = Constants.ID

    override fun getPresentableName() = PlsDiagramBundle.message("techTree.name.stellaris")

    override fun getColorManager() = _colorManager

    override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel) = DataModel(project, file, this)

    override fun getItemPropertyKeys() = Constants.ITEM_PROPERTY_KEYS

    override fun getDiagramSettings(project: Project) = project.service<StellarisTechTreeDiagramSettings>()

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
