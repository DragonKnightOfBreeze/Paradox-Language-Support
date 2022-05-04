package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.settings.*
import com.intellij.openapi.actionSystem.*
import icu.windea.pls.*

class ShowInlayHintsSettings : AnAction(PlsBundle.message("core.action.showInlayHintsSettings.name")) {
	override fun actionPerformed(e: AnActionEvent) {
		val file = CommonDataKeys.PSI_FILE.getData(e.dataContext) ?: return
		val fileLanguage = file.language
		InlayHintsConfigurable.showSettingsDialogForLanguage(file.project, fileLanguage)
	}
}