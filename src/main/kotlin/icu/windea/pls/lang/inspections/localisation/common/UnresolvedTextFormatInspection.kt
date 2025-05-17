package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.annotations.WithGameType
import icu.windea.pls.core.bindTextWhenChanged
import icu.windea.pls.core.matchesPattern
import icu.windea.pls.core.splitOptimized
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.util.ParadoxFilePathManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint
import javax.swing.JComponent

/**
 * 无法解析的文本格式的检查。
 *
 * @property ignoredNames （配置项）需要忽略的名字。使用GLOB模式。忽略大小写。
 */
@WithGameType(ParadoxGameType.Ck3, ParadoxGameType.Vic3)
class UnresolvedTextFormatInspection : LocalInspectionTool() {
    @JvmField
    var ignoredNames = "bold;semibold;extrabold;italic;underline;strikethrough;indent_newline;tooltip"
    //aka predefined format styles, or color expressions, or combined

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!shouldCheckFile(holder.file)) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if (element is ParadoxLocalisationTextFormat) visitIcon(element)
            }

            private fun visitIcon(element: ParadoxLocalisationTextFormat) {
                val name = element.name ?: return
                ignoredNames.splitOptimized(';').forEach {
                    if (name.matchesPattern(it, true)) return //忽略
                }
                val reference = element.reference
                if (reference == null || reference.resolve() != null) return
                val location = element.idElement ?: return
                holder.registerProblem(location, PlsBundle.message("inspection.localisation.unresolvedTextFormat.desc", name), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
            }
        }
    }

    private fun shouldCheckFile(file: PsiFile): Boolean {
        if (!ParadoxSyntaxConstraint.LocalisationTextFormat.supports(file)) return false
        val fileInfo = file.fileInfo ?: return false
        return ParadoxFilePathManager.inLocalisationPath(fileInfo.path)
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                label(PlsBundle.message("inspection.localisation.unresolvedTextFormat.option.ignoredNames"))
            }
            row {
                textField()
                    .bindText(::ignoredNames)
                    .bindTextWhenChanged(::ignoredNames)
                    .comment(PlsBundle.message("inspection.localisation.unresolvedTextFormat.option.ignoredNames.comment"))
                    .align(Align.FILL)
                    .resizableColumn()
            }
        }
    }
}
