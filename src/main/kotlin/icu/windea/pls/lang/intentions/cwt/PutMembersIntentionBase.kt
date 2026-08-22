package icu.windea.pls.lang.intentions.cwt

import com.intellij.modcommand.PsiUpdateModCommandAction
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiWhiteSpace
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.cwt.psi.CwtBoundMemberContainer
import icu.windea.pls.cwt.psi.CwtMember

@Suppress("UnstableApiUsage")
abstract class PutMembersIntentionBase : PsiUpdateModCommandAction<CwtBoundMemberContainer>(CwtBoundMemberContainer::class.java), DumbAware {
    protected fun getMemberTextSequence(element: CwtBoundMemberContainer): Sequence<String> {
        return element.members.asSequence().map { it.text.trim() }.filter { it.isNotEmpty() }
    }

    protected fun checkElementAvailable(element: CwtBoundMemberContainer, hasLineBreak: Boolean? = null): Boolean {
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
                is CwtMember -> flag = true
                else -> return false
            }
        }
        if (hasLineBreak != null && hasLineBreak != lineBreakFlag) return false
        return flag
    }
}

