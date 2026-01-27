package icu.windea.pls.inject.injectors.fix

import com.intellij.codeInsight.daemon.impl.IdentifierHighlightingResult
import com.intellij.psi.PsiFile
import icu.windea.pls.core.contains
import icu.windea.pls.core.memberProperty
import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectReturnValue
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
    fun Any.computeRanges(@InjectReturnValue returnValue: IdentifierHighlightingResult): IdentifierHighlightingResult {
        run {
            val file = myPsiFile
            if (file !is ParadoxFile) return@run
            val targets = returnValue.targets
            if (targets !is MutableCollection) return@run
            if (targets.size <= 1) return@run
            val first = targets.first()
            if (!targets.any { target -> target !== first && target in first }) return@run
            targets.remove(first)
            return returnValue
        }
        return returnValue
    }
}
