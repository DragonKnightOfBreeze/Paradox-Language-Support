package icu.windea.pls.lang.fixes.navigation

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.model.ParadoxRootInfo

class NavigateToOverridingFilesFix(
    private val key: String,
    target: PsiElement,
    elements: Collection<PsiElement>
) : NavigateToFix(target, elements) {
    override fun getFamilyName() = ChronicleInspectionBundle.message("fix.navigateTo.overridingFiles.name")

    override fun getPopupTitle(editor: Editor) = ChronicleInspectionBundle.message("fix.navigateTo.overridingFiles.popup.title", key)

    override fun getPopupText(editor: Editor, value: PsiElement): String {
        val file = runSmartReadAction { value.containingFile } ?: return ChronicleInspectionBundle.message("fix.navigate.popup.text.0", key)
        val fileInfo = file.fileInfo ?: return ChronicleInspectionBundle.message("fix.navigate.popup.text.0", key)
        val rootInfo = fileInfo.rootInfo
        if ((rootInfo !is ParadoxRootInfo.MetadataBased)) return ChronicleInspectionBundle.message("fix.navigate.popup.text.0", key)
        return ChronicleInspectionBundle.message("fix.navigate.popup.text.1", key, rootInfo.qualifiedName)
    }
}
