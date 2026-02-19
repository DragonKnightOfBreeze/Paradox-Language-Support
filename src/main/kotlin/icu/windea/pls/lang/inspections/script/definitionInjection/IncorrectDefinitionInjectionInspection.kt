package icu.windea.pls.lang.inspections.script.definitionInjection

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.lang.inspections.PlsInspectionUtil
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptVisitor
import javax.swing.JComponent

/**
 * 检查定义注入的用法是否正确。
 *
 * - 基于定义注入的模式，定义目标的存在性必须正确。
 */
class IncorrectDefinitionInjectionInspection : DefinitionInjectionInspectionBase() {
    private var checkForRelaxModes = false

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxScriptVisitor() {
            override fun visitProperty(element: ParadoxScriptProperty) {
                ProgressManager.checkCanceled()
                val definitionInjectionInfo = ParadoxDefinitionInjectionManager.getInfo(element) ?: return
                if (definitionInjectionInfo.target.isNullOrEmpty()) {
                    val description = PlsBundle.message("inspection.script.incorrectDefinitionInjection.desc.1")
                    holder.registerProblem(element.propertyKey, description)
                    return
                }
                if (definitionInjectionInfo.type.isNullOrEmpty()) return // considered "unsupported"
                if (definitionInjectionInfo.typeConfig == null) return // considered "unsupported"
                val target = definitionInjectionInfo.target
                val type = definitionInjectionInfo.type
                val relax = definitionInjectionInfo.isRelaxMode()
                val targetExist = definitionInjectionInfo.isTargetExist(holder.file)
                if (!targetExist) {
                    if (relax) {
                        if (checkForRelaxModes) {
                            val description = PlsBundle.message("inspection.script.incorrectDefinitionInjection.desc.3", target, type)
                            val highlightType = PlsInspectionUtil.getWeakerHighlightType() // use weaker highlight type
                            holder.registerProblem(element.propertyKey, description, highlightType)
                        }
                    } else {
                        val description = PlsBundle.message("inspection.script.incorrectDefinitionInjection.desc.2", target, type)
                        holder.registerProblem(element.propertyKey, description)
                    }
                }
            }
        }
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            // checkForRelaxModes
            row {
                checkBox(PlsBundle.message("inspection.script.incorrectDefinitionInjection.option.checkForRelaxModes"))
                    .bindSelected(::checkForRelaxModes.toAtomicProperty())
                contextHelp(PlsBundle.message("inspection.script.incorrectDefinitionInjection.option.checkForRelaxModes.tip"))
            }
        }
    }
}
