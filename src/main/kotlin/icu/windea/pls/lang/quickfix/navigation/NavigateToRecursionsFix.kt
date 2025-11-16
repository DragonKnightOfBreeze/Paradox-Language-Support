package icu.windea.pls.lang.quickfix.navigation

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.model.ParadoxRootInfo

class NavigateToRecursionsFix(
    private val key: String,
    target: PsiElement,
    elements: Collection<PsiElement>
) : NavigateToFix(target, elements) {
    override fun getText() = PlsBundle.message("fix.navigateTo.recursions.name")

    override fun getPopupTitle(editor: Editor) = PlsBundle.message("fix.navigateTo.recursions.popup.title", key)

    override fun getPopupText(editor: Editor, value: PsiElement): String {
        val file = value.containingFile ?: return PlsBundle.message("fix.navigate.popup.text.0", key)
        val fileInfo = file.fileInfo ?: return PlsBundle.message("fix.navigate.popup.text.0", key)
        val rootInfo = fileInfo.rootInfo
        if ((rootInfo !is ParadoxRootInfo.MetadataBased)) return PlsBundle.message("fix.navigate.popup.text.0", key)
        return PlsBundle.message("fix.navigate.popup.text.2", key, fileInfo.path, rootInfo.qualifiedName)
    }
}
