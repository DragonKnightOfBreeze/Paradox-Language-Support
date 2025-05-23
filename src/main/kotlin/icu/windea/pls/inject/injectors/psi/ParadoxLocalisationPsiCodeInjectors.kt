package icu.windea.pls.inject.injectors.psi

import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*

interface ParadoxLocalisationPsiCodeInjectors {
    //用于优化性能

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationLocale */
    @InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationLocaleImpl", pluginId = "icu.windea.pls")
    @InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
    @InjectFieldBasedCache("getName", cleanup = "subtreeChanged")
    class Locale : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationProperty */
    @InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyImpl", pluginId = "icu.windea.pls")
    @InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
    @InjectFieldBasedCache("getName", cleanup = "subtreeChanged")
    @InjectFieldBasedCache("getValue", cleanup = "subtreeChanged")
    class Property : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyKey */
    @InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyKeyImpl", pluginId = "icu.windea.pls")
    @InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
    class PropertyKey : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyValue */
    @InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyValueImpl", pluginId = "icu.windea.pls")
    @InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
    class PropertyValue : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationParameter */
    @InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationParameterImpl", pluginId = "icu.windea.pls")
    @InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
    @InjectFieldBasedCache("getName", cleanup = "subtreeChanged")
    class Parameter : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationIcon */
    @InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationIconImpl", pluginId = "icu.windea.pls")
    @InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
    @InjectFieldBasedCache("getName", cleanup = "subtreeChanged")
    class Icon : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat */
    @InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationTextFormatImpl", pluginId = "icu.windea.pls")
    @InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
    @InjectFieldBasedCache("getName", cleanup = "subtreeChanged")
    class TextFormat : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon */
    @InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationTextIconImpl", pluginId = "icu.windea.pls")
    @InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
    @InjectFieldBasedCache("getName", cleanup = "subtreeChanged")
    class TextIcon : CodeInjectorBase()
}
