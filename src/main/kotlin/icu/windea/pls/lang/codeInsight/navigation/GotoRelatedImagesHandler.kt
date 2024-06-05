package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.application.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.pom.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*
import java.util.*

//com.intellij.testIntegration.GotoTestOrCodeHandler

@Suppress("DialogTitleCapitalization")
class GotoRelatedImagesHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxRelatedImages"
    }
    
    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return null
        if(element.isDefinitionRootKeyOrName()) {
            val definition = element.findParentDefinition() ?: return null
            val definitionInfo = definition.definitionInfo ?: return null
            val imageInfos = definitionInfo.images
            if(imageInfos.isEmpty()) return GotoData(definition, PsiElement.EMPTY_ARRAY, emptyList())
            val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
            val runResult = ProgressManager.getInstance().runProcessWithProgressSynchronously({
                for((_, locationExpression) in imageInfos) {
                    ProgressManager.checkCanceled()
                    //need read action here
                    runReadAction {
                        val resolved = locationExpression.resolveAll(definition, definitionInfo)
                        if(resolved != null && resolved.elements.isNotEmpty()) {
                            targets.addAll(resolved.elements)
                        }
                    }
                }
            }, PlsBundle.message("script.goto.relatedImages.search.1", definitionInfo.name), true, project)
            if(!runResult) return null
            return GotoData(definition, targets.distinct().toTypedArray(), emptyList())
        }
        val modifierElement = ParadoxModifierHandler.resolveModifier(element)
        if(modifierElement != null) {
            val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
            val runResult = ProgressManager.getInstance().runProcessWithProgressSynchronously({
                runReadAction {
                    val paths = ParadoxModifierHandler.getModifierIconPaths(modifierElement.name, modifierElement)
                    val iconFiles = paths.firstNotNullOfOrNull { path ->
                        val iconSelector = fileSelector(project, element).contextSensitive()
                        ParadoxFilePathSearch.searchIcon(path, iconSelector).findAll().orNull()
                    }
                    if(iconFiles != null) targets.addAll(targets)
                }
            }, PlsBundle.message("script.goto.relatedImages.search.2", modifierElement.name), true, project)
            if(!runResult) return null
            return GotoData(element, targets.distinct().toTypedArray(), emptyList())
        }
        return null
    }
    
    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptStringExpressionElement? {
        return ParadoxPsiManager.findScriptExpression(file, offset).castOrNull()
    }
    
    override fun shouldSortTargets(): Boolean {
        return false
    }
    
    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val definitionInfo = sourceElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo
        if(definitionInfo != null) {
            val definitionName = definitionInfo.name.orAnonymous()
            return PlsBundle.message("script.goto.relatedImages.chooseTitle.1", definitionName.escapeXml())
        }
        val modifierElement = sourceElement.castOrNull<ParadoxScriptStringExpressionElement>()?.let { ParadoxModifierHandler.resolveModifier(it) }
        if(modifierElement != null) {
            val modifierName = modifierElement.name
            return PlsBundle.message("script.goto.relatedImages.chooseTitle.2", modifierName.escapeXml())
        }
        val sourceName = sourceElement.text.unquote()
        return PlsBundle.message("script.goto.relatedImages.chooseTitle.0", sourceName.escapeXml())
    }
    
    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val definitionInfo = sourceElement.castOrNull<ParadoxScriptDefinitionElement>()?.definitionInfo
        if(definitionInfo != null) {
            val definitionName = definitionInfo.name.orAnonymous()
            return PlsBundle.message("script.goto.relatedImages.findUsagesTitle.1", definitionName.escapeXml())
        }
        val modifierElement = sourceElement.castOrNull<ParadoxScriptStringExpressionElement>()?.let { ParadoxModifierHandler.resolveModifier(it) }
        if(modifierElement != null) {
            val modifierName = modifierElement.name
            return PlsBundle.message("script.goto.relatedImages.findUsagesTitle.2", modifierName.escapeXml())
        }
        val sourceName = sourceElement.text.unquote()
        return PlsBundle.message("script.goto.relatedImages.findUsagesTitle.0", sourceName.escapeXml())
    }
    
    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return PlsBundle.message("script.goto.relatedImages.notFoundMessage")
    }
    
    override fun navigateToElement(descriptor: Navigatable) {
        if(descriptor is PsiElement) {
            activateFileWithPsiElement(descriptor, true)
        } else {
            descriptor.navigate(true)
        }
    }
}
