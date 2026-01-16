package icu.windea.pls.lang.resolve

import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

class CwtLocalisationLocationResolveResult(
    val name: String,
    val message: String? = null,
    resolveAction: () -> ParadoxLocalisationProperty? = { null },
    resolveAllAction: () -> Collection<ParadoxLocalisationProperty> = { emptySet() },
) {
    val element: ParadoxLocalisationProperty? by lazy { resolveAction() }
    val elements: Collection<ParadoxLocalisationProperty> by lazy { resolveAllAction() }
}
