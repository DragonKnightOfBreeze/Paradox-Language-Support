package icu.windea.pls.lang.manipulation

import com.intellij.openapi.project.Project
import icu.windea.pls.core.findChild
import icu.windea.pls.lang.manipulation.ParadoxConditionalStatementManipulationService.isBlockForm
import icu.windea.pls.lang.manipulation.ParadoxConditionalStatementManipulationService.isPropertyForm
import icu.windea.pls.script.psi.ParadoxScriptConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptRootBlock

/**
 * 用于操作脚本文件中的条件化语句（conditional statement）。
 *
 * 示例 - 属性形式：
 *
 * ```paradox_script
 * [[PARAM] PARAM = $PARAM$ ]
 * ```
 *
 * 示例 - 块形式：
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

    /**
     * 判断 [element] 是否为属性形式。
     */
    fun isPropertyForm(element: ParadoxScriptProperty): Boolean {
        val text = element.text
        return propertyFormRegex.matches(text)
    }

    /**
     * 判断 [element] 是否为块形式。
     */
    fun isBlockForm(element: ParadoxScriptConditionalBlock): Boolean {
        val text = element.text
        return blockFormRegex.matches(text)
    }

    /**
     * 判断 [element] 是否可以转换为属性形式。
     *
     * 说明：
     * - [element] 必须是块形式。参见 [isBlockForm]。
     */
    fun canConvertToPropertyForm(element: ParadoxScriptConditionalBlock): Boolean {
        return isBlockForm(element)
    }

    /**
     * 判断 [element] 是否可以转换为块形式。
     *
     * 说明：
     * - [element] 必须是属性形式。参见 [isPropertyForm]。
     */
    fun canConvertToBlockForm(element: ParadoxScriptProperty): Boolean {
        return isPropertyForm(element)
    }

    /**
     * 将 [element] 从块形式转换为属性形式。
     *
     * 示例：
     *
     * ```paradox_script
     * # before
     * [[PARAM] PARAM = $PARAM$ ]
     *
     * # before
     * PARAM = $PARAM|no$
     * ```
     */
    fun convertToPropertyForm(element: ParadoxScriptConditionalBlock, project: Project) {
        val text = element.text
        val matchResult = blockFormRegex.matchEntire(text) ?: return
        val parameterName = matchResult.groupValues.get(1)
        val newText = propertyTemplate.invoke(parameterName)
        val newElement = ParadoxScriptElementFactory.createFileFromText(project, newText)
            .findChild<ParadoxScriptRootBlock>()
            ?.findChild<ParadoxScriptProperty>()
            ?: return
        element.replace(newElement)
    }

    /**
     * 将 [element] 从属性形式转换为块形式。
     *
     * 示例：
     *
     * ```paradox_script
     * # before
     * PARAM = $PARAM|no$
     *
     * # before
     * [[PARAM] PARAM = $PARAM$ ]
     * ```
     */
    fun convertToBlockForm(element: ParadoxScriptProperty, project: Project) {
        val text = element.text
        val matchResult = propertyFormRegex.matchEntire(text) ?: return
        val parameterName = matchResult.groupValues.get(1)
        val newText = blockTemplate.invoke(parameterName)
        val newElement = ParadoxScriptElementFactory.createFileFromText(project, newText)
            .findChild<ParadoxScriptRootBlock>()
            ?.findChild<ParadoxScriptConditionalBlock>()
            ?: return
        element.replace(newElement)
    }
}
