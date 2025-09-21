package icu.windea.pls.lang.inspections.lints

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.codeInspection.ex.ExternalAnnotatorBatchInspection
import com.intellij.psi.PsiElement
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.panel
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.integrations.settings.PlsIntegrationsSettingsManager
import icu.windea.pls.integrations.settings.PlsTigerHighlightDialog
import javax.swing.JComponent

//com.intellij.codeInspection.javaDoc.JavadocHtmlLintInspection

class PlsTigerLintInspection: LocalInspectionTool(), ExternalAnnotatorBatchInspection {
    companion object {
        const val SHORT_NAME = "PlsTigerLint"
    }

    override fun getBatchSuppressActions(element: PsiElement?): Array<out SuppressQuickFix?> {
        return super.getBatchSuppressActions(element)
    }

    override fun createOptionsPanel(): JComponent {
        val settings = PlsFacade.getIntegrationsSettings().lint
        return panel {
            row { comment(PlsBundle.message("inspection.lints.tigerHighlight.options.comment")) }
            row {
                link(PlsBundle.message("settings.integrations.lint.tigerHighlight.openDialog")) {
                    val dialog = PlsTigerHighlightDialog(settings.tigerHighlight)
                    if (dialog.showAndGet()) {
                        // 刷新检查结果
                        PlsIntegrationsSettingsManager.onTigerSettingsChanged(icu.windea.pls.core.util.CallbackLock())
                    }
                }
            }
        }
    }
}
