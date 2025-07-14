package icu.windea.pls.csv.psi

import icu.windea.pls.core.*
import icu.windea.pls.csv.psi.impl.*

val ParadoxCsvRow.greenStub: ParadoxCsvRowStub?
    get() = this.castOrNull<ParadoxCsvRowImpl>()?.greenStub
