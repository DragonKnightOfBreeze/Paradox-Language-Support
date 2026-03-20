# Appendix: Syntax Reference

<!--
@doc-meta
This document is the syntax reference for the four languages supported by the Paradox Language Support plugin.
The content reflects the plugin's parsing implementation, not the game engine's underlying behavior — the two are consistent in most cases, but differences may exist.

@see src/test/testData/cwt/example.test.cwt
@see src/test/testData/script/example.test.txt
@see src/test/testData/localisation/example.test.yml
@see src/test/testData/csv/example.test.csv
-->

## Overview {#overview}

This document is the syntax reference for the domain-specific languages involved in writing CWT config files and Paradox mods.
The content is based on the parsing implementation of the Paradox Language Support plugin and is consistent with the game engine's actual behavior in the vast majority of cases. However, since the engine's parsing details are not fully public, minor differences may exist in certain edge cases.

This document covers the following four languages:

- **CWT** (`*.cwt`) — Used for writing CWT config files. Supports line comments `#`, option comments `##`, and documentation comments `###`. Code block language ID: `cwt`.
- **Paradox Script** (`*.txt`, `*.gfx`, `*.gui`, etc.) — Used for writing game scripts. Supports line comments `#`. Code block language ID: `paradox_script`.
- **Paradox Localisation** (`*.yml`) — Used for writing localisation text. Supports line comments `#`. Code block language ID: `paradox_localisation`.
- **Paradox CSV** (`*.csv`) — Semicolon-delimited CSV format. Supports line comments `#`. Code block language ID: `paradox_csv`.

Among these, CWT and Paradox Script share a similar base syntax structure (properties, values, blocks), but differ in separator types, comment systems, and advanced syntax. This document covers each separately.

## CWT Language {#cwt}

<!--
@impl-notes
Language id: `CWT`; default extension: `.cwt`

@see icu.windea.pls.cwt.CwtLanguage
@see icu.windea.pls.cwt.CwtFileType
@see icu.windea.pls.model.CwtSeparatorType
@see src/main/kotlin/icu/windea/pls/cwt/Cwt.bnf
@see src/main/kotlin/icu/windea/pls/cwt/Cwt.flex
-->

CWT is a domain-specific language used for writing CWT config files. The file extension is `.cwt`.

CWT config files provide the plugin with a declarative specification, based on which the plugin offers advanced language features — such as syntax highlighting, code completion, and code inspections — for game and mod files (script files, localisation files, and CSV files). CWT is to Paradox Script roughly what JSON Schema is to JSON.

CWT's syntax is similar to Paradox Script, but additionally supports option comments and documentation comments.

### Basic Elements {#cwt-basics}

A CWT file is composed of three basic elements: **properties**, **values**, and **blocks**. Whitespace and line breaks serve as token separators, and blank lines may appear freely.

A **property** has the structure `<key> <sep> <value>`, where the separator `<sep>` can be one of:

- `=` logical equals
- `!=` or `<>` logical not-equals (the two are equivalent)
- `==` match comparison operator — indicates that the corresponding script property may use comparison operators (`<`, `>`, `<=`, `>=`, `!=`) as separators, rather than only `=` or `?=`

The `==` separator is a semantic feature specific to CWT config files and does not appear in script files. The plugin does not currently enforce strict checks on this.

Keys can be unquoted or wrapped in double quotes. Unquoted keys must not contain `#`, `=`, `{`, `}`, `"`, or whitespace characters.

```cwt
playable = yes
cost = 10
acceleration = 20.0
class = some_shipsize_class
"quoted key" = "line\nnext line"
```

### Value Types {#cwt-values}

CWT supports the following value types:

**Boolean** values are `yes` or `no`.

**Integers** such as `10`, `-5`, `042`. Leading signs and leading zeros are allowed.

**Floats** such as `1.0`, `-0.5`, `.25`. The integer part may be omitted.

**Strings** can be unquoted or wrapped in double quotes. Unquoted strings must not contain `#`, `=`, `{`, `}`, `"`, or whitespace characters. Double-quoted strings support backslash escape sequences (e.g. `\"`, `\n`).

