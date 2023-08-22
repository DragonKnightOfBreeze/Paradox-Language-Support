package icu.windea.pls.inject.injectors

import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*

//这些代码注入器用于优化性能

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptPropertyImpl", pluginId = "icu.windea.pls")
@InjectCachedMethods(methods = ["getText", "getValue", "getName"], cleanupMethod = "subtreeChanged")
class ParadoxScriptPropertyCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptPropertyKeyImpl", pluginId = "icu.windea.pls")
@InjectCachedMethods(methods = ["getText", "getValue"], cleanupMethod = "subtreeChanged")
class ParadoxScriptPropertyKeyCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptBooleanImpl", pluginId = "icu.windea.pls")
@InjectCachedMethods(methods = ["getText", "getValue"], cleanupMethod = "subtreeChanged")
class ParadoxScriptBooleanCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptIntImpl", pluginId = "icu.windea.pls")
@InjectCachedMethods(methods = ["getText", "getValue"], cleanupMethod = "subtreeChanged")
class ParadoxScriptIntCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptFloatImpl", pluginId = "icu.windea.pls")
@InjectCachedMethods(methods = ["getText", "getValue"], cleanupMethod = "subtreeChanged")
class ParadoxScriptFloatCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptStringImpl", pluginId = "icu.windea.pls")
@InjectCachedMethods(methods = ["getText", "getValue"], cleanupMethod = "subtreeChanged")
class ParadoxScriptStringCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptColorImpl", pluginId = "icu.windea.pls")
@InjectCachedMethods(methods = ["getText", "getValue", "getColorType", "getColorArgs"], cleanupMethod = "subtreeChanged")
class ParadoxScriptColorCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptParameterConditionParameterImpl", pluginId = "icu.windea.pls")
@InjectCachedMethods(methods = ["getText", "getValue"], cleanupMethod = "subtreeChanged")
class ParadoxScriptConditionParameterCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptParameterImpl", pluginId = "icu.windea.pls")
@InjectCachedMethods(methods = ["getText", "getValue"], cleanupMethod = "subtreeChanged")
class ParadoxScriptParameterCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptInlineMathParameterImpl", pluginId = "icu.windea.pls")
@InjectCachedMethods(methods = ["getText", "getValue"], cleanupMethod = "subtreeChanged")
class ParadoxScriptInlineMathParameterCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationLocaleImpl", pluginId = "icu.windea.pls")
@InjectCachedMethods(methods = ["getText", "getName"], cleanupMethod = "subtreeChanged")
class ParadoxLocalisationLocaleCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyImpl", pluginId = "icu.windea.pls")
@InjectCachedMethods(methods = ["getText", "getValue", "getName"], cleanupMethod = "subtreeChanged")
class ParadoxLocalisationPropertyCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyKeyImpl", pluginId = "icu.windea.pls")
@InjectCachedMethods(methods = ["getText"], cleanupMethod = "subtreeChanged")
class ParadoxLocalisationPropertyKeyCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationStringImpl", pluginId = "icu.windea.pls")
@InjectCachedMethods(methods = ["getText"], cleanupMethod = "subtreeChanged")
class ParadoxLocalisationStringCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyReferenceImpl", pluginId = "icu.windea.pls")
@InjectCachedMethods(methods = ["getText", "getName"], cleanupMethod = "subtreeChanged")
class ParadoxLocalisationPropertyReferenceCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationIconImpl", pluginId = "icu.windea.pls")
@InjectCachedMethods(methods = ["getText", "getName"], cleanupMethod = "subtreeChanged")
class ParadoxLocalisationIconCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationCommandScopeImpl", pluginId = "icu.windea.pls")
@InjectCachedMethods(methods = ["getText", "getName"], cleanupMethod = "subtreeChanged")
class ParadoxLocalisationCommandScopeCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationCommandFieldImpl", pluginId = "icu.windea.pls")
@InjectCachedMethods(methods = ["getText", "getName"], cleanupMethod = "subtreeChanged")
class ParadoxLocalisationCommandFieldCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationConceptNameImpl", pluginId = "icu.windea.pls")
@InjectCachedMethods(methods = ["getText", "getName"], cleanupMethod = "subtreeChanged")
class ParadoxLocalisationConceptNameCodeInjector : CodeInjectorBase()