package icu.windea.pls.localisation.psi

import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyImpl

val ParadoxLocalisationProperty.greenStub: ParadoxLocalisationPropertyStub?
    get() = this.castOrNull<ParadoxLocalisationPropertyImpl>()?.greenStub ?: this.stub