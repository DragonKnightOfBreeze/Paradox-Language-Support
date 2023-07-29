package icu.windea.pls.extension.diagram.provider.impl

import com.intellij.diagram.*
import com.intellij.openapi.components.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.provider.*
import icu.windea.pls.extension.diagram.settings.impl.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.data.impl.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.awt.*

private const val ID = "Stellaris.TechnologyTree"

private val ITEM_PROPERTY_KEYS = arrayOf("icon", "tier", "area", "category", "cost", "cost_per_level", "levels")

private val nodeDataKey = Key.create<StellarisTechnologyDataProvider.Data>("stellaris.technologyTree.node.data")

@WithGameType(ParadoxGameType.Stellaris)
class StellarisTechnologyTreeDiagramProvider : ParadoxTechnologyTreeDiagramProvider(ParadoxGameType.Stellaris) {
    private val _colorManager = ColorManager()
    
    override fun getID() = ID
    
    @Suppress("DialogTitleCapitalization")
    override fun getPresentableName() = PlsDiagramBundle.message("stellaris.technologyTree.name")
    
    override fun getColorManager() = _colorManager
    
    override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel) = DataModel(project, file, this)
    
    override fun getItemPropertyKeys() = ITEM_PROPERTY_KEYS
    
    override fun getDiagramSettings(project: Project) = project.service<StellarisTechnologyTreeDiagramSettings>()
    
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
            val definitionInfo = node.definitionInfo ?: return null
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
    ) : ParadoxTechnologyTreeDiagramProvider.DataModel(project, file, provider) {
        override fun updateDataModel(indicator: ProgressIndicator?) {
            provider as StellarisTechnologyTreeDiagramProvider
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
            
            val definitionInfo = definition.definitionInfo ?: return false
            val data = definition.getData<StellarisTechnologyDataProvider.Data>() ?: return false
            val settings = provider.getDiagramSettings(project).state
            
            //对于每组配置，只要其中任意一个配置匹配即可
            with(settings.typeSettings) {
                val v = definitionInfo.subtypes.takeIfNotEmpty() ?: return@with
                var enabled = false
                if(v.contains("start")) enabled = enabled || this.start
                if(v.contains("rare")) enabled = enabled || this.rare
                if(v.contains("dangerous")) enabled = enabled || this.dangerous
                if(v.contains("insight")) enabled = enabled || this.insight
                if(v.contains("repeatable")) enabled = enabled || this.repeatable
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
                val v = data.category.takeIfNotEmpty() ?: return@with
                val enabled = v.any { this[it] ?: false }
                if(!enabled) return false
            }
            return true
        }
    }
}