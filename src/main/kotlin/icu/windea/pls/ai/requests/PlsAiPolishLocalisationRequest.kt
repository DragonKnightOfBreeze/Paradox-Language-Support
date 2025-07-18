package icu.windea.pls.ai.requests

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.lang.util.manipulators.*

class PlsAiPolishLocalisationRequest(
    project: Project,
    file: PsiFile?,
    localisationContexts: List<ParadoxLocalisationContext>,
    description: String?,
) : PlsAiManipulateLocalisationsRequest(project, file, localisationContexts, description) {
    val context by lazy { createContext() }

    fun createContext(): Context {
        val map = mutableMapOf<String, Any?>()
        map["filePath"] = filePath
        map["fileName"] = fileName
        map["modName"] = modName
        return Context(map)
    }

    class Context(map: Map<String, Any?> = emptyMap()) : Map<String, Any?> by map {
        val filePath: String? by this
        val fileName: String? by this
        val modName: String? by this
    }
}
