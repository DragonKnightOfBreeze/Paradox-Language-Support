package icu.windea.pls.lang.actions

import com.intellij.openapi.actionSystem.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*

val AnActionEvent.editor by CommonDataKeys.EDITOR

val AnActionEvent.gameType by PlsDataKeys.gameType
val AnActionEvent.gameTypeProperty by PlsDataKeys.gameTypeProperty
