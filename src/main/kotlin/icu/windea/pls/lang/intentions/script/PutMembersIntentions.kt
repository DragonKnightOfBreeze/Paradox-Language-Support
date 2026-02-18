@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.siblings
import icu.windea.pls.PlsBundle
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptBoundMemberContainer
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition

sealed class PutMembersIntentionBase : PsiUpdateModCommandAction<ParadoxScriptBoundMemberContainer>(ParadoxScriptBoundMemberContainer::class.java), DumbAware {
    protected fun getMemberTextSequence(element: ParadoxScriptBoundMemberContainer): Sequence<String> {
        return element.members.asSequence().map { it.text.trim() }.filter { it.isNotEmpty() }
    }

    protected fun checkElementAvailable(element: ParadoxScriptBoundMemberContainer, hasLineBreak: Boolean? = null): Boolean {
        // 块中存在成员元素（包括仅存在一个的情况），且不存在空白以外的非成员元素（如注释）
        val leftBound = element.leftBound ?: return false
        val rightBound = element.rightBound ?: return false
        var flag = false
        var lineBreakFlag = false
        for (e in leftBound.siblings(withSelf = false).takeWhile { it != rightBound }) {
            when (e) {
                is PsiWhiteSpace -> {
                    if (hasLineBreak != null && !lineBreakFlag) lineBreakFlag = e.textContains('\n')
                    continue
                }
                is ParadoxScriptMember -> flag = true
                else -> return false
            }
        }
        if (hasLineBreak != null && hasLineBreak != lineBreakFlag) return false
        return flag
    }
}

/**
 * 将成员放到同一行。适用于 [ParadoxScriptBlock] 和 [ParadoxScriptParameterCondition]。
 *
 * ```paradox_script
 * # before
 * {
 *     V
 *     K = V
 * }
 * [[P]
 *     V
 *     K = V
 * ]
 *
 * # after
 * { V K = V }
 * [[P] V K = V ]
 * ```
 */
class PutMembersOnOneLineIntention : PutMembersIntentionBase() {
    override fun getFamilyName() = PlsBundle.message("intention.putMembersOnOneLine")

    override fun invoke(context: ActionContext, element: ParadoxScriptBoundMemberContainer, updater: ModPsiUpdater) {
        // if (!checkElementAvailable(element)) return

        val membersText = getMemberTextSequence(element).joinToString(" ")
        if (membersText.isEmpty()) return

        // 由于后续会自动格式化，这里只需处理换行即可
        val newElement = when (element) {
            is ParadoxScriptParameterCondition -> {
                val conditionExpression = element.conditionExpression ?: return
                val newText = "[[${conditionExpression}] ${membersText} ]"
                ParadoxScriptElementFactory.createParameterConditionFromText(context.project, newText)
            }
            else -> {
                val newText = "{ ${membersText} }"
                ParadoxScriptElementFactory.createBlockFromText(context.project, newText)
            }
        }
        element.replace(newElement)
    }

    override fun isElementApplicable(element: ParadoxScriptBoundMemberContainer, context: ActionContext): Boolean {
        return checkElementAvailable(element, hasLineBreak = true)
    }
}

/**
 * 将成员放到不同的行。适用于 [ParadoxScriptBlock] 和 [ParadoxScriptParameterCondition]。
 *
 * ```paradox_script
 * # before
 * { V K = V }
 * [[P] V K = V ]
 *
 * # after
 * {
 *     V
 *     K = V
 * }
 * [[P]
 *     V
 *     K = V
 * ]
 * ```
 */
class PutMembersOnSeparateLinesIntention : PutMembersIntentionBase() {
    override fun getFamilyName() = PlsBundle.message("intention.putMembersOnSeparateLines")

    override fun invoke(context: ActionContext, element: ParadoxScriptBoundMemberContainer, updater: ModPsiUpdater) {
        if (!checkElementAvailable(element)) return

        val membersText = getMemberTextSequence(element).joinToString("\n")
        if (membersText.isEmpty()) return

        // 由于后续会自动格式化，这里只需处理换行即可
        val newElement = when (element) {
            is ParadoxScriptParameterCondition -> {
                val conditionExpression = element.conditionExpression ?: return
                val newText = "[[${conditionExpression}]\n${membersText}\n]"
                ParadoxScriptElementFactory.createParameterConditionFromText(context.project, newText)
            }
            else -> {
                val newText = "{\n${membersText}\n}"
                ParadoxScriptElementFactory.createBlockFromText(context.project, newText)
            }
        }
        element.replace(newElement)
    }

    override fun isElementApplicable(element: ParadoxScriptBoundMemberContainer, context: ActionContext): Boolean {
        return checkElementAvailable(element, hasLineBreak = false)
    }
}
