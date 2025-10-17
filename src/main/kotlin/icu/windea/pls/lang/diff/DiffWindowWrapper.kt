package icu.windea.pls.lang.diff

import com.intellij.diff.DiffDialogHints
import com.intellij.diff.chains.DiffRequestChain
import com.intellij.diff.impl.DiffWindow
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.WindowWrapper
import icu.windea.pls.core.memberFunction
import icu.windea.pls.core.memberProperty

/**
 * DIFF 窗口的包装器。
 */
class DiffWindowWrapper(
    val project: Project,
    val requests: DiffRequestChain,
    val hints: DiffDialogHints = DiffDialogHints.DEFAULT
) : Disposable {
    companion object {
        private val showFunction = memberFunction<DiffWindow>("show")
        private val DiffWindow.myWrapper by memberProperty<WindowWrapper?>("myWrapper", "com.intellij.diff.impl.DiffWindowBase")
    }

    @Volatile
    private var diffWindow: DiffWindow? = null

    fun createDiffWindow(): DiffWindow {
        return DiffWindow(project, requests, hints)
    }

    fun getOrCreateDiffWindow(): DiffWindow {
        val old = diffWindow
        if (old != null && old.myWrapper?.isDisposed == false) return old
        val new = createDiffWindow().also { diffWindow = it }
        return new
    }

    /**
     * 打开新的 DIFF 窗口。
     */
    fun show() {
        val diffWindow = createDiffWindow()
        showFunction(diffWindow)
    }

    /**
     * 如果先前打开的 DIFF 窗口未关闭，则会直接将其前置。否则打开新的 DIFF 窗口。
     */
    fun showOrToFront() {
        val diffWindow = getOrCreateDiffWindow()
        val window = diffWindow.myWrapper?.window
        if (window?.isVisible != true) {
            showFunction(diffWindow)
        } else {
            window.toFront()
        }
    }

    override fun dispose() {
        diffWindow = null
    }
}
