package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.generation.actions.PresentableCodeInsightActionHandler
import com.intellij.ide.util.EditSourceUtil
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.resolve.ParadoxDefinitionInheritSupport
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.util.ParadoxPsiManager
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 从光标位置对应的定义跳转到其父定义。
 */
class ParadoxGotoSuperDefinitionActionHandler : PresentableCodeInsightActionHandler {
    private fun findSuperDefinition(editor: Editor, file: PsiFile): ParadoxScriptDefinitionElement? {
        val offset = editor.caretModel.offset
        val allOptions = ParadoxPsiManager.FindDefinitionOptions
        val options = allOptions.BY_ROOT_KEY or allOptions.BY_NAME or allOptions.BY_REFERENCE
        val definition = ParadoxPsiManager.findDefinition(file, offset, options) ?: return null
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

    @Suppress("DEPRECATION")
    override fun update(editor: Editor, file: PsiFile, presentation: Presentation?, actionPlace: String?) {
        if (presentation == null) return
        val useShortName = actionPlace != null && (ActionPlaces.MAIN_MENU == actionPlace || ActionPlaces.isPopupPlace(actionPlace))
        if (useShortName) {
            presentation.text = PlsBundle.message("action.GotoSuperDefinition.MainMenu.text")
        } else {
            presentation.text = PlsBundle.message("action.GotoSuperDefinition.text")
        }
        presentation.description = PlsBundle.message("action.GotoSuperDefinition.description")
    }

    override fun startInWriteAction() = false
}
