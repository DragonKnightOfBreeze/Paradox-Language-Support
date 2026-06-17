@file:Suppress("unused")

package icu.windea.pls.model.constraints

import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.paths.ParadoxPath

infix fun ParadoxPath.matchesBy(constraint: ParadoxPathConstraint): Boolean = constraint.test(this)

infix fun ParadoxGameType.matchesBy(constraint: ParadoxGameTypeConstraint): Boolean = constraint.test(this)
