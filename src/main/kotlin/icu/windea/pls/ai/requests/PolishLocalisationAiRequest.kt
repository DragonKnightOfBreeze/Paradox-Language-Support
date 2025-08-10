package icu.windea.pls.ai.requests

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.lang.util.manipulators.*
import java.util.concurrent.atomic.AtomicInteger

class PolishLocalisationAiRequest(
   project: Project,
   file: PsiFile,
   localisationContexts: List<ParadoxLocalisationContext>,
   val description: String?,
) : ManipulateLocalisationAiRequest(project, file, localisationContexts) {
    companion object {
        private val idIncrementer = AtomicInteger()
    }

    override val requestId by lazy { idIncrementer.getAndIncrement().toString() }
}
