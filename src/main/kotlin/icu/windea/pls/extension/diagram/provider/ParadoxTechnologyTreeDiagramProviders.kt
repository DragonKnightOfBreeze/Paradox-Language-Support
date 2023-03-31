package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.*
import com.intellij.diagram.settings.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.awt.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisTechnologyTreeDiagramProvider : ParadoxTechnologyTreeDiagramProvider(ParadoxGameType.Stellaris) {
    companion object {
        val nodeDataKey = Key.create<StellarisTechnologyDataProvider.Data>("paradox.technologyTree.node.data")
    }
    
    private val _colorManager = ColorManager()
    
    private val _itemPropertyKeys = arrayOf(
        "icon",
        "tier", "area", "category",
        "cost", "cost_per_level", "levels",
        "start_tech", "is_rare", "is_dangerous"
    )
    
    private val _addtionalDiagramSettings = buildList {
        DiagramConfigGroup(PlsDiagramBundle.message("stellaris.technologyTree.settings.type")).apply {
            addElement(DiagramConfigElement(PlsDiagramBundle.message("stellaris.technologyTree.settings.type.start"), true))
            addElement(DiagramConfigElement(PlsDiagramBundle.message("stellaris.technologyTree.settings.type.rare"), true))
            addElement(DiagramConfigElement(PlsDiagramBundle.message("stellaris.technologyTree.settings.type.dangerous"), true))
            addElement(DiagramConfigElement(PlsDiagramBundle.message("stellaris.technologyTree.settings.type.repeatable"), true))
            addElement(DiagramConfigElement(PlsDiagramBundle.message("stellaris.technologyTree.settings.type.other"), true))
        }.also { add(it) }
        //NOTE tier和category应当是动态获取的
        //NOTE 这里我们无法直接获得project，因此暂且合并所有已打开的项目
        //NOTE 这里的设置名不能包含本地化名字，因为这里的设置名同时也作为设置的ID
        DiagramConfigGroup(PlsDiagramBundle.message("stellaris.technologyTree.settings.tier")).apply {
            val tiers = ProjectManager.getInstance().openProjects.flatMap { project ->
                StellarisTechnologyHandler.getTechnologyTiers(project, null)
            }
            tiers.forEach {
                addElement(DiagramConfigElement(PlsDiagramBundle.message("stellaris.technologyTree.settings.tier.option", it.name), true))
            }
        }.also { add(it) }
        DiagramConfigGroup(PlsDiagramBundle.message("stellaris.technologyTree.settings.area")).apply {
            val areas = StellarisTechnologyHandler.getResearchAreas()
            areas.forEach {
                addElement(DiagramConfigElement(PlsDiagramBundle.message("stellaris.technologyTree.settings.area.option", it), true))
            }
        }.also { add(it) }
        DiagramConfigGroup(PlsDiagramBundle.message("stellaris.technologyTree.settings.category")).apply {
            val categories = ProjectManager.getInstance().openProjects.flatMap { project ->
                StellarisTechnologyHandler.getTechnologyCategories(project, null)
            }
            categories.forEach {
                addElement(DiagramConfigElement(PlsDiagramBundle.message("stellaris.technologyTree.settings.category.option", it.name), true))
            }
        }.also { add(it) }
    }.toTypedArray()
    
    override fun getColorManager() = _colorManager
    
    override fun getItemPropertyKeys() = _itemPropertyKeys
    
    override fun getAdditionalDiagramSettings() = _addtionalDiagramSettings
    
    override fun showNode(element: ParadoxScriptDefinitionElement): Boolean {
        if(element !is ParadoxScriptProperty) return false
        val data = element.getData<StellarisTechnologyDataProvider.Data>()
        if(data == null) return true
        val settings = getAdditionalDiagramSettings()
        if(settings.isEmpty()) return true
        val configuration = DiagramConfiguration.getInstance()
        
        val start = data.start_tech
        val rare = data.is_rare
        val dangerous = data.is_dangerous
        val repeatable = data.levels != null
        val other = !start && !rare && !dangerous && !repeatable
        
        //对于每组配置，只要其中任意一个配置匹配即可
        for(setting in settings) {
            when(setting.name) {
                PlsDiagramBundle.message("stellaris.technologyTree.settings.type") -> {
                    val enabled = setting.elements.any { config ->
                        val e = configuration.isEnabledByDefault(this, config.name)
                        when(config.name) {
                            PlsDiagramBundle.message("stellaris.technologyTree.settings.type.start") -> if(start) e else false
                            PlsDiagramBundle.message("stellaris.technologyTree.settings.type.rare") -> if(rare) e else false
                            PlsDiagramBundle.message("stellaris.technologyTree.settings.type.dangerous") -> if(dangerous) e else false
                            PlsDiagramBundle.message("stellaris.technologyTree.settings.type.repeatable") -> if(repeatable) e else false
                            PlsDiagramBundle.message("stellaris.technologyTree.settings.type.other") -> if(other) e else false
                            else -> false
                        }
                    }
                    if(!enabled) return false
                }
                PlsDiagramBundle.message("stellaris.technologyTree.settings.tier") -> {
                    val v = data.tier ?: return false
                    val configName = PlsDiagramBundle.message("stellaris.technologyTree.settings.tier.option", v)
                    val enabled = configuration.isEnabledByDefault(this, configName)
                    if(!enabled) return false
                }
                PlsDiagramBundle.message("stellaris.technologyTree.settings.area") -> {
                    val v = data.area ?: return false
                    val configName = PlsDiagramBundle.message("stellaris.technologyTree.settings.area.option", v)
                    val enabled = configuration.isEnabledByDefault(this, configName)
                    if(!enabled) return false
                }
                PlsDiagramBundle.message("stellaris.technologyTree.settings.category") -> {
                    val v = data.category.orEmpty()
                    val configNames = v.map { PlsDiagramBundle.message("stellaris.technologyTree.settings.category.option", it) }
                    val enabled = configNames.any { configName -> configuration.isEnabledByDefault(this, configName) }
                    if(!enabled) return false
                }
            }
        }
        return true
    }
    
    override fun handleNode(node: ParadoxDefinitionDiagramNode) {
        putDefinitionData(node, nodeDataKey)
    }
    
    class ColorManager : ParadoxTechnologyTreeDiagramProvider.ColorManager() {
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