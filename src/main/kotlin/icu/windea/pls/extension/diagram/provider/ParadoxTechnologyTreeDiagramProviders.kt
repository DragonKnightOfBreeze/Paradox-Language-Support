package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.*
import com.intellij.openapi.components.*
import com.intellij.openapi.util.*
import com.intellij.ui.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.settings.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.awt.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisTechnologyTreeDiagramProvider : ParadoxTechnologyTreeDiagramProvider(ParadoxGameType.Stellaris) {
    companion object {
        const val ID = "Stellaris.TechnologyTree"
        val nodeDataKey = Key.create<StellarisTechnologyDataProvider.Data>("paradox.technologyTree.node.data")
    }
    
    private val _colorManager = ColorManager()
    
    private val _itemPropertyKeys = arrayOf(
        "icon",
        "tier", "area", "category",
        "cost", "cost_per_level", "levels",
        "start_tech", "is_rare", "is_dangerous"
    )
    
    override fun getID() = ID
    
    override fun getColorManager() = _colorManager
    
    override fun getItemPropertyKeys() = _itemPropertyKeys
    
    override fun getDiagramSettings() = service<StellarisTechlonogyTreeDiagramSettings>()
    
    override fun showNode(definition: ParadoxScriptDefinitionElement): Boolean {
        if(definition !is ParadoxScriptProperty) return false
        val data = definition.getData<StellarisTechnologyDataProvider.Data>()
        if(data == null) return true
        val settings = getDiagramSettings().state
        
        val start = data.start_tech
        val rare = data.is_rare
        val dangerous = data.is_dangerous
        val insight = data.is_insight
        val repeatable = data.levels != null
        val other = !start && !rare && !dangerous && !insight && !repeatable
        
        //对于每组配置，只要其中任意一个配置匹配即可
        with(settings.type) {
            var enabled = false
            if(start) enabled = enabled || this.start
            if(rare) enabled = enabled || this.rare
            if(dangerous) enabled = enabled || this.dangerous
            if(insight) enabled = enabled || this.insight
            if(repeatable) enabled = enabled || this.repeatable
            if(other) enabled = enabled || this.other
            if(!enabled) return false
        }
        with(settings.tier) {
            val v = data.tier ?: return@with
            val enabled = this[v] ?: true
            if(!enabled) return false
        }
        with(settings.area) {
            val v = data.area ?: return@with
            val enabled = this[v] ?: true
            if(!enabled) return false
        }
        with(settings.category) {
            val v = data.category ?: return@with
            val enabled = v.any { this[it] ?: true }
            if(!enabled) return false
        }
        return true
    }
    
    override fun handleNode(node: ParadoxDefinitionDiagramProvider.Node) {
        putDefinitionData(node, nodeDataKey)
    }
    
    class ColorManager : DiagramColorManagerBase() {
        override fun getNodeBorderColor(builder: DiagramBuilder, node: DiagramNode<*>?, isSelected: Boolean): Color {
            //基于科技领域和类型
            if(node !is Node) return super.getNodeBorderColor(builder, node, isSelected)
            return doGetNodeBorderColor(node) ?: super.getNodeBorderColor(builder, node, isSelected)
        }
        
        private fun doGetNodeBorderColor(node: Node): Color? {
            //这里使用的颜色是来自灰机wiki的特殊字体颜色
            //https://qunxing.huijiwiki.com/wiki/%E7%A7%91%E6%8A%80
            val data = node.getUserData(nodeDataKey) ?: return null
            return when {
                data.is_dangerous && data.is_rare -> ColorUtil.fromHex("#e8514f")
                data.is_dangerous -> ColorUtil.fromHex("#e8514f")
                data.is_rare -> ColorUtil.fromHex("#9743c4")
                data.area == "physics" -> ColorUtil.fromHex("#2370af")
                data.area == "society" -> ColorUtil.fromHex("#47a05f")
                data.area == "engineering" -> ColorUtil.fromHex("#fbaa29")
                else -> null
            }
        }
    }
}