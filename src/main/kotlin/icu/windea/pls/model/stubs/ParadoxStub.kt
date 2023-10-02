package icu.windea.pls.model.stubs

import com.intellij.openapi.project.*
import icu.windea.pls.model.*

interface ParadoxStub {
    val name: String
    val gameType: ParadoxGameType
    val project: Project
}