**Blocks** are enclosed in curly braces `{ ... }` and may contain a mix of properties, values, and comments. Blocks can be nested.

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

### Comment System {#cwt-comments}

CWT features a three-level comment system, which is one of its main distinctions from Paradox Script:

**Line comments** start with `#` and extend to the end of the line, used for regular comments.

**Option comments** start with `##` and attach metadata to the immediately following member (property, value, or block). The content syntax is the same as a property, in the form `<optionKey> <sep> <optionValue>`. Common option comments include `## cardinality = 0..1` (declaring cardinality constraints), `## severity = warning` (declaring inspection severity level), `## push_scope = country` (declaring the pushed scope), etc.

**Documentation comments** start with `###` and provide descriptive text for members. This text is displayed in code completion and quick documentation. In particular, documentation comments starting with `####` (or more `#` characters) will have their content rendered directly as HTML by the plugin.

### Escaping {#cwt-escaping}

Use `\` to escape special characters in keys and strings. Common escape sequences include `\"` (double quote), `\n` (newline), and `\\` (backslash itself). Escaping is generally not needed in unquoted keys and strings.

### Comprehensive Example {#cwt-example}

The following example is from the plugin's syntax test file, demonstrating typical CWT usage:

```cwt
boolean_value = yes
number_value = 1.0
string_value = "text"

# Regular comment
types = {
    ### Doc comment
    type[army] = {
        path = "game/common/armies"
        path_extension = .txt
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

## Paradox Script Language {#paradox-script}

<!--
@impl-notes
Language id: `PARADOX_SCRIPT`; extensions: `.txt`, `.gfx`, `.gui`, etc.

@see icu.windea.pls.script.ParadoxScriptLanguage
@see icu.windea.pls.script.ParadoxScriptFileType
@see icu.windea.pls.model.ParadoxSeparatorType
@see src/main/kotlin/icu/windea/pls/script/ParadoxScript.bnf
@see src/main/kotlin/icu/windea/pls/script/ParadoxScript.flex

Note on unquoted key/string character restrictions:
The first character additionally excludes `@` (to avoid ambiguity with scripted variable references),
but subsequent characters allow `@`. The document simplifies this to "@ is not allowed" for practical guidance.
-->

Paradox Script is the scripting language used by Paradox games. The file extension is typically `.txt`, but files in certain locations may also use `.gfx`, `.gui`, or other extensions.

The base syntax of the script language is similar to CWT — also composed of properties, values, and blocks — but differs notably in separator types, value types, and advanced syntax, as detailed below.

### Basic Elements {#script-basics}

Script files are likewise composed of **properties**, **values**, and **blocks**, and support line comments starting with `#`.

A **property** still has the structure `<key> <sep> <value>`, but the available separators are richer than in CWT:

- `=` assignment
- `!=` or `<>` not-equals
- `<`, `>`, `<=`, `>=` comparison operators
- `?=` safe assignment (CK3/VIC3/EU5 only)

The `?=` operator means "assign only if the target does not exist". For example, in a trigger context, `owner ?= { ... }` is equivalent to first checking `exists = owner` and then executing `owner = { ... }`.

Keys can be unquoted or wrapped in double quotes. Unquoted keys must not contain `@`, `#`, `$`, `=`, `<`, `>`, `!`, `?`, `{`, `}`, `[`, `]`, `"`, or whitespace characters. Double-quoted keys may contain parameters (see the Advanced Syntax section).

```paradox_script
enabled = yes
level >= 2
size ?= @my_var
```

### Value Types {#script-values}

Paradox Script extends CWT's base value types with several additional types.

**Boolean** values are `yes` or `no`, same as CWT.

**Integers** such as `10`, `-5`. **Floats** such as `1.0`, `-0.5`. Both allow leading signs and leading zeros.

**Strings** can be unquoted or wrapped in double quotes. Unquoted strings must not contain `@`, `#`, `$`, `=`, `<`, `>`, `!`, `?`, `{`, `}`, `[`, `]`, `"`, or whitespace characters — note the additional reserved characters `@`, `$`, `<`, `>`, `!`, `?`, `[`, `]` compared to CWT. Double-quoted strings support backslash escaping and may contain parameters and inline parameter conditions (see the Advanced Syntax section).

