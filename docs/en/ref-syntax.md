# Appendix: Syntax Reference

<!-- TODO 人工改进与润色 -->

## Positioning & Vision {#vision}

This chapter aims to be an authoritative, practical, and concise reference for Paradox modding syntaxes. We strive to align with real-world usage and toolchain behaviour, and will continue refining it as the ecosystem evolves.

## Overview {#overview}

This chapter covers the following syntaxes (examples use Prism code fences):

- **CWT config file** (`*.cwt`)
  - Comments: `#`, `##` (option comment), `###` (doc comment)
  - Code fence language id: `cwt`
- **Paradox Script**
  - Comments: `#`
  - Code fence language id: `paradox_script`
- **Paradox Localisation** (`.yml`)
  - Comments: `#`
  - Code fence language id: `paradox_localisation`
- **Paradox CSV** (semicolon-separated CSV)
  - Comments: `#`
  - Code fence language id: `paradox_csv`

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

This section defines the syntax of CWT config files used to author and organize CWT configuration content. The surface form resembles Paradox Script with additional capabilities such as option comments.

Basics:

- **Member types**: property, value, block.
- **Separators**: `=`/`==` mean equal; `!=`/`<>` mean not equal.
- **Whitespace/newlines**: whitespace and newlines separate tokens; blank lines are allowed.

Property:

- Structure: `<key> <sep> <value>`, where `<sep>` is one of `=`/`==`/`!=`/`<>`.
- Key: unquoted or double-quoted. Unquoted keys must not contain `# = { }` or whitespace.
- Example:

```cwt
cost = int
acceleration = float
class = enum[shipsize_class]
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
    ## cardinality = 0..1
    ### The base cost of this ship_size
    cost = int

    modifier = {
        alias_name[modifier] = alias_match_left[modifier]
    }
}
```

Comments and documentation:

- **Line comment**: starts with `#`; the whole line is a comment.
- **Option comment**: starts with `##`; declares metadata for the immediately following member (property/block/value).
  - Syntax mirrors properties: `<optionKey> <sep> <optionValue>`.
  - Common examples: `## cardinality = 0..1`, `## severity = warning`, `## push_scope = country`.
- **Documentation comment**: starts with `###`; provides human-readable docs for a member (shown in completion/tooltips).

Grammar highlights and example:

```cwt
# regular comment
types = {
    ### Documentation text
    ## option_key = option_value
    type[army] = {
        path = "game/common/armies"
        subtype[buildable] = {
            potential = {
                ## cardinality = 0..0
                always = no
            }
        }
        localisation = {
            ## required
            name = "$"
            ## required
            desc = "$_desc"
        }
        images = {
            ## primary
            icon = "#icon"
        }
    }
}
```

Notes:

- An option comment applies to the immediately following member (and, depending on consumer semantics, may scope to its body).
- Avoid reserved characters in unquoted keys/strings; use double quotes for complex content.
- Both equal and not-equal separators are valid for properties and options; pick according to intended semantics.

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

This section documents the surface syntax of Paradox Script and strives to match actual usage and toolchain behaviour.

Basics:

- **Members**: property, value, block.
- **Separators/relations**: `=`, `!=`, `<`, `>`, `<=`, `>=`, `?=`.
- **Comment**: single-line comment starts with `#`.

Property:

- Structure: `<key> <sep> <value>` where `<sep>` is one of the separators above.
- Key: unquoted or double-quoted. Keys can be parameterized.
- Examples: `enabled = yes`, `level >= 2`, `size ?= @var`.

Values:

- **Boolean**: `yes`/`no`.
- **Integer/Float**: e.g., `10`, `-5`, `1.25`.
- **String**: unquoted or double-quoted; quoted strings may embed parameters and inline-parameter-conditions.
- **Color**: `rgb{...}`, `hsv{...}`, `hsv360{...}`.
- **Block**: `{ ... }` with nested properties/values/variables.
- **Scripted variable reference**: `@name`.
- **Inline math**: `@[ <expr> ]` with `+ - * / %`, unary `+/-`, absolute `|x|`, and parentheses.

Scripted variables:

- Declaration: `@<name> = <value>` where value can be boolean/int/float/string/inline-math.
- Reference: `@<name>`.

Parameters:

- Syntax: `$name$` or `$name|argument$`.
- Locations: may appear in variable names, variable references, keys, strings, and inline math.

Parameter conditions:

- Outer form: `[ [!]<parameter> <members...> ]`.
- Purpose: conditionally define members in declaration contexts.

Inline parameter condition:

- Within strings, fragments like: `"a[[cond]b]c"`.

Example:

```paradox_script
# comment
@my_var = 42

effect = {
    enabled = yes
    level >= 2
    size ?= @my_var

    name = "Hello $who|leader$!"

    modifier = {
        add = 1
    }

    result = @[ 1 + 2 * 3 ]
}
```

> [!warning]
> Parameters, parameter conditions and inline math are advanced features, typically meaningful only in specific definitions (e.g., scripted effects/triggers) or when evaluated by the engine.

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

This section documents the surface syntax of Paradox Localisation and strives to match actual usage and toolchain behaviour.


File layout:

- Optional **locale header** line(s): e.g., `l_english:` or `l_simp_chinese:` (multiple allowed to be compatible with `localisation/languages.yml`).
- Multiple **key-value** entries: `<key>:<number?> "<text>"`, where `<number>` is an optional tracking number.
- Comments: single-line comments start with `#`.

Markup inside the quoted text:

- **Color**: `§X ... §!` (`X` is a single-character id).
- **Parameters**: `$name$` or `$name|argument$`. The `name` can be a localisation key, a command, or a scripted variable reference using `$@var$` (parse-equivalent).
- **Bracket commands**: `[text|argument]`, `text` can be parameterized; often used for `Get...` functions/context lookups.
- **Icon**: `£icon|frame£` (the `|frame` part is optional) to embed a GFX icon.
- **Concept command (Stellaris)**: `['concept' <rich text>]` for linking a concept and showing rich text.
- **Text format (CK3/Vic3)**: `#format ... #!` to style a text block; and **Text icon**: `@icon!`.

Example:

```paradox_localisation
l_english:
  my_key:0 "Hello §Y$target$§! [GetPlayerName] £alloys£"
  concept_key:0 "['pop_growth', §G+10%§!]"
```

Notes:

- The number after the colon can be omitted.
- Quoted text generally does not require escaping for quotes if kept balanced; avoid stray quotes.

> [!warning]
> `#format` and `@icon!` are advanced, game-specific constructs and only available in games that implement them. `['concept' ...]` is Stellaris-only.

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

Paradox CSV files are typically regular CSV with project-specific conventions:

- **Column separator**: semicolon `;`.
- **Comments**: single-line comments starting with `#`.
- **Strings**: double-quoted; inner quotes follow the regular CSV rule of doubling (`""`).

Example:

```paradox_csv
# comment
key;col1;col2
id1;"text with ; semicolon";42
id2;plain;"quoted"