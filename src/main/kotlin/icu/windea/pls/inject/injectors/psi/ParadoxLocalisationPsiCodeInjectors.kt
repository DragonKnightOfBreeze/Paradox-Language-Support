package icu.windea.pls.inject.injectors.psi

import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.FieldCache
import icu.windea.pls.inject.annotations.InjectionTarget

interface ParadoxLocalisationPsiCodeInjectors {
    // 用于优化性能

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationLocale */
    @InjectionTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationLocaleImpl", pluginId = "icu.windea.pls")
    // @FieldCache("getText", cleanup = "subtreeChanged")
    @FieldCache("getName", cleanUp = "subtreeChanged")
    class Locale : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationProperty */
    @InjectionTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyImpl", pluginId = "icu.windea.pls")
    // @FieldCache("getText", cleanup = "subtreeChanged")
    @FieldCache("getName", cleanUp = "subtreeChanged")
    // @FieldCache("getValue", cleanup = "subtreeChanged")
    class Property : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyKey */
    @InjectionTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyKeyImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    class PropertyKey : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyValue */
    @InjectionTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyValueImpl", pluginId = "icu.windea.pls")
    // @FieldCache("getText", cleanup = "subtreeChanged")
    class PropertyValue : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationParameter */
    @InjectionTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationParameterImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    @FieldCache("getName", cleanUp = "subtreeChanged")
    class Parameter : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationIcon */
    @InjectionTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationIconImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    @FieldCache("getName", cleanUp = "subtreeChanged")
    class Icon : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat */
    @InjectionTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationTextFormatImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    @FieldCache("getName", cleanUp = "subtreeChanged")
    class TextFormat : CodeInjectorBase()

    /** @see icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon */
    @InjectionTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationTextIconImpl", pluginId = "icu.windea.pls")
    @FieldCache("getText", cleanUp = "subtreeChanged")
    @FieldCache("getName", cleanUp = "subtreeChanged")
    class TextIcon : CodeInjectorBase()
}
