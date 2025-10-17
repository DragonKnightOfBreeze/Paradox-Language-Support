package icu.windea.pls.lang.execution.filters

import com.intellij.diff.DiffDialogHints
import com.intellij.diff.DiffManager
import com.intellij.diff.chains.DiffRequestChain
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import icu.windea.pls.lang.diff.DiffWindowWrapper

/**
 * 控制台文本中的超链接信息，点击打开 DIFF 窗口。
 *
 * @property reusable 如果为 true，则会复用先前打开的未关闭的 DIFF 视图，点击导航时，直接将其前置。
 */
class ShowDiffWindowHyperlinkInfo(
    project: Project,
    private val requests: DiffRequestChain,
    private val hints: DiffDialogHints = DiffDialogHints.DEFAULT,
    private val reusable: Boolean = true,
) : HyperlinkInfo, Disposable {
    private val diffWindowWrapper = DiffWindowWrapper(project, requests, hints)

    override fun navigate(project: Project) {
        try {
            if (reusable) {
                diffWindowWrapper.showOrToFront()
            } else {
                diffWindowWrapper.show()
            }
        } catch (e: Exception) {
            thisLogger().warn(e)
            // 回退，直接调用 DiffManager.showDiff()
            DiffManager.getInstance().showDiff(project, requests, hints)
        }
    }

    override fun dispose() {
        diffWindowWrapper.dispose()
    }
}
