package icu.windea.pls.lang.intentions.script

import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiWhiteSpace
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.script.psi.ParadoxScriptBoundMemberContainer
import icu.windea.pls.script.psi.ParadoxScriptMember

@Suppress("UnstableApiUsage")
abstract class PutMembersIntentionBase : PsiUpdateModCommandAction<ParadoxScriptBoundMemberContainer>(ParadoxScriptBoundMemberContainer::class.java), DumbAware {
    protected fun getMemberTextSequence(element: ParadoxScriptBoundMemberContainer): Sequence<String> {
        return element.members.asSequence().map { it.text.trim() }.filter { it.isNotEmpty() }
    }

    protected fun checkElementAvailable(element: ParadoxScriptBoundMemberContainer, hasLineBreak: Boolean? = null): Boolean {
        // 块中存在成员元素（包括仅存在一个的情况），且不存在空白以外的非成员元素（如注释）
        val collected = PsiService.collectBetweenBounds(element) ?: return false
        var flag = false
        var lineBreakFlag = false
        for (e in collected) {
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

