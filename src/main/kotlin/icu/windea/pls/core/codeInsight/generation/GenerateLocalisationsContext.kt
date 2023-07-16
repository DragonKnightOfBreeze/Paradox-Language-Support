@file:Suppress("UnusedReceiverParameter")

package icu.windea.pls.core.codeInsight.generation

import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*

data class GenerateLocalisationsContext(
    val definitionName: String,
    val localisationNames: Set<String>
) {
    lateinit var project: Project
    lateinit var editor: Editor
    lateinit var file: PsiFile
}

val PlsKeys.generateLocalisationsContext by lazy { Key.create<GenerateLocalisationsContext>("paradox.generateLocalisationContext") }