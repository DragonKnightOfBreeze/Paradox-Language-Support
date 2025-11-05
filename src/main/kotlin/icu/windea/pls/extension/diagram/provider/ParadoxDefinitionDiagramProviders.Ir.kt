package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.DiagramPresentationModel
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import icu.windea.pls.extension.diagram.PlsDiagramBundle
import icu.windea.pls.extension.diagram.settings.IrEventTreeDiagramSettings
import icu.windea.pls.extension.diagram.settings.ParadoxDiagramSettings
import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

@WithGameType(ParadoxGameType.Ir)
class IrEventTreeDiagramProvider : ParadoxEventTreeDiagramProvider(ParadoxGameType.Ir) {
    object Constants {
        const val ID = "Ir.EventTree"
        val ITEM_PROPERTY_KEYS = listOf("picture")
    }

    override fun getID() = Constants.ID

    override fun getPresentableName() = PlsDiagramBundle.message("eventTree.name.ir")

    override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel) = DataModel(project, file, this)

    override fun getDiagramSettings(project: Project) = project.service<IrEventTreeDiagramSettings>()

    override fun getItemPropertyKeys() = Constants.ITEM_PROPERTY_KEYS

    class DataModel(
        project: Project,
        file: VirtualFile?, // umlFile
        override val provider: IrEventTreeDiagramProvider
    ) : ParadoxEventTreeDiagramProvider.DataModel(project, file, provider) {
        override fun showNode(definition: ParadoxScriptDefinitionElement, settings: ParadoxDiagramSettings.State): Boolean {
            if (settings !is IrEventTreeDiagramSettings.State) return true
            val definitionInfo = definition.definitionInfo ?: return false

            // 对于每组配置，只要其中任意一个配置匹配即可
            if (!showNodeBySettings(settings.type, definitionInfo.subtypes)) return false
            if (!showNodeBySettings(settings.attribute, definitionInfo.subtypes)) return false
            return true
        }
    }
}
