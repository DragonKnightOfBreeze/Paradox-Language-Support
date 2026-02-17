@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.findChild
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptRootBlock

private val propertyFormatRegex = "(\\w+)\\s*=\\s*\\$\\1\\|no\\$".toRegex()
private val blockFormatRegex = "\\[\\[(\\w+)]\\s*\\1\\s*=\\s*\\$\\1\\$\\s*]".toRegex()

private val propertyTemplate = { p: String -> "$p = $$p|no$" }
private val blockTemplate = { p: String -> "[[$p] $p = $$p$ ]" }

/**
 * 将条件片段转换为属性格式。
 *
 * ```paradox_script
 * # before
 * [[PARAM] PARAM = $PARAM$ ]
 *
 * # after
 * PARAM = $PARAM|no$
 * ```
 */
class ConditionalSnippetToPropertyFormatIntention : PsiUpdateModCommandAction<ParadoxScriptParameterCondition>(ParadoxScriptParameterCondition::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.conditionalSnippetToPropertyFormat")

    override fun invoke(context: ActionContext, element: ParadoxScriptParameterCondition, updater: ModPsiUpdater) {
        val text = element.text
        val matchResult = blockFormatRegex.matchEntire(text) ?: return
        val parameterName = matchResult.groupValues.get(1)
        val newText = propertyTemplate.invoke(parameterName)
        val newElement = ParadoxScriptElementFactory.createDummyFile(context.project, newText)
            .findChild<ParadoxScriptRootBlock>()
            ?.findChild<ParadoxScriptProperty>()
            ?: return
        element.replace(newElement)
    }

    override fun isElementApplicable(element: ParadoxScriptParameterCondition, context: ActionContext): Boolean {
        val text = element.text
        return blockFormatRegex.matches(text)
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptParameterCondition
    }
}

/**
 * 将条件片段转为块格式。
 *
 * ```paradox_script
 * # before
 * PARAM = $PARAM|no$
 *
 * # after
 * [[PARAM] PARAM = $PARAM$ ]
 * ```
 */
class ConditionalSnippetToBlockFormatIntention : PsiUpdateModCommandAction<ParadoxScriptProperty>(ParadoxScriptProperty::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.conditionalSnippetToBlockFormat")

    override fun invoke(context: ActionContext, element: ParadoxScriptProperty, updater: ModPsiUpdater) {
        val text = element.text
        val matchResult = propertyFormatRegex.matchEntire(text) ?: return
        val parameterName = matchResult.groupValues.get(1)
        val newText = blockTemplate.invoke(parameterName)
        val newElement = ParadoxScriptElementFactory.createDummyFile(context.project, newText)
            .findChild<ParadoxScriptRootBlock>()
            ?.findChild<ParadoxScriptParameterCondition>()
            ?: return
        element.replace(newElement)
    }

    override fun isElementApplicable(element: ParadoxScriptProperty, context: ActionContext): Boolean {
        val text = element.text
        return propertyFormatRegex.matches(text)
    }

    override fun stopSearchAt(element: PsiElement, context: ActionContext): Boolean {
        return element is ParadoxScriptProperty
    }
}
