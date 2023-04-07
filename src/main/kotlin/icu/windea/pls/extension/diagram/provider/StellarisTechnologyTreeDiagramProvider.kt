package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.*
import com.intellij.openapi.components.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.extension.diagram.settings.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.awt.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisTechnologyTreeDiagramProvider : ParadoxTechnologyTreeDiagramProvider(ParadoxGameType.Stellaris) {
    companion object {
        const val ID = "Stellaris.TechnologyTree"
        
        val ITEM_PROPERTY_KEYS = arrayOf(
            "icon",
            "tier", "area", "category",
            "cost", "cost_per_level", "levels",
            "start_tech", "is_rare", "is_dangerous"
        )
        
        val nodeDataKey = Key.create<StellarisTechnologyDataProvider.Data>("stellaris.technologyTree.node.data")
    }
    
    private val _colorManager = ColorManager()
    
    override fun getID() = ID
    
    override fun getColorManager() = _colorManager
    
    override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel) = DataModel(project, file, this)
    
    override fun getItemPropertyKeys() = ITEM_PROPERTY_KEYS
    
    override fun getDiagramSettings() = service<StellarisTechnologyTreeDiagramSettings>()
    
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
    
    class DataModel(
        project: Project,
        file: VirtualFile?, //umlFile
        provider: ParadoxDefinitionDiagramProvider
    ) : ParadoxTechnologyTreeDiagramProvider.DataModel(project, file, provider) {
        override fun refreshDataModel() {
            provider as StellarisTechnologyTreeDiagramProvider
            
            ProgressManager.checkCanceled()
            nodes.clear()
            edges.clear()
            val technologies = getDefinitions("technology")
            if(technologies.isEmpty()) return
            //群星原版科技有400+
            val nodeMap = mutableMapOf<ParadoxScriptDefinitionElement, Node>()
            val techMap = mutableMapOf<String, ParadoxScriptDefinitionElement>()
            for(technology in technologies) {
                ProgressManager.checkCanceled()
                if(!showNode(technology)) continue
                val node = Node(technology, provider)
                node.putUserData(nodeDataKey, technology.getData())
                nodeMap.put(technology, node)
                val name = technology.definitionInfo?.name.orAnonymous()
                techMap.put(name, technology)
                nodes.add(node)
            }
            for(technology in technologies) {
                ProgressManager.checkCanceled()
                val data = technology.getData<StellarisTechnologyDataProvider.Data>() ?: continue
                //循环科技 ..> 循环科技
                val levels = data.levels
                if(levels != null) {
                    val label = if(levels <= 0) "max level: inf" else "max level: $levels"
                    val node = nodeMap.get(technology) ?: continue
                    val edge = Edge(node, node, REL_REPEAT(label))
                    edges.add(edge)
                }
                //前置 --> 科技
                val prerequisites = data.prerequisites
                if(prerequisites.isNotEmpty()) {
                    for(prerequisite in prerequisites) {
                        val source = techMap.get(prerequisite)?.let { nodeMap.get(it) } ?: continue
                        val target = nodeMap.get(technology) ?: continue
                        val edge = Edge(source, target, REL_PREREQUISITE)
                        edges.add(edge)
                    }
                }
            }
        }
        
        private fun showNode(definition: ParadoxScriptDefinitionElement): Boolean {
            provider as StellarisTechnologyTreeDiagramProvider
            
            if(definition !is ParadoxScriptProperty) return false
            val data = definition.getData<StellarisTechnologyDataProvider.Data>()
            if(data == null) return true
            val settings = provider.getDiagramSettings().state
            
            val start = data.start_tech
            val rare = data.is_rare
            val dangerous = data.is_dangerous
            val insight = data.is_insight
            val repeatable = data.levels != null
            val other = !start && !rare && !dangerous && !insight && !repeatable
            
            //对于每组配置，只要其中任意一个配置匹配即可
            with(settings.typeSettings) {
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
    }
}