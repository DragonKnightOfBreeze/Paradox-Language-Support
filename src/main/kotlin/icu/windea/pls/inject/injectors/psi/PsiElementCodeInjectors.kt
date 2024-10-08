package icu.windea.pls.inject.injectors.psi

import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*

//这些代码注入器用于优化性能

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptPropertyImpl", pluginId = "icu.windea.pls")
@InjectFieldBasedCache("getName", cleanup = "subtreeChanged")
@InjectFieldBasedCache("getValue", cleanup = "subtreeChanged")
@InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
class ParadoxScriptPropertyCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptPropertyKeyImpl", pluginId = "icu.windea.pls")
//@InjectFieldBasedCache("getName", cleanup = "subtreeChanged") // = value
@InjectFieldBasedCache("getValue", cleanup = "subtreeChanged")
@InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
class ParadoxScriptPropertyKeyCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptBooleanImpl", pluginId = "icu.windea.pls")
//@InjectFieldBasedCache("getName", cleanup = "subtreeChanged") // = value
//@InjectFieldBasedCache("getValue", cleanup = "subtreeChanged") // = text
@InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
class ParadoxScriptBooleanCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptIntImpl", pluginId = "icu.windea.pls")
//@InjectFieldBasedCache("getName", cleanup = "subtreeChanged") // = value
//@InjectFieldBasedCache("getValue", cleanup = "subtreeChanged") // = text
@InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
class ParadoxScriptIntCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptFloatImpl", pluginId = "icu.windea.pls")
//@InjectFieldBasedCache("getName", cleanup = "subtreeChanged") // = value
//@InjectFieldBasedCache("getValue", cleanup = "subtreeChanged") // = text
@InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
class ParadoxScriptFloatCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptStringImpl", pluginId = "icu.windea.pls")
//@InjectFieldBasedCache("getName", cleanup = "subtreeChanged") // = value
@InjectFieldBasedCache("getValue", cleanup = "subtreeChanged")
@InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
class ParadoxScriptStringCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptColorImpl", pluginId = "icu.windea.pls")
//@InjectFieldBasedCache("getName", cleanup = "subtreeChanged") // = value
//@InjectFieldBasedCache("getValue", cleanup = "subtreeChanged") // = text
@InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
@InjectFieldBasedCache("getColorType", cleanup = "subtreeChanged")
@InjectFieldBasedCache("getColorArgs", cleanup = "subtreeChanged")
class ParadoxScriptColorCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptParameterConditionParameterImpl", pluginId = "icu.windea.pls")
@InjectFieldBasedCache("getName", cleanup = "subtreeChanged")
//@InjectFieldBasedCache("getValue", cleanup = "subtreeChanged") // = name
@InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
class ParadoxScriptConditionParameterCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptParameterImpl", pluginId = "icu.windea.pls")
@InjectFieldBasedCache("getName", cleanup = "subtreeChanged")
//@InjectFieldBasedCache("getValue", cleanup = "subtreeChanged") // = name
@InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
class ParadoxScriptParameterCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.script.psi.impl.ParadoxScriptInlineMathParameterImpl", pluginId = "icu.windea.pls")
@InjectFieldBasedCache("getName", cleanup = "subtreeChanged")
//@InjectFieldBasedCache("getValue", cleanup = "subtreeChanged") // = name
@InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
class ParadoxScriptInlineMathParameterCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationLocaleImpl", pluginId = "icu.windea.pls")
@InjectFieldBasedCache("getName", cleanup = "subtreeChanged")
@InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
class ParadoxLocalisationLocaleCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyImpl", pluginId = "icu.windea.pls")
@InjectFieldBasedCache("getName", cleanup = "subtreeChanged")
@InjectFieldBasedCache("getValue", cleanup = "subtreeChanged")
@InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
class ParadoxLocalisationPropertyCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyKeyImpl", pluginId = "icu.windea.pls")
@InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
class ParadoxLocalisationPropertyKeyCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationStringImpl", pluginId = "icu.windea.pls")
@InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
class ParadoxLocalisationStringCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationPropertyReferenceImpl", pluginId = "icu.windea.pls")
@InjectFieldBasedCache("getName", cleanup = "subtreeChanged")
@InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
class ParadoxLocalisationPropertyReferenceCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationIconImpl", pluginId = "icu.windea.pls")
@InjectFieldBasedCache("getName", cleanup = "subtreeChanged")
@InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
class ParadoxLocalisationIconCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationCommandTextImpl", pluginId = "icu.windea.pls")
//@InjectFieldBasedCache("getName", cleanup = "subtreeChanged") // = text
//@InjectFieldBasedCache("getValue", cleanup = "subtreeChanged") // = text
@InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
class ParadoxLocalisationCommandTextCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationConceptImpl", pluginId = "icu.windea.pls")
@InjectFieldBasedCache("getName", cleanup = "subtreeChanged")
@InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
class ParadoxLocalisationConceptCodeInjector : CodeInjectorBase()

@InjectTarget("icu.windea.pls.localisation.psi.impl.ParadoxLocalisationConceptNameImpl", pluginId = "icu.windea.pls")
//@InjectFieldBasedCache("getName", cleanup = "subtreeChanged") // = text
//@InjectFieldBasedCache("getValue", cleanup = "subtreeChanged") // = text
@InjectFieldBasedCache("getText", cleanup = "subtreeChanged")
class ParadoxLocalisationConceptNameCodeInjector : CodeInjectorBase()
