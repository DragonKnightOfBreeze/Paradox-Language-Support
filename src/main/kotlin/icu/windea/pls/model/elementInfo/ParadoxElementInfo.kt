package icu.windea.pls.model.elementInfo

import com.intellij.openapi.project.*
import icu.windea.pls.model.*

interface ParadoxElementInfo {
    val name: String
    val gameType: ParadoxGameType
    val project: Project
}