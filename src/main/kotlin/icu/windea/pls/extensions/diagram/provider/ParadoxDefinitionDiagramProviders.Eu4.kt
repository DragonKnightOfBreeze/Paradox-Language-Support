package icu.windea.pls.extensions.diagram.provider

import com.intellij.diagram.DiagramPresentationModel
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import icu.windea.pls.extensions.diagram.PlsDiagramBundle
import icu.windea.pls.extensions.diagram.settings.Eu4EventTreeDiagramSettings
import icu.windea.pls.extensions.diagram.settings.ParadoxDiagramSettings
import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxDefinitionElement

@WithGameType(ParadoxGameType.Eu4)
class Eu4EventTreeDiagramProvider : ParadoxEventTreeDiagramProvider(ParadoxGameType.Eu4) {
    object Constants {
        const val ID = "Eu4.EventTree"
        val ITEM_PROPERTY_KEYS = listOf("picture")
    }

    override fun getID() = Constants.ID

    override fun getPresentableName() = PlsDiagramBundle.message("eventTree.name.eu4")

    override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel) = DataModel(project, file, this)

    override fun getDiagramSettings(project: Project) = project.service<Eu4EventTreeDiagramSettings>()

    override fun getItemPropertyKeys() = Constants.ITEM_PROPERTY_KEYS

    class DataModel(
        project: Project,
        file: VirtualFile?, // umlFile
        override val provider: Eu4EventTreeDiagramProvider
    ) : ParadoxEventTreeDiagramProvider.DataModel(project, file, provider) {
        override fun showNode(definition: ParadoxDefinitionElement, settings: ParadoxDiagramSettings.State): Boolean {
            if (settings !is Eu4EventTreeDiagramSettings.State) return true
            val definitionInfo = definition.definitionInfo ?: return false

            // 对于每组配置，只要其中任意一个配置匹配即可
            if (!showNodeBySettings(settings.type, definitionInfo.subtypes)) return false
            if (!showNodeBySettings(settings.attribute, definitionInfo.subtypes)) return false
            return true
        }
    }
}
