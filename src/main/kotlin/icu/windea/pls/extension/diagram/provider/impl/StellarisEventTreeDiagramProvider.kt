package icu.windea.pls.extension.diagram.provider.impl

import com.intellij.diagram.*
import com.intellij.openapi.components.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.extension.diagram.provider.*
import icu.windea.pls.extension.diagram.settings.impl.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.data.impl.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.awt.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisEventTreeDiagramProvider : ParadoxEventTreeDiagramProvider(ParadoxGameType.Stellaris) {
    companion object {
        const val ID = "Stellaris.EventTree"
        
        val ITEM_PROPERTY_KEYS = arrayOf("picture")
        
        val nodeDataKey = Key.create<StellarisEventDataProvider.Data>("stellaris.eventTree.node.data")
        val invocationTypeKey = Key.create<ParadoxEventHandler.InvocationType>("stellaris.eventTree.edge.invocationType")
    }
    
    private val _colorManager = ColorManager()
    
    override fun getID() = ID
    
    override fun getColorManager() = _colorManager
    
    override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel) = DataModel(project, file, this)
    
    override fun getItemPropertyKeys() = ITEM_PROPERTY_KEYS
    
    override fun getDiagramSettings(project: Project) = project.service<StellarisEventTreeDiagramSettings>()
    
    class ColorManager : DiagramColorManagerBase() {
        override fun getEdgeColor(builder: DiagramBuilder, edge: DiagramEdge<*>): Color {
            if(edge !is Edge) return super.getEdgeColor(builder, edge)
            //基于调用类型
            return doGetEdgeColor(edge) ?: super.getEdgeColor(builder, edge)
        }
        
        private fun doGetEdgeColor(edge: Edge): Color? {
            val invocationType = edge.getUserData(invocationTypeKey) ?: return null
            return when(invocationType) {
                ParadoxEventHandler.InvocationType.All -> null
                ParadoxEventHandler.InvocationType.Immediate -> Color.RED
                ParadoxEventHandler.InvocationType.After -> Color.BLUE
            }
        }
    }
    
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
            val events = getDefinitions("event")
            if(events.isEmpty()) return
            //群星原版事件有5000+
            val nodeMap = mutableMapOf<ParadoxScriptDefinitionElement, Node>()
            val eventMap = mutableMapOf<String, ParadoxScriptDefinitionElement>()
            for(event in events) {
                ProgressManager.checkCanceled()
                if(!showNode(event)) continue
                val node = Node(event, provider)
                node.putUserData(nodeDataKey, event.getData())
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
                    val edge = Edge(source, target, relationship)
                    edge.putUserData(invocationTypeKey, invocationType)
                    edges.add(edge)
                }
            }
        }
        
        private fun showNode(definition: ParadoxScriptDefinitionElement): Boolean {
            provider as StellarisEventTreeDiagramProvider
            
            val definitionInfo = definition.definitionInfo ?: return false
            val settings = provider.getDiagramSettings(project).state
            
            //对于每组配置，只要其中任意一个配置匹配即可
            with(settings.typeSettings) {
                val v = definitionInfo.subtypes.takeIfNotEmpty() ?: return@with
                var enabled = false
                if(v.contains("hidden")) enabled = enabled || this.hidden
                if(v.contains("triggered")) enabled = enabled || this.triggered
                if(v.contains("major")) enabled = enabled || this.major
                if(v.contains("diplomatic")) enabled = enabled || this.diplomatic
                if(!enabled) return false
            }
            with(settings.eventType) {
                val v = definitionInfo.subtypes.takeIfNotEmpty() ?: return@with
                val enabled = v.any { this[it] ?: false }
                if(!enabled) return false
            }
            return true
        }
    }
}