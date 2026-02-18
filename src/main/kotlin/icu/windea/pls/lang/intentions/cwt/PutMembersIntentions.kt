@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.cwt

import com.intellij.application.options.CodeStyle
import com.intellij.modcommand.ActionContext
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.siblings
import com.intellij.util.DocumentUtil
import com.intellij.util.text.CharArrayUtil
import icu.windea.pls.PlsBundle
import icu.windea.pls.cwt.CwtFileType
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtBoundMemberContainer
import icu.windea.pls.cwt.psi.CwtElementFactory
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.lang.codeStyle.PlsCodeStyleUtil

sealed class PutMembersIntentionBase : PsiUpdateModCommandAction<CwtBoundMemberContainer>(CwtBoundMemberContainer::class.java), DumbAware {
    protected fun getMemberTextSequence(element: CwtBoundMemberContainer): Sequence<String> {
        return element.members.asSequence().map { it.text.trim() }.filter { it.isNotEmpty() }
    }

    protected fun createBlockFromText(project: Project, text: String): CwtBlock {
        return CwtElementFactory.createBlock(project, text)
    }

    protected fun checkElementAvailable(element: CwtBoundMemberContainer, hasLineBreak: Boolean? = null): Boolean {
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
                is CwtMember -> flag = true
                else -> return false
            }
        }
        if (hasLineBreak != null && hasLineBreak != lineBreakFlag) return false
        return flag
    }
}

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
class PutMembersOnOneLineIntention : PutMembersIntentionBase() {
    override fun getFamilyName() = PlsBundle.message("intention.putMembersOnOneLine")

    override fun invoke(context: ActionContext, element: CwtBoundMemberContainer, updater: ModPsiUpdater) {
        // if (!checkElementAvailable(element)) return

        val membersText = getMemberTextSequence(element).joinToString(" ")
        if (membersText.isEmpty()) return

        val file = element.containingFile
        val spaceWithinBraces = PlsCodeStyleUtil.isSpaceWithinBraces(file)
        val newText = buildString {
            append("{")
            if (spaceWithinBraces) append(" ")
            append(membersText)
            if (spaceWithinBraces) append(" ")
            append("}")
        }
        val newElement = createBlockFromText(context.project, newText)
        element.replace(newElement)
    }

    override fun isElementApplicable(element: CwtBoundMemberContainer, context: ActionContext): Boolean {
        return checkElementAvailable(element, hasLineBreak = true)
    }
}

/**
 * 将成员放到不同的行。适用于 [CwtBlock]。
 *
 * ```cwt
 * # before
 * { V K = V }
 * # after
 * {
 *     V
 *     K = V
 * }
 * ```
 */
class PutMembersOnSeparateLinesIntention : PutMembersIntentionBase() {
    override fun getFamilyName() = PlsBundle.message("intention.putMembersOnSeparateLines")

    override fun invoke(context: ActionContext, element: CwtBoundMemberContainer, updater: ModPsiUpdater) {
        // if (!checkElementAvailable(element)) return

        val file = element.containingFile
        val settings = CodeStyle.getSettings(file)
        val indentOptions = settings.getIndentOptions(CwtFileType)
        val indentUnit = when {
            indentOptions.USE_TAB_CHARACTER -> "\t"
            else -> " ".repeat(indentOptions.INDENT_SIZE.coerceAtLeast(1))
        }
        val baseIndent = run {
            val document = PsiDocumentManager.getInstance(context.project).getDocument(file) ?: return@run ""
            val offset = element.textOffset
            val lineStartOffset = DocumentUtil.getLineStartOffset(offset, document)
            val chars = document.immutableCharSequence
            val firstNonWsLineOffset = CharArrayUtil.shiftForward(chars, lineStartOffset, " \t")
            chars.subSequence(lineStartOffset, firstNonWsLineOffset).toString()
        }
        val innerIndent = baseIndent + indentUnit

        val membersText = getMemberTextSequence(element).joinToString("\n") { innerIndent + it }
        if (membersText.isEmpty()) return

        val newText = buildString {
            append("{")
            append("\n")
            append(membersText)
            append("\n")
            append(baseIndent)
            append("}")
        }
        val newElement = createBlockFromText(context.project, newText)
        element.replace(newElement)
    }

    override fun isElementApplicable(element: CwtBoundMemberContainer, context: ActionContext): Boolean {
        return checkElementAvailable(element, hasLineBreak = false)
    }
}
