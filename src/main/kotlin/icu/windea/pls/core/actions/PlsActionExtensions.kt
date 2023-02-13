package icu.windea.pls.core.actions

import com.intellij.openapi.actionSystem.*
import icu.windea.pls.core.*

val AnActionEvent.editor by CommonDataKeys.EDITOR

val AnActionEvent.gameTypeProperty by PlsDataKeys.gameTypePropertyKey