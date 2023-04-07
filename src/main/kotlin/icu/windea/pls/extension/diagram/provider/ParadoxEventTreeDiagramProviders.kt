package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.*
import com.intellij.openapi.components.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

class StellarisEventTreeDiagramProvider : ParadoxEventTreeDiagramProvider(ParadoxGameType.Stellaris) {
    companion object {
        const val ID = "Stellaris.EventTree"
        val nodeDataKey = Key.create<ParadoxEventDataProvider.Data>("paradox.eventTree.node.data")
    }
    
    private val _itemPropertyKeys = arrayOf(
        "picture",
        "hide_window", "is_triggered_only", "major", "diplomatic"
    )
    
    override fun getID() = ID
    
    override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel) = DataModel(project, file, this)
    
    override fun getItemPropertyKeys() = _itemPropertyKeys
    
    override fun getDiagramSettings() = service<StellarisEventTreeDiagramSettings>()
    
    class DataModel(
        project: Project,
        file: VirtualFile?, //umlFile
        provider: ParadoxDefinitionDiagramProvider
    ) : ParadoxEventTreeDiagramProvider.DataModel(project, file, provider) {
        override fun refreshDataModel() {
            provider as StellarisEventTreeDiagramProvider
            
            ProgressManager.checkCanceled()
            nodes.clear()
            edges.clear()
            val events = getDefinitions("events")
            if(events.isEmpty()) return
            //群星原版事件有5000+
            val nodeMap = mutableMapOf<ParadoxScriptDefinitionElement, Node>()
            val eventMap = mutableMapOf<String, ParadoxScriptDefinitionElement>()
            for(event in events) {
                ProgressManager.checkCanceled()
                if(!showNode(event)) continue
                val node = Node(event, provider)
                putDefinitionData(node, nodeDataKey)
                nodeMap.put(event, node)
                val name = event.definitionInfo?.name.orAnonymous()
                eventMap.put(name, event)
                nodes.add(node)
            }
            for(event in events) {
                ProgressManager.checkCanceled()
                val invocations = ParadoxEventHandler.getInvocations(event)
                if(invocations.isEmpty()) continue
                //事件 --> 调用的事件
                for((invocation, invocationType) in invocations) {
                    ProgressManager.checkCanceled()
                    val source = nodeMap.get(event) ?: continue
                    val target = eventMap.get(invocation)?.let { nodeMap.get(it) } ?: continue
                    val relationship = when(invocationType) {
                        ParadoxEventHandler.InvocationType.All -> REL_INVOKE
                        ParadoxEventHandler.InvocationType.Immediate -> REL_INVOKE_IMMEDIATE
                        ParadoxEventHandler.InvocationType.After -> REL_INVOKE_AFTER
                    }
                    val edge = Edge(source, target, relationship, invocationType)
                    edges.add(edge)
                }
            }
        }
        
        private fun showNode(definition: ParadoxScriptDefinitionElement): Boolean {
            provider as StellarisEventTreeDiagramProvider
            
            if(definition !is ParadoxScriptProperty) return false
            val data = definition.getData<ParadoxEventDataProvider.Data>()
            if(data == null) return true
            val settings = provider.getDiagramSettings().state
            
            val hidden = data.hide_window
            val triggered = data.is_triggered_only
            val major = data.major
            val diplomatic = data.diplomatic
            val other = !hidden && !triggered && !major && !diplomatic
            
            //对于每组配置，只要其中任意一个配置匹配即可
            with(settings.type) {
                var enabled = false
                if(hidden) enabled = enabled || this.hidden
                if(triggered) enabled = enabled || this.triggered
                if(major) enabled = enabled || this.major
                if(diplomatic) enabled = enabled || this.diplomatic
                if(other) enabled = enabled || this.other
                if(!enabled) return false
            }
            return true
        }
    }
}