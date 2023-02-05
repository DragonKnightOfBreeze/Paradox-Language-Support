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
import icu.windea.pls.lang.expression.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 无法解析的文件路径的检查。
 *
 * @property ignoredFileNames （配置项）需要忽略的文件路径的模式。使用ANT模式。忽略大小写。
 */
class UnresolvedFilePathInspection : LocalInspectionTool() {
    @JvmField var ignoredFileNames = "*.lua"
    
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
            val configExpression = valueConfig.valueExpression
            val project = valueElement.project
            val location = valueElement
            if(configExpression.type == CwtDataType.AbsoluteFilePath) {
                val filePath = valueElement.value
                val path = filePath.toPathOrNull() ?: return
                if(VfsUtil.findFile(path, false) != null) return
                val message = PlsBundle.message("inspection.script.general.unresolvedFilePath.description.abs", path)
                holder.registerProblem(location, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
                    ImportGameOrModDirectoryFix(valueElement)
                )
                return
            }
            val fileReferenceExpression = ParadoxPathReferenceExpression.get(configExpression)
            if(fileReferenceExpression != null) {
                val pathReference = valueElement.value.normalizePath()
                val fileName = fileReferenceExpression.resolveFileName(configExpression, pathReference)
                if(fileName.matchesGlobFileName(inspection.ignoredFileNames, true)) return
                val selector = fileSelector().gameTypeFrom(valueElement)
                if(ParadoxFilePathSearch.search(pathReference, project, configExpression, selector = selector).findFirst() != null) return
                val message = fileReferenceExpression.getUnresolvedMessage(configExpression, pathReference)
                holder.registerProblem(location, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
                    ImportGameOrModDirectoryFix(valueElement)
                )
            }
        }
    }
    
    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                label(PlsBundle.message("inspection.script.general.unresolvedFilePath.option.ignoredFileNames"))
            }
            row {
                textField().bindText(::ignoredFileNames)
                    .applyToComponent {
                        whenTextChanged {
                            val document = it.document
                            val text = document.getText(0, document.length)
                            if(text != ignoredFileNames) ignoredFileNames = text
                        }
                    }
                    .comment(PlsBundle.message("inspection.script.general.unresolvedFilePath.option.ignoredFileNames.comment"))
                    .align(Align.FILL)
                    .resizableColumn()
            }
        }
    }
}
