package icu.windea.pls.core

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.stubs.*
import com.intellij.psi.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.core.index.*
import icu.windea.pls.script.*

@Service(Service.Level.PROJECT)
class ParadoxModificationTrackerProvider(
    project: Project
) {
    companion object {
        fun getInstance() = this
        
        fun getInstance(project: Project) = project.service<ParadoxModificationTrackerProvider>()
    
        val DefinitionMemberInfo = SimpleModificationTracker()
        val InlineScript = SimpleModificationTracker()
        val Modifier = SimpleModificationTracker()
    }
}