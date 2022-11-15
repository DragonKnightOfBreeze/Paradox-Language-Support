package icu.windea.pls.core.actions

import com.intellij.openapi.actionSystem.*

val AnActionEvent.editor get() = CommonDataKeys.EDITOR.getData(dataContext)

val AnActionEvent.gameTypeProperty get() = getData(PlsDataKeys.gameTypePropertyKey)
val AnActionEvent.rootTypeProperty get() = getData(PlsDataKeys.rootTypePropertyKey)