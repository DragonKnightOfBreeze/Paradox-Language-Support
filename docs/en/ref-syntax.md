# Appendix: Syntax Reference

<!-- TODO 人工改进与润色 -->

## Position & Vision {#vision}

This document aims to be the authoritative guide and quick reference for the syntax of the Domain-Specific Languages (DSLs) used when writing CWT config files and Paradox mods.
It is in maintaining principles of clarity, accuracy, and practicality, ensuring consistency with actual usage and toolchain behavior, 
and will be continuously polished and improved alongside the evolution of the ecosystem and versions.

## Overview {#overview}

This document covers the syntax specifications for the following languages:

- **CWT** (`*.cwt`)
  - Comments: `#`, `##` (option comment), `###` (doc comment)
  - Code fence language ID: `cwt`
- **Paradox Script**
  - Comments: `#`
  - Code fence language ID: `paradox_script`
- **Paradox Localisation** (Localization `.yml`)
  - Comments: `#`
  - Code fence language ID: `paradox_localisation`
- **Paradox CSV** (Semicolon-delimited CSV, `*.csv`)
  - Comments: `#`
  - Code fence language ID: `paradox_csv`

## CWT Language {#cwt}

<!--
@impl-notes
Language id: `CWT`; default extension: `.cwt`
This section describes surface syntax and lexer tokens; details follow the plugin grammar.

@see icu.windea.pls.cwt.CwtLanguage
@see icu.windea.pls.cwt.CwtFileType
@see src/main/kotlin/icu/windea/pls/cwt/Cwt.bnf
@see src/main/kotlin/icu/windea/pls/cwt/Cwt.flex
@see src/main/kotlin/icu/windea/pls/cwt/Cwt.OptionDocument.flex
-->

This chapter describes the syntax of the CWT language.

The CWT language is a Domain-Specific Language used for writing CWT configs.
CWT language files use the `.cwt` extension. Its syntax is similar to Paradox Script but additionally supports *option comments* and *documentation comments*.

CWT configs are used to provide advanced language features for the CWT language itself and various Paradox languages, including but not limited to syntax highlighting, code inspection, code completion, etc.

Basic Concepts:

- **Member Types**: Property, Value, Block.
- **Separators**: `=`, `==` mean equals; `!=`, `<>` mean not equals.
- **Whitespace/Newlines**: Whitespace and newlines are used to separate tokens; blank lines are allowed.

Property:

- Structure: `<key> <sep> <value>`, where `<sep>` is `=`/`==`/`!=`/`<>`.
- Key: Unquoted or double-quoted; unquoted keys should not contain `# = { }` or whitespace.
- Example:

```cwt
playable = yes
cost = 10
acceleration = 20.0
class = some_shipsize_class
"text" = "line\nnext line"
```

Value:

- **Boolean**: `yes`/`no`.
- **Integer**: `10`, `-5` (leading minus and leading zeros are permitted).
- **Float**: `1.0`, `-0.5`.
- **String**:
  - Unquoted: must not contain `# = { }` or whitespace.
  - Double-quoted: supports escapes like `\"`.
- **Block**: see below.

Block:

- Enclosed by braces: `{ ... }`.
- Content may mix: properties, values, and option comments (see below). Both inline and standalone comments are allowed.
- Example:

```cwt
ship_size = {
    ### The base cost of this ship_size
    ## cardinality = 0..1
    cost = int

    modifier = {
        alias_name[modifier] = alias_match_left[modifier]
    }
}
```

Comments and documentation:

- **Line Comment**: Starts with `#`, the entire line is treated as a comment.
- **Option Comment**: Starts with `##`; used to declare metadata for the member (property/block/value) immediately following it.
  - Syntax is the same as a property: `<optionKey> <sep> <optionValue>`.
  - Common examples: `## cardinality = 0..1`, `## severity = warning`, `## push_scope = country`.
- **Documentation Comment**: Starts with `###`; used to provide descriptive text for a member (shown in completion/documentation).

Grammar highlights and example:

```cwt
boolean_value = yes
number_value = 1.0
string_value = "text"

# Regular comment
types = {
	### Doc comment
	type[army] = {
        path = "game/common/armies"
        file_extension = .txt
		subtype[has_species] = {
			## cardinality = 0..0
			has_species = no
		}
        localisation = {
            ## required
            ## primary
            name = "$"
            plural = "$_plural"
            desc = "$_desc"
        }
        images = {
            ## primary
            icon = icon # <sprite>
        }
	}
}
```

Notes:

- An option comment only affects the single member immediately following it (or the content within that member, depending on implementation).
- Avoid including whitespace and reserved symbols in unquoted keys and strings; use double quotes for complex content.
- Both equals and not-equals symbols can be used for options and properties; choose based on semantics.

## Paradox Script Language {#paradox-script}

<!--
@impl-notes
Language id: `PARADOX_SCRIPT`; extensions: `.txt`, `.gfx`, `.gui`, etc.
This section describes surface syntax and lexer tokens; details follow the plugin grammar.

@see icu.windea.pls.script.ParadoxScriptLanguage
@see icu.windea.pls.script.ParadoxScriptFileType
@see src/main/kotlin/icu/windea/pls/script/ParadoxScript.bnf
@see src/main/kotlin/icu/windea/pls/script/ParadoxScript.flex
-->

This chapter describes the basic syntax of the Paradox Script language.

Paradox Script is a Domain-Specific Language used for writing game scripts.
Its file extension is typically `.txt`. Files in specific locations may use other extensions, such as `.gfx` or `.gui`.

Basic Concepts:

- **Member Types**: Property, Value, Block.
- **Separators (Comparison/Assignment)**: `=`, `!=`, `<`, `>`, `<=`, `>=`, `?=`.
- **Comments**: Single-line comments starting with `#`.

