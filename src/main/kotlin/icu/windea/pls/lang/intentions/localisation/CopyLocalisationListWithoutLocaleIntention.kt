package icu.windea.pls.lang.intentions.localisation

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.values.FallbackStrings
import icu.windea.pls.ide.notification.PlsNotificationGroups
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.lang.util.ParadoxLocalisationListManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList

/**
 * 复制当前语言环境下的本地化列表到剪贴板（不保留语言环境前缀，保留其中的注释和空行）。
 */
class CopyLocalisationListWithoutLocaleIntention : ManipulateLocalisationListIntentionBase(), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.copyLocalisationListWithoutLocale")

    override fun doInvoke(project: Project, editor: Editor, file: PsiFile, element: ParadoxLocalisationPropertyList) {
        ParadoxLocalisationListManager.copyWithoutLocale(element)
        createNotification(element)
    }

    private fun createNotification(element: ParadoxLocalisationPropertyList): Notification {
        val localeElement = element.locale
        val locale = selectLocale(localeElement)?.text ?: localeElement?.name ?: FallbackStrings.unknown
        val content = PlsBundle.message("intention.copyLocalisationList.notification", locale)
        return PlsNotificationGroups.manipulation().createNotification(content, NotificationType.INFORMATION)
    }

    override fun generatePreview(project: Project, editor: Editor, psiFile: PsiFile) = IntentionPreviewInfo.EMPTY
}
