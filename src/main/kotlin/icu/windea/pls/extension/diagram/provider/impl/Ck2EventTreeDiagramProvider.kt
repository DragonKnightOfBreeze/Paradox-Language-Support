package icu.windea.pls.extension.diagram.provider.impl

import com.intellij.diagram.*
import com.intellij.openapi.components.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.provider.*
import icu.windea.pls.extension.diagram.settings.impl.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Ck2)
class Ck2EventTreeDiagramProvider : ParadoxEventTreeDiagramProvider(ParadoxGameType.Ck2) {
    companion object {
        const val ID = "Ck2.EventTree"
        
        val ITEM_PROPERTY_KEYS = arrayOf("picture")
    }
    
    override fun getID() = ID
    
    @Suppress("DialogTitleCapitalization")
    override fun getPresentableName() = PlsDiagramBundle.message("ck2.eventTree.name")
    
    override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel) = DataModel(project, file, this)
    
    override fun getItemPropertyKeys() = ITEM_PROPERTY_KEYS
    
    override fun getDiagramSettings(project: Project) = project.service<Ck2EventTreeDiagramSettings>()
    
    class DataModel(
        project: Project,
        file: VirtualFile?, //umlFile
        provider: ParadoxDefinitionDiagramProvider
    ) : ParadoxEventTreeDiagramProvider.DataModel(project, file, provider) {
        override fun updateDataModel(indicator: ProgressIndicator?) {
            provider as Ck2EventTreeDiagramProvider
            val events = getDefinitions("event")
            if(events.isEmpty()) return
            //群星原版事件有5000+
            val nodeMap = mutableMapOf<ParadoxScriptDefinitionElement, Node>()
            val eventMap = mutableMapOf<String, ParadoxScriptDefinitionElement>()
            for(event in events) {
                ProgressManager.checkCanceled()
                if(!showNode(event)) continue
                val node = Node(event, provider)
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
                for(invocation in invocations) {
                    ProgressManager.checkCanceled()
                    val source = nodeMap.get(event) ?: continue
                    val target = eventMap.get(invocation)?.let { nodeMap.get(it) } ?: continue
                    val edge = Edge(source, target, REL_INVOKE)
                    edges.add(edge)
                }
            }
        }
        
        private fun showNode(definition: ParadoxScriptDefinitionElement): Boolean {
            provider as Ck2EventTreeDiagramProvider
            
            val definitionInfo = definition.definitionInfo ?: return false
            val settings = provider.getDiagramSettings(project).state
            
            //对于每组配置，只要其中任意一个配置匹配即可
            with(settings.typeSettings) {
                val v = definitionInfo.subtypes.takeIfNotEmpty() ?: return@with
                var enabled = false
                if(v.contains("hidden")) enabled = enabled || this.hidden
                if(v.contains("triggered")) enabled = enabled || this.triggered
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