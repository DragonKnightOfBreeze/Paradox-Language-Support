package icu.windea.pls.lang.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import icu.windea.pls.core.getValue
import icu.windea.pls.lang.PlsDataKeys

val AnActionEvent.editor by CommonDataKeys.EDITOR

val AnActionEvent.gameType by PlsDataKeys.gameType
val AnActionEvent.gameTypeProperty by PlsDataKeys.gameTypeProperty
