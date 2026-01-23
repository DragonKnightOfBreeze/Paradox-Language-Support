package icu.windea.pls.inject.injectors.fix

import com.intellij.codeInsight.daemon.impl.IdentifierHighlightingResult
import com.intellij.psi.PsiFile
import icu.windea.pls.core.memberProperty
import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectionTarget
import icu.windea.pls.lang.psi.ParadoxFile

@Suppress("UnstableApiUsage")
@InjectionTarget("com.intellij.codeInsight.daemon.impl.IdentifierHighlightingComputer")
class IdentifierHighlightingComputerCodeInjector : CodeInjectorBase() {
    // https://youtrack.jetbrains.com/issue/IJPL-231595/Code-logic-flaw-with-identifier-highlighting

    // see: com.intellij.codeInsight.daemon.impl.IdentifierHighlightingComputer
    // see: com.intellij.codeInsight.daemon.impl.IdentifierHighlightingComputer.computeRanges

    private val Any.myPsiFile: PsiFile by memberProperty("myPsiFile", null)

    @Suppress("unused")
    @InjectMethod(pointer = InjectMethod.Pointer.AFTER)
    fun Any.computeRanges(returnValue: IdentifierHighlightingResult): IdentifierHighlightingResult {
        run {
            if (returnValue.targets.size <= 1) return@run
            if (myPsiFile !is ParadoxFile) return@run
            val first = returnValue.targets.first()
            val others = returnValue.targets.drop(1)
            if (!others.any { other -> first.startOffset <= other.startOffset && first.endOffset >= other.endOffset }) return@run
            return IdentifierHighlightingResult(returnValue.occurrences, others)
        }
        return returnValue
    }
}
