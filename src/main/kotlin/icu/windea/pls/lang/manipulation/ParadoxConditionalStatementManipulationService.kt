package icu.windea.pls.lang.manipulation

import com.intellij.openapi.project.Project
import icu.windea.pls.core.findChild
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptRootBlock

/**
 * 用于操作脚本文件中的条件化语句（conditional statement）。
 *
 * 属性形式：
 *
 * ```paradox_script
 * [[PARAM] PARAM = $PARAM$ ]
 * ```
 *
 * 块形式：
 *
 * ```paradox_script
 * PARAM = $PARAM|no$
 * ```
 */
object ParadoxConditionalStatementManipulationService {
    private val propertyFormRegex = "(\\w+)\\s*=\\s*\\$\\1\\|no\\$".toRegex()
    private val blockFormRegex = "\\[\\[(\\w+)]\\s*\\1\\s*=\\s*\\$\\1\\$\\s*]".toRegex()

    private val propertyTemplate = { p: String -> "$p = $$p|no$" }
    private val blockTemplate = { p: String -> "[[$p] $p = $$p$ ]" }

    fun isPropertyForm(element: ParadoxScriptProperty): Boolean {
        val text = element.text
        return propertyFormRegex.matches(text)
    }

    fun isBlockForm(element: ParadoxScriptParameterCondition): Boolean {
        val text = element.text
        return blockFormRegex.matches(text)
    }

    fun convertToPropertyForm(element: ParadoxScriptParameterCondition, project: Project) {
        val text = element.text
        val matchResult = blockFormRegex.matchEntire(text) ?: return
        val parameterName = matchResult.groupValues.get(1)
        val newText = propertyTemplate.invoke(parameterName)
        val newElement = ParadoxScriptElementFactory.createDummyFile(project, newText)
            .findChild<ParadoxScriptRootBlock>()
            ?.findChild<ParadoxScriptProperty>()
            ?: return
        element.replace(newElement)
    }

    fun convertToBlockForm(element: ParadoxScriptProperty, project: Project) {
        val text = element.text
        val matchResult = propertyFormRegex.matchEntire(text) ?: return
        val parameterName = matchResult.groupValues.get(1)
        val newText = blockTemplate.invoke(parameterName)
        val newElement = ParadoxScriptElementFactory.createDummyFile(project, newText)
            .findChild<ParadoxScriptRootBlock>()
            ?.findChild<ParadoxScriptParameterCondition>()
            ?: return
        element.replace(newElement)
    }
}
