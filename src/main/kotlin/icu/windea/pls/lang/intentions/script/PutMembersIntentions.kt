@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.intentions.script

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
import com.intellij.util.IncorrectOperationException
import com.intellij.util.text.CharArrayUtil
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.findChild
import icu.windea.pls.lang.codeStyle.PlsCodeStyleUtil
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptBoundMemberContainer
import icu.windea.pls.script.psi.ParadoxScriptElementFactory
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptProperty

sealed class PutMembersIntentionBase : PsiUpdateModCommandAction<ParadoxScriptBoundMemberContainer>(ParadoxScriptBoundMemberContainer::class.java), DumbAware {
    protected fun getMemberTextSequence(element: ParadoxScriptBoundMemberContainer): Sequence<String> {
        return element.members.asSequence().map { it.text.trim() }.filter { it.isNotEmpty() }
    }

    protected fun createBlockFromText(project: Project, text: String): ParadoxScriptBlock {
        return ParadoxScriptElementFactory.createBlock(project, text)
    }

    protected fun createParameterConditionFromText(project: Project, text: String): ParadoxScriptParameterCondition {
        return ParadoxScriptElementFactory.createRootBlock(project, "a = { $text }")
            .findChild<ParadoxScriptProperty>()
            ?.findChild<ParadoxScriptBlock>()
            ?.findChild<ParadoxScriptParameterCondition>()
            ?: throw IncorrectOperationException()
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

        val file = element.containingFile
        val spaceWithinBraces = PlsCodeStyleUtil.isSpaceWithinBraces(file)
        val newElement = when (element) {
            is ParadoxScriptParameterCondition -> {
                val conditionExpression = element.conditionExpression ?: return
                val newText = buildString {
                    append("[")
                    append("[")
                    append(conditionExpression)
                    append("]")
                    if (spaceWithinBraces) append(" ")
                    append(membersText)
                    if (spaceWithinBraces) append(" ")
                    append("]")
                }
                createParameterConditionFromText(context.project, newText)
            }
            else -> {
                val newText = buildString {
                    append("{")
                    if (spaceWithinBraces) append(" ")
                    append(membersText)
                    if (spaceWithinBraces) append(" ")
                    append("}")
                }
                createBlockFromText(context.project, newText)
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

        val file = element.containingFile
        val settings = CodeStyle.getSettings(file)
        val indentOptions = settings.getIndentOptions(ParadoxScriptFileType)
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

        val newElement = when (element) {
            is ParadoxScriptParameterCondition -> {
                val conditionExpression = element.conditionExpression ?: return
                val newText = buildString {
                    append("[")
                    append("[")
                    append(conditionExpression)
                    append("]")
                    append("\n")
                    append(membersText)
                    append("\n")
                    append(baseIndent)
                    append("]")
                }
                createParameterConditionFromText(context.project, newText)
            }
            else -> {
                val newText = buildString {
                    append("{")
                    append("\n")
                    append(membersText)
                    append("\n")
                    append(baseIndent)
                    append("}")
                }
                createBlockFromText(context.project, newText)
            }
        }
        element.replace(newElement)
    }

    override fun isElementApplicable(element: ParadoxScriptBoundMemberContainer, context: ActionContext): Boolean {
        return checkElementAvailable(element, hasLineBreak = false)
    }
}