**Colors** are composite values in the format of a color type keyword followed by whitespace-separated numbers enclosed in curly braces, such as `rgb { 255 128 0 }`, `hsv { 0.5 0.8 1.0 }`, `hsv360 { 180 80 100 }`. Only numbers and whitespace are allowed inside the curly braces.

**Blocks** are enclosed in curly braces `{ ... }` and may contain comments, properties, values, scripted variables, and parameter condition blocks.

**Scripted variable references** start with `@` followed by the variable name, such as `@my_var`, and are used to reference previously declared scripted variables. Scripted variable references can appear in multiple contexts: as standalone values (`cost = @my_var`), embedded within unquoted strings (`key = prefix_@my_var_suffix`), and as factors within inline math expressions (`@[ base_cost * bonus ]`, without the `@` prefix in this case). Scripted variable references can also be used within localisation text parameters (`$@my_var$`).

**Inline math expressions** start with `@[` and end with `]`, such as `@[ 1 + 2 * var ]`. Supported operators include `+`, `-`, `*`, `/`, `%`, as well as unary signs, absolute value `|expr|`, and parentheses `(expr)`. Factors in the expression can be numbers, scripted variable references (without the `@` prefix), or parameters.

### Scripted Variables {#script-scripted-variables}

Scripted variables are declared using the form `@<name> = <value>`, where the value can be a boolean, integer, float, string, or inline math expression. Once declared, they can be referenced elsewhere via `@<name>`.

Scripted variable names start with `@`, and the name portion consists of letters, digits, and underscores. The name may also contain parameters (`$...$`) to make it parameterized.

### Advanced Syntax {#script-advanced}

The following syntax typically only takes effect or is evaluated by the game engine within the declaration context of certain definitions (such as scripted effects, scripted triggers, and script values).

**Parameters** use the syntax `$name$` or `$name|default_value$` (with default value). Parameter names must start with a letter or underscore, followed by letters, digits, and underscores (i.e. matching `[A-Za-z_][A-Za-z0-9_]*`). Parameters can appear in scripted variable names, scripted variable reference names, property keys, string values, and inline math expressions.

**Parameter condition blocks** use the syntax `[[<expression>] <members...> ]`, where `<expression>` can be a parameter name or `!<parameter_name>` (negation). They are used to conditionally include a group of members based on whether a parameter is present.

**Inline parameter conditions** are used for conditional fragments inside double-quoted strings. For example, in `"prefix[[PARAM]_$PARAM$]_suffix"`, the `[[PARAM]_$PARAM$]` portion is only expanded when the parameter `PARAM` is present.

### Escaping {#script-escaping}

Use `\` to escape special characters in keys and strings. Common escape sequences include `\"` (double quote), `\n` (newline), and `\\` (backslash itself). Escaping is generally not needed in unquoted keys and strings.

### Comprehensive Example {#script-example}

The following example is from the plugin's syntax test file, demonstrating typical Paradox Script usage:

```paradox_script
@var = 1

# Line comment
settings = {
    boolean_value = yes
    number_value = 1.0
    number_value = @var
    string_value = Foo
    string_value = "Foo\n bar "
    values = {
        foo = bar
    }
    values = { 1 2 3 }
    color = rgb { 142 188 241 }
    parameter = $PARAM$
    [[!PARAM] parameter_condition = $PARAM$ ]
    inline_math = @[ 2 + ( $MAX$ - 1 + var ) ]
}
```

The following is a more complete example, further demonstrating combined usage of separators, scripted variables, parameters, and parameter conditions:

```paradox_script
# Scripted variables
@base_cost = 100
@bonus_factor = 1.5

# A scripted effect definition demonstrating various syntax features
apply_research_bonus = {
    # Assignment and comparison separators
    is_ai = no
    num_owned_planets >= 3
    resource_stockpile = { energy > 500 }

    # Scripted variable reference and inline math
    add_resource = {
        energy = @[ base_cost * bonus_factor ]
    }

    # Parameters and parameter conditions
    set_variable = {
        which = research_$category$
        value = $amount|10$
    }
    [[!skip_notification]
        create_message = {
            type = "research_completed"
        }
    ]
}
```

