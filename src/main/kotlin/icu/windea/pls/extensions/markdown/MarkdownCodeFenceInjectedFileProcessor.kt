package icu.windea.pls.extensions.markdown

import com.intellij.psi.PsiFile
import icu.windea.pls.inject.processors.InjectedFileProcessor
import icu.windea.pls.lang.analysis.ParadoxAnalysisInjector
import icu.windea.pls.lang.util.PlsFileManager

/**
 * 用于为 Markdown 代码块注入文件信息，从而获取其中的规则上下文。
 *
 * 通过在语言 ID 声明之后添加 `path={gameType}:{filePath}` 来注入额外的元数据。
 */
class MarkdownCodeFenceInjectedFileProcessor : InjectedFileProcessor {
    override fun process(file: PsiFile): Boolean {
        val vFile = file.virtualFile
        if (PlsFileManager.isStubFile(vFile)) return false
        val element = PlsMarkdownManager.getCodeFenceFromInjectedFile(file) ?: return false
        val injectedFileInfo = PlsMarkdownManager.getInjectedFileInfoFromInjectedFile(element)
        ParadoxAnalysisInjector.injectFileInfo(vFile, injectedFileInfo)
        return true
    }
}
