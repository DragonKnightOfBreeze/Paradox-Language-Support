package icu.windea.pls.lang.intentions.cwt

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtBoundMemberContainer
import icu.windea.pls.cwt.psi.CwtElementFactory
import icu.windea.pls.lang.intentions.ChronicleIntentionBundle

/**
 * 将成员放到同一行。适用于 [CwtBlock]。
 *
 * ```cwt
 * # before
 * {
 *     V
 *     K = V
 * }
 *
 * # after
 * { V K = V }
 * ```
 */
@Suppress("UnstableApiUsage")
class PutMembersOnOneLineIntention : PutMembersIntentionBase() {
    override fun getFamilyName() = ChronicleIntentionBundle.message("intention.putMembersOnOneLine")

    override fun invoke(context: ActionContext, element: CwtBoundMemberContainer, updater: ModPsiUpdater) {
        val membersText = getMemberTextSequence(element).joinToString(" ")
        if (membersText.isEmpty()) return

        // 由于后续会自动格式化，这里只需处理换行即可
        val newText = "{ ${membersText} }"
        val newElement = CwtElementFactory.createBlockFromText(context.project, newText)
        element.replace(newElement)
    }

    override fun isElementApplicable(element: CwtBoundMemberContainer, context: ActionContext): Boolean {
        return checkElementAvailable(element, hasLineBreak = true)
    }
}
