package icu.windea.pls.inject.injectors

import com.intellij.codeInsight.daemon.impl.IdentifierHighlightingResult
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.ProperTextRange
import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectionTarget
import icu.windea.pls.lang.ParadoxFileType
import kotlin.coroutines.Continuation

/**
 * @see com.intellij.codeInsight.daemon.impl.IdentifierHighlightingManagerImpl
 * @see com.intellij.codeInsight.daemon.impl.IdentifierHighlightingManagerImpl.getMarkupData
 */
@Suppress("UnstableApiUsage")
@InjectionTarget("com.intellij.codeInsight.daemon.impl.IdentifierHighlightingManagerImpl")
class IdentifierHighlightingManagerImplCodeInjector : CodeInjectorBase() {
    // https://youtrack.jetbrains.com/issue/IJPL-231595/Code-logic-flaw-with-identifier-highlighting

    @Suppress("unused")
    @InjectMethod(pointer = InjectMethod.Pointer.AFTER)
    fun getMarkupData(editor: Editor, visibleRange: ProperTextRange, continuation: Continuation<*>, result: IdentifierHighlightingResult): IdentifierHighlightingResult {
        run {
            if (result.targets.size <= 1) return@run
            val vFile = editor.virtualFile ?: return@run
            if (vFile.fileType !is ParadoxFileType) return@run
            val first = result.targets.first()
            val others = result.targets.drop(1)
            if (!others.all { other -> first.startOffset <= other.startOffset && first.endOffset >= other.endOffset }) return@run
            return IdentifierHighlightingResult(result.occurrences, others)
        }
        return result
    }
}
