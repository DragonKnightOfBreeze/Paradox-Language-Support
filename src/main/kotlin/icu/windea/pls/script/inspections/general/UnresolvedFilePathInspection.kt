package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.observable.util.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 无法解析的文件路径的检查。
 *
 * @property ignoredFilePaths （配置项）需要忽略的文件路径的模式。使用ANT模式。忽略大小写。
 */
class UnresolvedFilePathInspection : LocalInspectionTool() {
    @JvmField var ignoredFilePaths = "*.lua"
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return Visitor(this, holder)
    }
    
    private class Visitor(
        private val inspection: UnresolvedFilePathInspection,
        private val holder: ProblemsHolder
    ) : ParadoxScriptVisitor() {
        override fun visitString(valueElement: ParadoxScriptString) {
            ProgressManager.checkCanceled()
            //match or single
            val valueConfig = ParadoxCwtConfigHandler.resolveValueConfigs(valueElement).firstOrNull() ?: return
            val expression = valueConfig.valueExpression
            val project = valueElement.project
            val location = valueElement
            when(expression.type) {
                CwtDataType.AbsoluteFilePath -> {
                    val filePath = valueElement.value
                    val path = filePath.toPathOrNull() ?: return
                    if(VfsUtil.findFile(path, false) != null) return
                    val message = PlsBundle.message("inspection.script.general.unresolvedFilePath.description.1", path)
                    holder.registerProblem(location, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
                        ImportGameOrModDirectoryFix(valueElement)
                    )
                }
                CwtDataType.FileName -> {
                    val filePath = CwtPathExpressionType.FileName.resolve(expression.value, valueElement.value.normalizePath()) ?: return
                    if(filePath.matchesAntPath(inspection.ignoredFilePaths, true)) return
                    val selector = fileSelector().gameTypeFrom(valueElement)
                    if(ParadoxFilePathSearch.search(filePath, project, selector = selector).findFirst() != null) return
                    val message = PlsBundle.message("inspection.script.general.unresolvedFilePath.description.2", filePath)
                    holder.registerProblem(location, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
                        ImportGameOrModDirectoryFix(valueElement)
                    )
                }
                CwtDataType.FilePath -> {
                    val filePath = CwtPathExpressionType.FilePath.resolve(expression.value, valueElement.value.normalizePath()) ?: return
                    if(filePath.matchesAntPath(inspection.ignoredFilePaths, true)) return
                    val selector = fileSelector().gameTypeFrom(valueElement)
                    if(ParadoxFilePathSearch.search(filePath, project, selector = selector).findFirst() != null) return
                    val filePathExpression = expression.value?.replace(",", "$")
                    val message = if(filePathExpression == null) PlsBundle.message("inspection.script.general.unresolvedFilePath.description.3", filePath)
                    else PlsBundle.message("inspection.script.general.unresolvedFilePath.description.3_1", filePath, filePathExpression)
                    holder.registerProblem(location, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
                        ImportGameOrModDirectoryFix(valueElement)
                    )
                }
                CwtDataType.Icon -> {
                    val filePath = CwtPathExpressionType.Icon.resolve(expression.value, valueElement.value.normalizePath()) ?: return
                    if(filePath.matchesAntPath(inspection.ignoredFilePaths, true)) return
                    val selector = fileSelector().gameTypeFrom(valueElement)
                    if(ParadoxFilePathSearch.search(filePath, project, selector = selector).findFirst() != null) return
                    val filePathExpression = expression.value?.let { "${it}/$.dds" }
                    val message = if(filePathExpression == null) PlsBundle.message("inspection.script.general.unresolvedFilePath.description.4", filePath)
                    else PlsBundle.message("inspection.script.general.unresolvedFilePath.description.4_1", filePath, filePathExpression)
                    holder.registerProblem(location, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
                        ImportGameOrModDirectoryFix(valueElement)
                    )
                }
                else -> pass()
            }
        }
    }
    
    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                label(PlsBundle.message("inspection.script.general.unresolvedFilePath.option.ignoredFilePaths"))
            }
            row {
                textField().bindText(::ignoredFilePaths)
                    .applyToComponent {
                        whenTextChanged {
                            val document = it.document
                            val text = document.getText(0, document.length)
                            if(text != ignoredFilePaths) ignoredFilePaths = text
                        }
                    }
                    .comment(PlsBundle.message("inspection.script.general.unresolvedFilePath.option.ignoredFilePaths.comment"))
                    .align(Align.FILL)
                    .resizableColumn()
            }
        }
    }
}
