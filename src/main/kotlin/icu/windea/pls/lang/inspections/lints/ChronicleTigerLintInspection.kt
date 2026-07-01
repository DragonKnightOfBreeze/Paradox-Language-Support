package icu.windea.pls.lang.inspections.lints

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.codeInspection.ex.ExternalAnnotatorBatchInspection
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.util.CallbackLock
import icu.windea.pls.integrations.ChronicleIntegrationsBundle
import icu.windea.pls.integrations.lints.TigerLintResult
import icu.windea.pls.integrations.lints.providers.TigerLintToolProvider
import icu.windea.pls.integrations.settings.ChronicleIntegrationsSettingsManager
import icu.windea.pls.integrations.settings.TigerHighlightDialog
import javax.swing.JComponent

// com.intellij.codeInspection.javaDoc.JavadocHtmlLintInspection

/**
 * @see TigerLintResult
 * @see TigerLintToolProvider
 * @see ChronicleTigerLintAnnotator
 */
class ChronicleTigerLintInspection : LocalInspectionTool(), ExternalAnnotatorBatchInspection, DumbAware {
    private val callbackLock = CallbackLock()

    override fun getBatchSuppressActions(element: PsiElement?): Array<out SuppressQuickFix?> {
        return super.getBatchSuppressActions(element)
    }

    override fun createOptionsPanel(): JComponent {
        callbackLock.reset()
        return panel {
            row {
                label(ChronicleIntegrationsBundle.message("settings.integrations.lint.tigerHighlight"))
                contextHelp(ChronicleIntegrationsBundle.message("settings.integrations.lint.tigerHighlight.tip"))

                link(ChronicleBundle.message("link.configure")) {
                    // Tiger highlight mapping - open dialog - save settings and refresh files after dialog closed with ok
                    val dialog = TigerHighlightDialog()
                    if (dialog.showAndGet()) ChronicleIntegrationsSettingsManager.onTigerSettingsChanged(callbackLock)
                }
            }
        }
    }
}
