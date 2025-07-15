package icu.windea.pls.ai.requests

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.config.config.CwtLocaleConfig
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.manipulators.*
import icu.windea.pls.model.*

class PlsAiPolishLocalisationRequest(
    project: Project,
    file: PsiFile?,
    inputContexts: List<ParadoxLocalisationContext>,
    inputText: String,
    inputDescription: String?,
) : PlsAiManipulateLocalisationsRequest(project, file, inputContexts, inputText, inputDescription) {
    val context by lazy { createContext() }

    fun createContext(): Context {
        val context = Context()
        context["filePath"] = filePath
        context["fileName"] = fileName
        context["modName"] = modName
        return Context()
    }

    class Context : MutableMap<String, Any?> by mutableMapOf() {
        val filePath: String? by this
        val fileName: String? by this
        val modName: String? by this
    }
}
