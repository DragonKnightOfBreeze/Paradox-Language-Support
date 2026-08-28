package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import icu.windea.pls.lang.intentions.ChronicleIntentionBundle
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptBoundMemberContainer
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptNormalConditionalBlock

/**
 * 将成员放到同一行。适用于 [ParadoxScriptBlock] 和 [ParadoxScriptNormalConditionalBlock]。
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
@Suppress("UnstableApiUsage")
class PutMembersOnOneLineIntention : PutMembersIntentionBase() {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.putMembersOnOneLine")

    override fun invoke(context: ActionContext, element: ParadoxScriptBoundMemberContainer, updater: ModPsiUpdater) {
        // if (!checkElementAvailable(element)) return

        val membersText = getMemberTextSequence(element).joinToString(" ")
        if (membersText.isEmpty()) return

        // 由于后续会自动格式化，这里只需处理换行即可
        val newElement = when (element) {
            is ParadoxScriptNormalConditionalBlock -> {
                val expressionText = element.conditionalExpression?.presentableText ?: return
                val newText = "[[${expressionText}] ${membersText} ]"
                ParadoxScriptElementFactory.createConditionalBlockFromText(context.project, newText)
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
