package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.base.annotations.WithGameType
import icu.windea.pls.core.matchesPatterns
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat
import icu.windea.pls.localisation.psi.ParadoxLocalisationVisitor
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint
import javax.swing.JComponent

/**
 * 无法解析的文本格式的代码检查。
 *
 * @property ignoredNames （配置项）需要忽略的名字。一组模式，分号分隔，忽略大小写。
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
@WithGameType(ParadoxGameType.Ck3, ParadoxGameType.Vic3, ParadoxGameType.Eu5)
class UnresolvedTextFormatInspection : LocalInspectionTool() {
    // aka predefined format styles, or color expressions, or combined
    @JvmField var ignoredNames = "bold;semibold;extrabold;italic;underline;strikethrough;indent_newline;tooltip:"
    @JvmField var ignoredInInjectedFiles = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求游戏类型支持文本格式
        if (!ParadoxSyntaxConstraint.LocalisationTextFormat.testTarget(file)) return false
        // 要求规则分组数据已加载完毕
        if (!ChronicleFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 要求是可接受的本地化文件
        return ParadoxPsiFileMatcher.isLocalisationFile(file, injectable = !ignoredInInjectedFiles)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxLocalisationVisitor() {
            override fun visitTextFormat(element: ParadoxLocalisationTextFormat) {
                ProgressManager.checkCanceled()
                val name = element.name ?: return
                if (name.matchesPatterns(ignoredNames, ignoreCase = true)) return // 忽略
                val reference = element.reference
                if (reference == null || reference.resolve() != null) return
                val location = element.idElement ?: return
                val description = ChronicleBundle.message("inspection.localisation.unresolvedTextFormat.desc", name)
                holder.registerProblem(location, description, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
            }
        }
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            // ignoredNames
            row {
                label(ChronicleBundle.message("inspection.localisation.unresolvedTextFormat.option.ignoredNames"))
                textField()
                    .bindText(::ignoredNames.toAtomicProperty())
                    .comment(ChronicleBundle.message("comment.patterns"))
                    .align(Align.FILL)
                    .resizableColumn()
            }
            // ignoredInInjectedFile
            row {
                checkBox(ChronicleBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles.toAtomicProperty())
            }
        }
    }
}
