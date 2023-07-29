package icu.windea.pls.lang.cwt

import com.intellij.openapi.util.*

val CwtConfigGroup.Keys.parameterModificationTracker by lazy { Key.create<ModificationTracker>("paradox.definition.parameter.modificationTracker") }