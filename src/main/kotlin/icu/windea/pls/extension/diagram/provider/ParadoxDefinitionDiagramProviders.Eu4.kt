package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.*
import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Eu4)
class Eu4EventTreeDiagramProvider : ParadoxEventTreeDiagramProvider(ParadoxGameType.Eu4) {
    object Constants {
        const val ID = "Eu4.EventTree"
        val ITEM_PROPERTY_KEYS = arrayOf("picture")
    }

    override fun getID() = Constants.ID

    override fun getPresentableName() = PlsDiagramBundle.message("eventTree.name.eu4")

    override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel) = DataModel(project, file, this)

    override fun getItemPropertyKeys() = Constants.ITEM_PROPERTY_KEYS

    override fun getDiagramSettings(project: Project) = project.service<Eu4EventTreeDiagramSettings>()

    class DataModel(
        project: Project,
        file: VirtualFile?, //umlFile
        provider: ParadoxDefinitionDiagramProvider
    ) : ParadoxEventTreeDiagramProvider.DataModel(project, file, provider) {
        override fun showNode(definition: ParadoxScriptDefinitionElement, settings: ParadoxDiagramSettings.State): Boolean {
            if (provider !is Eu4EventTreeDiagramProvider) return true
            if (settings !is Eu4EventTreeDiagramSettings.State) return true
            val definitionInfo = definition.definitionInfo ?: return false

            //对于每组配置，只要其中任意一个配置匹配即可
            with(settings.attributeSettings) {
                val v = definitionInfo.subtypes.orNull() ?: return@with
                var enabled = false
                if (v.contains("hidden")) enabled = enabled || this.hidden
                if (v.contains("triggered")) enabled = enabled || this.triggered
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
