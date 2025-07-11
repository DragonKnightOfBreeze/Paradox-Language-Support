package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import javax.swing.*

/**
 * 无法解析的图标的检查。
 *
 * @property ignoredNames （配置项）需要忽略的名字。使用GLOB模式。忽略大小写。
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class UnresolvedIconInspection : LocalInspectionTool() {
    @JvmField
    var ignoredNames = ""
    @JvmField
    var ignoredInInjectedFiles = false

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (ignoredInInjectedFiles && PlsFileManager.isInjectedFile(holder.file.virtualFile)) return PsiElementVisitor.EMPTY_VISITOR

        if (!shouldCheckFile(holder.file)) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if (element is ParadoxLocalisationIcon) visitIcon(element)
            }

            private fun visitIcon(element: ParadoxLocalisationIcon) {
                val name = element.name ?: return
                ignoredNames.splitOptimized(';').forEach {
                    if (name.matchesPattern(it, true)) return //忽略
                }
                val reference = element.reference
                if (reference == null || reference.resolve() != null) return
                val location = element.idElement ?: return
                holder.registerProblem(location, PlsBundle.message("inspection.localisation.unresolvedIcon.desc", name), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
            }
        }
    }

    private fun shouldCheckFile(file: PsiFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        return ParadoxFileManager.inLocalisationPath(fileInfo.path)
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                label(PlsBundle.message("inspection.localisation.unresolvedIcon.option.ignoredNames"))
            }
            row {
                textField()
                    .bindText(::ignoredNames)
                    .bindTextWhenChanged(::ignoredNames)
                    .comment(PlsBundle.message("inspection.localisation.unresolvedIcon.option.ignoredNames.comment"))
                    .align(Align.FILL)
                    .resizableColumn()
            }
            //ignoredInInjectedFile
            row {
                checkBox(PlsBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles)
                    .actionListener { _, component -> ignoredInInjectedFiles = component.isSelected }
            }
        }
    }
}