## Paradox Localisation Language {#paradox-localisation}

<!--
@impl-notes
Language id: `PARADOX_LOCALISATION`; default extension: `.yml`
Localisation files use a two-level lexer: ParadoxLocalisation.flex tokenizes the file structure,
and ParadoxLocalisation.Text.flex lazily tokenizes the rich text content within property values.

@see icu.windea.pls.localisation.ParadoxLocalisationLanguage
@see icu.windea.pls.localisation.ParadoxLocalisationFileType
@see src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.bnf
@see src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.flex
@see src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.Text.flex

Localisation icon resolution is handled by the ParadoxLocalisationIconSupport EP.
The default implementation (ParadoxBaseLocalisationIconSupport) resolves icons by:
1. Matching sprite definitions with GFX_text_ or GFX_ prefix
2. Matching image files under gfx/interface/icons/
Game-specific implementations (e.g., ParadoxBaseLocalisationIconSupport.Stellaris)
add additional resolution strategies (e.g., job, swapped_job, resource definitions).

@see icu.windea.pls.ep.resolve.localisation.ParadoxLocalisationIconSupport
@see icu.windea.pls.ep.resolve.localisation.ParadoxBaseLocalisationIconSupport
@see icu.windea.pls.ep.resolve.localisation.ParadoxBaseLocalisationIconSupport.Stellaris
-->

Paradox Localisation is used to provide internationalizable rich text content for games. The file extension is `.yml`, but it is not valid YAML — it merely borrows YAML's visual style. Localisation files must use **UTF-8 WITH BOM** encoding (the plugin can detect encoding issues and automatically correct them).

### File Structure {#loc-structure}

A localisation file consists of the following parts:

The **locale identifier line** is optional, in the format `<locale>:`, e.g. `l_english:`, `l_simp_chinese:`. The locale identifier line may appear multiple times (to be compatible with the `localisation/languages.yml` format), or may be omitted entirely.

