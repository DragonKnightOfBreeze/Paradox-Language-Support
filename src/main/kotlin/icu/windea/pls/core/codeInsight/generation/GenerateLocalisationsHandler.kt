package icu.windea.pls.core.codeInsight.generation

import com.intellij.codeInsight.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

@Suppress("UNUSED_PARAMETER")
class GenerateLocalisationsHandler : CodeInsightActionHandler {
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val contextKey = PlsKeys.generateLocalisationsContextKey
        val context = file.getUserData(contextKey)
            ?: getDefaultContext(project, editor, file)
            ?: return
        file.putUserData(contextKey, null)
        ParadoxPsiGenerator.generateLocalisations(context, project, file)
    }
    
    private fun getDefaultContext(project: Project, editor: Editor, file: PsiFile): GenerateLocalisationsContext? {
        //直接基于所有的相关本地化，无论是否确实
        val offset = editor.caretModel.offset
        val definition = findElement(file, offset)?.findParentDefinition() ?: return null
        val definitionInfo = definition.definitionInfo ?: return null
        return getDefaultContext(definitionInfo)
    }
    
    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptStringExpressionElement? {
        return ParadoxPsiFinder.findScriptExpression(file, offset).castOrNull()
    }
    
    companion object {
        @JvmStatic
        fun getDefaultContext(definitionInfo: ParadoxDefinitionInfo): GenerateLocalisationsContext? {
            if(definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
            
            val definitionName = definitionInfo.name
            val localisationInfos = definitionInfo.localisations
            if(localisationInfos.isEmpty()) return null
            val localisationNames = localisationInfos.mapNotNullTo(mutableSetOf()) { it.locationExpression.resolvePlaceholder(definitionName) }
            return GenerateLocalisationsContext(definitionName, localisationNames)
        }
    }
}

