package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.generation.actions.*
import com.intellij.ide.util.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.inherit.*
import icu.windea.pls.script.psi.*

/**
 * 从光标位置对应的定义跳转到其父定义。
 */
class ParadoxScriptGotoSuperActionHandler: PresentableCodeInsightActionHandler {
    private fun findSuperDefinition(editor: Editor, file: PsiFile): ParadoxScriptDefinitionElement? {
        val offset = editor.caretModel.offset
        val allOptions = ParadoxPsiFinder.FindDefinitionOptions
        val options = allOptions.BY_ROOT_KEY or allOptions.BY_NAME or allOptions.BY_REFERENCE
        val definition = ParadoxPsiFinder.findDefinition(file, offset, options) ?: return null
        val definitionInfo = definition.definitionInfo ?: return null
        val superDefinition = ParadoxDefinitionInheritSupport.getSuperDefinition(definition, definitionInfo) ?: return null
        return superDefinition
    }
    
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val superDefinition = findSuperDefinition(editor, file) ?: return
        val navigatable = EditSourceUtil.getDescriptor(superDefinition) ?: return
        navigatable.navigate(true)
    }
    
    override fun update(editor: Editor, file: PsiFile, presentation: Presentation?) {
        update(editor, file, presentation, null)
    }
    
    override fun update(editor: Editor, file: PsiFile, presentation: Presentation?, actionPlace: String?) {
        if(presentation == null) return
        val useShortName = actionPlace != null && (ActionPlaces.MAIN_MENU == actionPlace || ActionPlaces.isPopupPlace(actionPlace))
        if(useShortName) {
            presentation.text = PlsBundle.message("action.GotoSuperDefinition.MainMenu.text")
        } else {
            presentation.text = PlsBundle.message("action.GotoSuperDefinition.text")
        }
        presentation.description = PlsBundle.message("action.GotoSuperDefinition.description")
    }
    
    override fun startInWriteAction() = false
}