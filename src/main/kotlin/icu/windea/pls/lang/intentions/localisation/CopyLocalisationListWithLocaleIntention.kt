package icu.windea.pls.lang.intentions.localisation

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.util.ParadoxLocalisationListManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList

/**
 * 复制当前语言环境下的本地化列表到剪贴板（保留语言环境前缀，保留其中的注释和空行）。
 */
class CopyLocalisationListWithLocaleIntention : ManipulateLocalisationListIntentionBase(), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.copyLocalisationListWithLocale")

    override fun doInvoke(project: Project, editor: Editor, file: PsiFile, element: ParadoxLocalisationPropertyList) {
        ParadoxLocalisationListManager.copyWithLocale(element)
    }

    override fun generatePreview(project: Project, editor: Editor, psiFile: PsiFile) = IntentionPreviewInfo.EMPTY
}
