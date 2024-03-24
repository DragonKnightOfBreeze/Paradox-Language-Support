package icu.windea.pls.lang.actions

import com.intellij.openapi.actionSystem.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*

val AnActionEvent.editor by CommonDataKeys.EDITOR

val AnActionEvent.gameType by PlsDataKeys.gameType
val AnActionEvent.gameTypeProperty by PlsDataKeys.gameTypeProperty