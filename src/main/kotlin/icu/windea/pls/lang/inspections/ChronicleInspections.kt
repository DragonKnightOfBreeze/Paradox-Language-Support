package icu.windea.pls.lang.inspections

@Suppress("unused")
object ChronicleInspections {
    object Overrides {
        /** @see icu.windea.pls.lang.inspections.overrides.OverrideForFileInspection */
        const val OverrideForFile = "ParadoxOverrideForFile"
        /** @see icu.windea.pls.lang.inspections.overrides.OverrideForScriptedVariableInspection */
        const val OverrideForScriptedVariable = "ParadoxOverrideForScriptedVariable"
        /** @see icu.windea.pls.lang.inspections.overrides.OverrideForDefineVariableInspection */
        const val OverrideForDefinition = "ParadoxOverrideForDefinition"
        /** @see icu.windea.pls.lang.inspections.overrides.OverrideForDefineVariableInspection */
        const val OverrideForDefineVariable = "ParadoxOverrideForDefineVariable"
        /** @see icu.windea.pls.lang.inspections.overrides.IncorrectOverrideForScriptedVariableInspection */
        const val IncorrectOverrideForScriptedVariable = "ParadoxIncorrectOverrideForScriptedVariable"
        /** @see icu.windea.pls.lang.inspections.overrides.IncorrectOverrideForDefinitionInspection */
        const val IncorrectOverrideForDefinition = "ParadoxIncorrectOverrideForDefinition"
        /** @see icu.windea.pls.lang.inspections.overrides.IncorrectOverrideForDefineVariableInspection */
        const val IncorrectOverrideForDefineVariable = "ParadoxIncorrectOverrideForDefineVariable"
    }

    object Script {
        const val MissingImage = "ParadoxScriptMissingImage"
        const val MissingLocalisation = "ParadoxScriptMissingLocalisation"
        const val UnresolvedExpression = "ParadoxScriptUnresolvedExpression"
    }

    object Localisation {
        // Paradox Localisation/Common

        /** @see icu.windea.pls.lang.inspections.localisation.common.IncorrectFileEncodingInspection */
        const val IncorrectFileEncoding = "ParadoxLocalisationIncorrectFileEncoding"
        /** @see icu.windea.pls.lang.inspections.localisation.common.IncorrectFileNameInspection */
        const val IncorrectFileName = "ParadoxLocalisationIncorrectFileName"
        /** @see icu.windea.pls.lang.inspections.localisation.common.DuplicatePropertiesInspection */
        const val DuplicateProperties = "ParadoxLocalisationDuplicateProperties"
        /** @see icu.windea.pls.lang.inspections.localisation.common.MultipleLocalesInspection */
        const val MultipleLocales = "ParadoxLocalisationMultipleLocales"
        /** @see icu.windea.pls.lang.inspections.localisation.common.MissingLocalisationInspection */
        const val MissingLocalisation = "ParadoxLocalisationMissingLocalisation"
        /** @see icu.windea.pls.lang.inspections.localisation.common.UnsupportedLocaleInspection */
        const val UnsupportedLocale = "ParadoxLocalisationUnsupportedLocale"
        /** @see icu.windea.pls.lang.inspections.localisation.common.UnresolvedColorInspection */
        const val UnresolvedColor = "ParadoxLocalisationUnresolvedColor"
        /** @see icu.windea.pls.lang.inspections.localisation.common.UnresolvedScriptedVariableInspection */
        const val UnresolvedScriptedVariable = "ParadoxLocalisationUnresolvedScriptedVariable"
        /** @see icu.windea.pls.lang.inspections.localisation.common.UnresolvedIconInspection */
        const val UnresolvedIcon = "ParadoxLocalisationUnresolvedIcon"
        /** @see icu.windea.pls.lang.inspections.localisation.common.UnresolvedConceptInspection */
        const val UnresolvedConcept = "ParadoxLocalisationUnresolvedConcept"
        /** @see icu.windea.pls.lang.inspections.localisation.common.UnresolvedTextFormatInspection */
        const val UnresolvedTextFormat = "ParadoxLocalisationUnresolvedTextFormat"
        /** @see icu.windea.pls.lang.inspections.localisation.common.UnresolvedTextIconInspection */
        const val UnresolvedTextIcon = "ParadoxLocalisationUnresolvedTextIcon"
        /** @see icu.windea.pls.lang.inspections.localisation.common.IncorrectSyntaxInspection */
        const val IncorrectSyntax = "ParadoxLocalisationIncorrectSyntax"
        /** @see icu.windea.pls.lang.inspections.localisation.common.UnsupportedRecursionInspection */
        const val UnsupportedRecursion = "ParadoxLocalisationUnsupportedRecursion"

        // Paradox Localisation/Complex expressions

        /** @see icu.windea.pls.lang.inspections.localisation.complexExpression.IncorrectCommandExpressionInspection */
        const val IncorrectCommandExpression = "ParadoxLocalisationIncorrectCommandExpression"
        /** @see icu.windea.pls.lang.inspections.localisation.complexExpression.IncorrectDatabaseObjectExpressionInspection */
        const val IncorrectDatabaseObjectExpression = "ParadoxLocalisationIncorrectDatabaseObjectExpression"

        // Paradox Localisation/Scope issues

        /** @see icu.windea.pls.lang.inspections.localisation.scope.IncorrectScopeInspection */
        const val IncorrectScope = "ParadoxLocalisationIncorrectScope"
        /** @see icu.windea.pls.lang.inspections.localisation.scope.IncorrectScopeSwitchInspection */
        const val IncorrectScopeSwitch = "ParadoxLocalisationIncorrectScopeSwitch"
        /** @see icu.windea.pls.lang.inspections.localisation.scope.IncorrectScopeLinkChainInspection */
        const val IncorrectScopeLinkChain = "ParadoxLocalisationIncorrectScopeLinkChain"
    }

    object Csv {
        // Paradox CSV/Common

        /** @see  icu.windea.pls.lang.inspections.csv.common.IncorrectFileEncodingInspection */
        const val IncorrectFileEncoding = "ParadoxCsvIncorrectFileEncoding"
        /** @see  icu.windea.pls.lang.inspections.csv.common.UnmatchedFileInspection */
        const val UnmatchedFile = "ParadoxCsvUnmatchedFile"
        /** @see icu.windea.pls.lang.inspections.csv.common.IncorrectColumnNameInspection */
        const val IncorrectColumnName = "ParadoxCsvIncorrectColumnName"
        /** @see icu.windea.pls.lang.inspections.csv.common.IncorrectColumnSizeInspection */
        const val IncorrectColumnSize = "ParadoxCsvIncorrectColumnSize"

        // Paradox CSV/Expressions

        /** @see icu.windea.pls.lang.inspections.csv.expression.UnresolvedExpressionInspection */
        const val UnresolvedExpression = "ParadoxCsvUnresolvedExpression"
        /** @see icu.windea.pls.lang.inspections.csv.expression.IncorrectExpressionInspection */
        const val IncorrectExpression = "ParadoxCsvIncorrectExpression"
    }

    object Lints {
        /** @see icu.windea.pls.lang.inspections.lints.ChronicleTigerLintInspection */
        const val Tiger = "ChronicleTigerLint"
    }
}
