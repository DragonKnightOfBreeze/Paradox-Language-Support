package icu.windea.pls.config

import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*

val Project.configGroupLibrary: CwtConfigGroupLibrary
    get() = this.getOrPutUserData(PlsKeys.configGroupLibrary) { CwtConfigGroupLibrary(this) }