package icu.windea.pls.extension.markdown

import com.intellij.psi.PsiFile
import icu.windea.pls.core.util.tryPutUserData
import icu.windea.pls.inject.processors.InjectedFileProcessor
import icu.windea.pls.lang.PlsKeys

/**
 * 用于为Markdown代码块注入文件信息，从而获取其中的CWT规则上下文。
 *
 * 通过在语言ID声明之后添加 `path={gameType}:{filePath}` 来注入额外的元数据。
 */
class MarkdownCodeFenceInjectedFileProcessor : InjectedFileProcessor {
    override fun process(file: PsiFile): Boolean {
        val element = PlsMarkdownManager.getCodeFenceFromInjectedFile(file) ?: return false
        val injectedFileInfo = PlsMarkdownManager.getInjectFileInfoFromInjectedFile(element)
        file.virtualFile.tryPutUserData(PlsKeys.injectedFileInfo, injectedFileInfo)
        return true
    }
}