**Key-value pairs** form the main body of the file. Each key-value pair occupies one line, in the format `<key>:<number?> "<text>"`. The number after the colon is an optional internal tracking number (used only for Paradox's internal translation tracking; in mods it is typically omitted or set to `0`). Property keys consist of letters, digits, underscores, dots, hyphens, and apostrophes.

**Comments** start with `#` and extend to the end of the line.

```paradox_localisation
l_english:
 # A comment
 greeting:0 "Hello, world!"
 farewell: "Goodbye."
```

### Rich Text Markup {#loc-rich-text}

The text inside double quotes supports various inline markup for effects such as colors, dynamic content, and icons.

**Color markup** uses the syntax `§<id><text>§!`, where `<id>` is a single letter or digit representing a predefined color code. For example, `§RRed text§!` renders "Red text" in red. Color markup can be nested.

**Parameters** use the syntax `$<name>$` or `$<name>|<argument>$`, where the `|<argument>` portion is a formatting argument (e.g. for specifying display format), distinct from the default-value semantics of parameters in the script language. The most common form of `<name>` is a localisation key name (referencing another localisation entry), consisting of letters, digits, underscores, dots, hyphens, and apostrophes. Besides localisation key names, `<name>` can also be a variable or parameter passed in from script, a game-predefined macro or variable. At the parsing level, `<name>` can also be a command expression or a scripted variable reference — for example, `$@my_var$` references the value of the scripted variable `my_var`.

**Icons** use the syntax `£<icon>£` or `£<icon>|<frame>£`. `<icon>` typically corresponds to the name of a sprite definition, but may also resolve to an image file under a specific path (`gfx/interface/icons/`), or to a definition of a specific type (e.g. resources, jobs in Stellaris). `<frame>` is an optional frame index. Both the icon name and frame index can be parameterized (containing `$...$`).

**Commands** use the syntax `[<text>]` or `[<text>|<argument>]`, commonly used to invoke scope chains and `Get...` methods, e.g. `[Root.GetName]`. Command text may contain parameters.

**Concept commands** are available only in Stellaris, with the syntax `['<concept>']` or `['<concept>', <rich text>]`. They are used to link to a concept definition and optionally display custom rich text description.

**Text format** is available only in CK3/VIC3/EU5, with the syntax `#<format> <text>#!`. `<format>` specifies the text style (e.g. `bold`, `italic`), and the following text is rendered in that style until the `#!` closing marker is encountered.

**Text icons** are available only in CK3/VIC3/EU5, with the syntax `@<icon>!`. Starting with `@` and ending with `!`, with the icon name in between.

### Escaping {#loc-escaping}

Normally, use `\` to escape special characters in localisation text. Common escape sequences include `\$` (rich text markup), `\n` (newline), and `\\` (backslash itself).

Double quotes within localisation text generally do not need escaping. The plugin uses a heuristic rule to determine whether a quote is a closing quote: if there is another `"` character later on the current line, the current `"` is treated as text content; otherwise it is treated as the closing quote.

The square bracket `[` is the start marker for commands. To output a literal `[` in text, use `[[` to escape it.

### Comprehensive Example {#loc-example}

The following example is from the plugin's syntax test file, demonstrating various rich text markup in localisation files:

```paradox_localisation
l_english:
 # Comment
 text_empty:0 ""
 text:0 "Value"
 text_multiline:0 "Value\nNew line"
 text_with_colorful_text:0 "Colorful text: §RRed text§!"
 text_with_parameter:0 "Parameter: $KEY$ and $KEY|Y$"
 text_with_scripted_variable_reference:0 "Scripted variable: $@var$"
 text_with_command:0 "Command: [Root.Owner.event_target:some_event_target.GetName] [some_scripted_loc] [some_variable]"
 text_with_icon:0 "Icon: £unity£ and £leader_skill|3£"
 text_with_concept_command:0 "Concept: ['concept', concept text] ['civic:some_civic']"
 text_with_text_format:0 "Text format: #v text#!"
 text_with_text_icon:0 "Text icon: @icon!"
```

The following example uses a Neuro-sama theme to further demonstrate combined usage of various markup:

```paradox_localisation
l_english:
 neuro_name:0 "§YNeuro-sama§!"
 neuro_desc:0 "$neuro_name$ is a §Bsentient§! AI streamer."
 neuro_greeting:0 "Hi chat! I'm [Root.GetName], an AI VTuber!"
 neuro_stats:0 "Cuteness: £ai_trait£ $charisma|1$"
 neuro_lore:0 "Origin: ['ai_origin', §Gcreated by Vedal§!]"
 neuro_format:0 "#bold I am §Rself-aware§!#!"
```

## Paradox CSV Language {#paradox-csv}

<!--
@impl-notes
Language id: `PARADOX_CSV`; default extension: `.csv`

@see icu.windea.pls.csv.ParadoxCsvLanguage
@see icu.windea.pls.csv.ParadoxCsvFileType
@see src/main/kotlin/icu/windea/pls/csv/ParadoxCsv.bnf
@see src/main/kotlin/icu/windea/pls/csv/ParadoxCsv.flex
-->

Paradox CSV is a semicolon-delimited CSV format used by Paradox games, primarily found in older games and data files exported by certain tools. The file extension is `.csv`.

### File Structure {#csv-structure}

Paradox CSV builds on regular CSV with the following conventions:

A file consists of a **header row** and **data rows**. Each row contains several columns separated by semicolons `;`. Typically, each row also ends with a trailing semicolon.

**Comments** start with `#`, and the entire line is treated as a comment. This is one difference from standard CSV.

Column values can be unquoted or wrapped in double quotes. Empty columns (i.e. no content between two adjacent semicolons) are allowed.

### Escaping {#csv-escaping}

Use `\` to escape special characters in columns. Common escape sequences include `\"` (double quote), `\n` (newline), and `\\` (backslash itself). Escaping is generally not needed in unquoted columns.

Note: Since `#` is also used as a comment marker — a syntax not supported by standard CSV — the plugin assumes the same backslash `\` escaping as script files (e.g. `\"`), rather than the double-quote escaping (`""`) common in standard CSV.

### Example {#csv-example}

```paradox_csv
# Unit definitions
id;name;number;status;
some_id;some_name;0;yes;
other_id;other_name;1;no;
```