Property:

- Structure: `<key> <sep> <value>`, where `<sep>` is one of the separators listed above.
- Key: Unquoted or double-quoted. Keys can be parameterized (see below).
- Example: `enabled = yes`, `level >= 2`, `size ?= @var`.

Value:

- **Boolean**: `yes`/`no`.
- **Integer/Float**: e.g., `10`, `-5`, `1.25`.
- **String**: Unquoted or double-quoted; double-quoted strings can embed parameters and inline parameter conditions.
- **Color**: `rgb {...}`, `hsv {...}`, `hsv360 {...}`.
- **Block**: `{ ... }`, can contain comments, properties, values, scripted variables, etc., inside.
- **Scripted Variable Reference**: `@name`.
- **Inline Math Expression**: `@[ <expr> ]`, supports `+ - * / %`, unary plus/minus, absolute value `|x|`, and parentheses.

Scripted Variable:

- Declaration: `@<name> = <value>`, where `<value>` can be Boolean/Integer/Float/String/Inline math expression.
- Reference: `@<name>`.

Parameter:

- Syntax: `$name$` or `$name|argument$`.
- Location: Can be used in scripted variable names, scripted variable reference names, keys, strings, inline math expressions.

Parameter Condition:

- Syntax (outer): `[[!<expression>] <members...> ]`.
- Description: Used to define conditional members within a declaration context (only valid in specific definitions).

Inline Parameter Condition:

- Used for conditional fragments inside strings, formatted like: `"a[[cond]b]c"`.

Example:

```paradox_script
# comment
@my_var = 42

effect = {
    enabled = yes
    level >= 2
    size ?= @my_var
    color = rgb { 34, 136, 255 }

    name = "Hello $who|leader$!"
    "tooltip" = "line\nnext line"

    modifier = {
        add = 1
        [[!PARAM]
            factor = 10
        ]
    }

    result = @[ 1 + 2 * $PARAM$ / var ]
}
```

> [!warning]
> Parameters, parameter conditions, inline math, etc., are advanced syntax and are typically only evaluated by the engine or effective within specific definitions (like scripted effects/triggers).

## Paradox Localisation Language {#paradox-localisation}

<!--
@impl-notes
Language id: `PARADOX_LOCALISATION`; default extension: `.yml`
This section describes surface syntax and lexer tokens; details follow the plugin grammar.

@see icu.windea.pls.localisation.ParadoxLocalisationLanguage
@see icu.windea.pls.localisation.ParadoxLocalisationFileType
@see src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.bnf
@see src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.flex
@see src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.Text.flex
-->

This chapter describes the basic syntax of the Paradox Localisation language.

The Paradox Localisation language is a Domain-Specific Language used to provide internationalize rich text with dynamic content for the game.
Its file extension is `.yml`, but it is not actually valid YAML and must use **UTF-8 WITH BOM** encoding.

> [!tip]
> PLS can detect file encoding issues in localisation files and supports automatically fixing the encoding.

File Structure:

- Optional **Language Identifier** line: e.g., `l_english:`, `l_simp_chinese:` (multiple can exist for compatibility with `localisation/languages.yml`).
- Multiple **Key-Value Pairs**: `<key>:<number?> "<text>"`, where `<number>` is an optional internal tracking number.
- Comments: Single-line comments starting with `#`.

Markup available inside the text (`"<text>"`):

- **Color**: `§X ... §!` (`X` is a single-character ID).
- **Parameter**: `$name$` or `$name|argument$`. `name` can be a localisation key, command, or scripted variable reference (e.g., `$@var$` is equivalent at the parsing level).
- **Command**: `[text|argument]`, where `text` can be parameterized; commonly used for `Get...`/context calls.
- **Icon**: `£icon|frame£` (`|frame` can be omitted), embeds a GFX icon when rendered.
- **Concept Command (Stellaris)**: `['concept' <rich text>]`, used to link concepts and display descriptive text.
- **Text Format (CK3/Vic3)**: `#format ... #!`, used to style text blocks; and **Text Icons**: `@icon!` (starts with `@`, ends with `!`).

Example:

```paradox_localisation
l_english:
 # comment
 key:0 "line\nnext line"
 another_key:0 "§Y$target$§! produces £unity£"
 command_key:0 "Name: [Root.GetName]"
 concept_command_key:0 "['pop_growth', §G+10%§!]"
```

Notes:

- The number (tracking number) after the colon can be omitted.
- Double quotes inside the text generally do not need escaping, but avoid unpaired quotes.

> [!warning]
> `#format`, `@icon!`, etc., are advanced markup supported by specific games; they are only valid in those games. `['concept' ...]` is only supported in Stellaris.

## Paradox CSV Language {#paradox-csv}

<!--
@impl-notes
Language id: `PARADOX_CSV`; default extension: `.csv`
This section describes surface syntax and lexer tokens; details follow the plugin grammar.

@see icu.windea.pls.csv.ParadoxCsvLanguage
@see icu.windea.pls.csv.ParadoxCsvFileType
@see src/main/kotlin/icu/windea/pls/csv/ParadoxCsv.bnf
@see src/main/kotlin/icu/windea/pls/csv/ParadoxCsv.flex
-->

This chapter describes the syntax of the Paradox CSV language.
Paradox CSV language files use the `.csv` extension. Based on standard CSV, its conventions:

- **Column Separator**: Semicolon `;` (common in early/tool-exported CSVs).
- **Comments**: Whole-line comments starting with `#`.
- **Strings**: Enclosed in double quotes; internal double quotes are represented by `""` (standard CSV rule).

Example:

```paradox_csv
# comment
key;col1;col2
id1;"text with ; semicolon";42
id2;plain;"line\nnext line"
```