package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.DiagramPresentationModel
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import icu.windea.pls.extension.diagram.PlsDiagramBundle
import icu.windea.pls.extension.diagram.settings.ParadoxDiagramSettings
import icu.windea.pls.extension.diagram.settings.Vic3EventTreeDiagramSettings
import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

@WithGameType(ParadoxGameType.Vic3)
class Vic3EventTreeDiagramProvider : ParadoxEventTreeDiagramProvider(ParadoxGameType.Vic3) {
    object Constants {
        const val ID = "Vic3.EventTree"
        val ITEM_PROPERTY_KEYS = listOf("picture", "icon", "left_icon", "right_icon", "minor_left_icon", "minor_right_icon", "gui_window")
    }

    override fun getID() = Constants.ID

    override fun getPresentableName() = PlsDiagramBundle.message("eventTree.name.vic3")

    override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel) = DataModel(project, file, this)

    override fun getDiagramSettings(project: Project) = project.service<Vic3EventTreeDiagramSettings>()

    override fun getItemPropertyKeys() = Constants.ITEM_PROPERTY_KEYS

    class DataModel(
        project: Project,
        file: VirtualFile?, // umlFile
        override val provider: Vic3EventTreeDiagramProvider
    ) : ParadoxEventTreeDiagramProvider.DataModel(project, file, provider) {
        override fun showNode(definition: ParadoxScriptDefinitionElement, settings: ParadoxDiagramSettings.State): Boolean {
            if (settings !is Vic3EventTreeDiagramSettings.State) return true
            val definitionInfo = definition.definitionInfo ?: return false

            // 对于每组配置，只要其中任意一个配置匹配即可
            if (!showNodeBySettings(settings.type, definitionInfo.subtypes)) return false
            if (!showNodeBySettings(settings.attribute, definitionInfo.subtypes)) return false
            return true
        }
    }
}
