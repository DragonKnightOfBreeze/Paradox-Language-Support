@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.elementType
import com.intellij.psi.util.siblings
import icu.windea.pls.PlsBundle
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptMember

private fun checkElementAvailable(element: ParadoxScriptBlock): Boolean {
    // 块中存在成员元素（包括仅存在一个的情况），且不存在空白以外的非成员元素（如注释）
    val leftBrace = element.firstChild?.takeIf { it.elementType == ParadoxScriptElementTypes.LEFT_BRACE } ?: return false
    val rightBrace = element.lastChild?.takeIf { it.elementType == ParadoxScriptElementTypes.RIGHT_BRACE } ?: return false
    var flag = false
    for (e in leftBrace.siblings(withSelf = false).takeWhile { it != rightBrace }) {
        when (e) {
            is PsiWhiteSpace -> continue
            is ParadoxScriptMember -> flag = true
            else -> return false
        }
    }
    return flag
}

/**
 * 将成员放到同一行。
 *
 * ```paradox_script
 * # before
 * KEY = {
 *     V
 *     K = V
 * }
 * # after
 * KEY = { V K = V }
 * ```
 */
class PutMembersOnOneLineIntention : PsiUpdateModCommandAction<ParadoxScriptBlock>(ParadoxScriptBlock::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.putMembersOnOneLine")

    override fun invoke(context: ActionContext, element: ParadoxScriptBlock, updater: ModPsiUpdater) {
        TODO()
    }

    override fun isElementApplicable(element: ParadoxScriptBlock, context: ActionContext): Boolean {
        return checkElementAvailable(element)
    }
}

/**
 * 将成员放到不同的行。
 *
 * ```paradox_script
 * # before
 * KEY = { V K = V }
 * # after
 * KEY = {
 *     V
 *     K = V
 * }
 * ```
 */
class PutMembersOnSeparateLinesIntention : PsiUpdateModCommandAction<ParadoxScriptBlock>(ParadoxScriptBlock::class.java), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.putMembersOnSeparateLines")

    override fun invoke(context: ActionContext, element: ParadoxScriptBlock, updater: ModPsiUpdater) {
        TODO()
    }

    override fun isElementApplicable(element: ParadoxScriptBlock, context: ActionContext): Boolean {
        return checkElementAvailable(element)
    }
}
