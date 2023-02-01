package icu.windea.pls.core

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.stubs.*
import com.intellij.psi.util.*
import icu.windea.pls.config.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.script.*

@Service(Service.Level.PROJECT)
class ParadoxModificationTrackerProvider(
    project: Project
) {
    val Modifier = PsiModificationTracker.getInstance(project).forLanguage(ParadoxScriptLanguage)
    
    @Suppress("UnstableApiUsage") val InlineScript = StubIndex.getInstance().cast<StubIndexImpl>()
        .getIndexModificationTracker(ParadoxInlineScriptIndex.key, project)
}