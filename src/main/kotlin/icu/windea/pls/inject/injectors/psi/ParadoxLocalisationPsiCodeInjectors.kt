package icu.windea.pls.inject.injectors.psi

import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectFieldCache
import icu.windea.pls.inject.annotations.InjectTarget

interface ParadoxLocalisationPsiCodeInjectors {
    // 用于优化性能

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationLocale */
    @InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationLocaleImpl", pluginId = "icu.windea.pls")
    // @InjectFieldCache("getText", cleanup = "subtreeChanged")
    @InjectFieldCache("getName", cleanUp = "subtreeChanged")
    class Locale : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationProperty */
    @InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyImpl", pluginId = "icu.windea.pls")
    // @InjectFieldCache("getText", cleanup = "subtreeChanged")
    @InjectFieldCache("getName", cleanUp = "subtreeChanged")
    // @InjectFieldCache("getValue", cleanup = "subtreeChanged")
    class Property : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyKey */
    @InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyKeyImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    class PropertyKey : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyValue */
    @InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyValueImpl", pluginId = "icu.windea.pls")
    // @InjectFieldCache("getText", cleanup = "subtreeChanged")
    class PropertyValue : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationParameter */
    @InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationParameterImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    @InjectFieldCache("getName", cleanUp = "subtreeChanged")
    class Parameter : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationIcon */
    @InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationIconImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    @InjectFieldCache("getName", cleanUp = "subtreeChanged")
    class Icon : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat */
    @InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationTextFormatImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    @InjectFieldCache("getName", cleanUp = "subtreeChanged")
    class TextFormat : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon */
    @InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationTextIconImpl", pluginId = "icu.windea.pls")
    @InjectFieldCache("getText", cleanUp = "subtreeChanged")
    @InjectFieldCache("getName", cleanUp = "subtreeChanged")
    class TextIcon : CodeInjectorBase()
}
