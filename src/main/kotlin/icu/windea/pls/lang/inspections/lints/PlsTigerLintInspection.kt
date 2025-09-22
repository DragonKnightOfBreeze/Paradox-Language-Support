package icu.windea.pls.lang.inspections.lints

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.codeInspection.ex.ExternalAnnotatorBatchInspection
import com.intellij.psi.PsiElement
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.CallbackLock
import icu.windea.pls.integrations.settings.PlsIntegrationsSettingsManager
import icu.windea.pls.integrations.settings.PlsTigerHighlightDialog
import javax.swing.JComponent

//com.intellij.codeInspection.javaDoc.JavadocHtmlLintInspection

class PlsTigerLintInspection : LocalInspectionTool(), ExternalAnnotatorBatchInspection {
    companion object {
        const val SHORT_NAME = "PlsTigerLint"
    }

    private val callbackLock = CallbackLock()

    override fun getBatchSuppressActions(element: PsiElement?): Array<out SuppressQuickFix?> {
        return super.getBatchSuppressActions(element)
    }

    override fun createOptionsPanel(): JComponent {
        callbackLock.reset()
        return panel {
            row {
                label(PlsBundle.message("settings.integrations.lint.tigerHighlight"))
                contextHelp(PlsBundle.message("settings.integrations.lint.tigerHighlight.tip"))

                link(PlsBundle.message("configure")) {
                    // Tiger highlight mapping - open dialog - save settings and refresh files after dialog closed with ok
                    val dialog = PlsTigerHighlightDialog()
                    if (dialog.showAndGet()) PlsIntegrationsSettingsManager.onTigerSettingsChanged(callbackLock)
                }
            }
        }
    }
}
