package com.windea.plugin.idea.paradox

import com.intellij.openapi.util.*

//Strings

const val paradoxLocalisationName = "Paradox Localisation"
const val paradoxLocalisationNamePc = "ParadoxLocalisation"
const val paradoxLocalisationNameSsc = "PARADOX_LOCALISATION"
const val paradoxLocalisationLanguageName = "$paradoxLocalisationName Language"
const val paradoxLocalisationFileTypeName = "$paradoxLocalisationName File"
const val paradoxLocalisationFileTypeDescription = "$paradoxLocalisationName Language"
const val paradoxLocalisationExtension = "yml"
val paradoxLocalisationSampleText = "sampleText/ParadoxLocalisation.txt".toClassPathResource()!!.readText()

const val paradoxScriptName = "Paradox Script"
const val paradoxScriptNamePc = "ParadoxScript"
const val paradoxScriptNameSsc = "PARADOX_SCRIPT"
const val paradoxScriptLanguageName = "$paradoxScriptName Language"
const val paradoxScriptFileTypeName = "$paradoxScriptName File"
const val paradoxScriptFileTypeDescription = "$paradoxScriptName Language"
const val paradoxScriptExtension = "txt"
val paradoxScriptSampleText = "sampleText/ParadoxScript.txt".toClassPathResource()!!.readText()

const val commentFolder = "#..."
const val blockFolder = "{...}"
const val defaultFolder = "<folder>"
const val anonymousName = "<anonymous>"

val utf8Bom = byteArrayOf(0xef.toByte(), 0xbb.toByte(), 0xbf.toByte())

val booleanValues = arrayOf("yes", "no")

const val paradoxBundleName = "messages.ParadoxBundle"

val localisationFileExtensions = arrayOf("yml")
val scriptFileExtensions = arrayOf("txt", "mod", "gfx", "gui", "asset", "dlc","settings")
val scriptRuleFileExtensions = arrayOf("cw","cwt") //兼容cwtools

const val descriptorFileName = "descriptor.mod"
val exeFileNames = arrayOf("stellaris.exe")
val ignoredScriptFileNameRegex = """(readme|changelog|license|credits).*\.txt""".toRegex(RegexOption.IGNORE_CASE)

const val rulesPath = "rules"
const val corePath = "core"
const val ruleFileExtension = "yml"

//Pattern Prefixes

const val primitivePrefix="$"
const val primitivePrefixLength = primitivePrefix.length
const val aliasPrefix="alias:"
const val aliasPrefixLength=aliasPrefix.length
const val typePrefix="type:"
const val typePrefixLength = typePrefix.length
const val enumPrefix="enum:"
const val enumPrefixLength=enumPrefix.length
const val eventTargetPrefix = "event_target:"
const val eventTargetPrefixLength = eventTargetPrefix.length

//Icons

val localisationFileIcon = IconLoader.findIcon("/icons/paradoxLocalisationFile.svg")!!
val localisationLocaleIcon = IconLoader.findIcon("/icons/paradoxLocalisationLocale.svg")!!
val localisationPropertyIcon = IconLoader.findIcon("/icons/paradoxLocalisationProperty.svg")!!
val localisationIconIcon = IconLoader.findIcon("/icons/paradoxLocalisationIcon.svg")!!
val localisationSequentialNumberIcon = IconLoader.findIcon("/icons/paradoxLocalisationSequentialNumber.svg")!!
val localisationCommandScopeIcon = IconLoader.findIcon("/icons/paradoxLocalisationCommandScope.svg")!!
val localisationCommandFieldIcon = IconLoader.findIcon("/icons/paradoxLocalisationCommandField.svg")!!

val scriptFileIcon = IconLoader.findIcon("/icons/paradoxScriptFile.svg")!!
val scriptVariableIcon = IconLoader.findIcon("/icons/paradoxScriptVariable.svg")!!
val scriptPropertyIcon = IconLoader.findIcon("/icons/paradoxScriptProperty.svg")!!
val scriptValueIcon = IconLoader.findIcon("/icons/paradoxScriptValue.svg")!!

val definitionIcon = IconLoader.findIcon("/icons/paradoxDefinition.svg")!!
val definitionLocalisationIcon = IconLoader.findIcon("/icons/paradoxDefinitionLocalisation.svg")!!
val localisationIcon = IconLoader.findIcon("/icons/paradoxLocalisation.svg")!!
val stringScriptPropertyIcon =IconLoader.findIcon("/icons/paradoxStringScriptProperty.svg")!!
val stringLocalisationPropertyIcon = IconLoader.findIcon("/icons/paradoxStringLocalisationProperty.svg")!!
val scriptLocalisationIcon = IconLoader.findIcon("/icons/paradoxScriptLocalisation.svg")

val definitionGutterIcon = definitionIcon.resize(12)
val definitionLocalisationGutterIcon = definitionLocalisationIcon.resize(12)
val localisationGutterIcon = localisationIcon.resize(12)
val stringScriptPropertyGutterIcon =stringScriptPropertyIcon.resize(12)
val stringLocalisationPropertyGutterIcon = stringLocalisationPropertyIcon.resize(12)
//val eventIdGutterIcon = IconUtil.toSize(AllIcons.Nodes.Protected, 12, 12)
//val gfxKeyGutterIcon = IconUtil.toSize(AllIcons.Nodes.Related, 12, 12)
//val assetKeyGutterIcon = IconUtil.toSize(AllIcons.Nodes.Related, 12, 12)

val stellarisIcon = IconLoader.findIcon("icons/stellaris.png")!!
