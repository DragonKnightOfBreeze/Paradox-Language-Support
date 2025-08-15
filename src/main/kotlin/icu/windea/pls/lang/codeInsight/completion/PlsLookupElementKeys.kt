package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.lookup.*
import icu.windea.pls.core.util.*
import javax.swing.*

object PlsLookupElementKeys : KeyRegistry()

var LookupElement.completionId: String? by createKey(PlsLookupElementKeys)
var LookupElement.extraLookupElements: List<LookupElement>? by createKey(PlsLookupElementKeys)

var LookupElement.priority: Double? by createKey(PlsLookupElementKeys)
var LookupElement.patchableIcon: Icon? by createKey(PlsLookupElementKeys)
var LookupElement.patchableTailText: String? by createKey(PlsLookupElementKeys)
var LookupElement.localizedNames: Set<String>? by createKey(PlsLookupElementKeys)
var LookupElement.scopeMatched: Boolean by createKey(PlsLookupElementKeys) { true }
var LookupElement.forceInsertCurlyBraces: Boolean by createKey(PlsLookupElementKeys) { false }
