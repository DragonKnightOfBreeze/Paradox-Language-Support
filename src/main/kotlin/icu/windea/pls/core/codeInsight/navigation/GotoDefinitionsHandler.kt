package icu.windea.pls.core.codeInsight.navigation

import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.application.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.pom.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.script.psi.*
import java.util.*

@Suppress("DialogTitleCapitalization")
class GotoDefinitionsHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxDefinitions"
    }
    
    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return null
        if(!element.isDefinitionRootKeyOrName()) return null
        val definition = element.findParentDefinition() ?: return null
        val definitionInfo = definition.definitionInfo ?: return null
        val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
        val runResult = ProgressManager.getInstance().runProcessWithProgressSynchronously({
            //need read action here
            runReadAction {
                val selector = definitionSelector(project, definition).contextSensitive()
                val resolved = ParadoxDefinitionSearch.search(definitionInfo.name, definitionInfo.type, selector).findAll()
                targets.addAll(resolved)
            }
        }, PlsBundle.message("script.goto.definitions.search", definitionInfo.name), true, project)
        if(!runResult) return null
        return GotoData(definition, targets.toTypedArray(), emptyList())
    }
    
    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptStringExpressionElement? {
        return ParadoxPsiFinder.findScriptExpression(file, offset).castOrNull()
    }
    
    override fun shouldSortTargets(): Boolean {
        return false
    }
    
    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val definitionInfo = sourceElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo
        if(definitionInfo == null) return ""
        val definitionName = definitionInfo.name.orAnonymous()
        return PlsBundle.message("script.goto.definitions.chooseTitle", definitionName.escapeXml())
    }
    
    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val definitionInfo = sourceElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo
        if(definitionInfo == null) return ""
        val definitionName = definitionInfo.name.orAnonymous()
        return PlsBundle.message("script.goto.definitions.findUsagesTitle", definitionName.escapeXml())
    }
    
    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return PlsBundle.message("script.goto.definitions.notFoundMessage")
    }
    
    override fun navigateToElement(descriptor: Navigatable) {
        if(descriptor is PsiElement) {
            activateFileWithPsiElement(descriptor, true)
        } else {
            descriptor.navigate(true)
        }
    }
}
