# Appendix: Syntax Reference

## Positioning & Vision

This chapter aims to be an authoritative, practical, and concise reference for Paradox modding syntaxes. We strive to align with real-world usage and toolchain behaviour, and will continue refining it as the ecosystem evolves.

## Overview

This chapter covers the following syntaxes (examples use Prism code fences):

* __CWT config file__ (`*.cwt`)
  - Comments: `#`, `##` (option comment), `###` (doc comment)
  - Code fence language id: `cwt`
* __Paradox Script__
  - Comments: `#`
  - Code fence language id: `paradox_script`
* __Paradox Localisation__ (`.yml`)
  - Comments: `#`
  - Code fence language id: `paradox_localisation`
* __Paradox CSV__ (semicolon-separated CSV)
  - Comments: `#`
  - Code fence language id: `paradox_csv`

## CWT File

<!-- AI: maps to icu.windea.pls.cwt.CwtLanguage; icu.windea.pls.cwt.CwtFileType -->
<!-- AI: impl-notes
Language id: CWT; default extension: .cwt. This section describes surface syntax and lexer tokens; details follow the plugin grammar.
-->

This section defines the syntax of CWT config files used to author and organize CWT configuration content. The surface form resembles Paradox Script with additional capabilities such as option comments.

Basics:

* __Member types__: property, value, block.
* __Separators__: `=`/`==` mean equal; `!=`/`<>` mean not equal.
* __Whitespace/newlines__: whitespace and newlines separate tokens; blank lines are allowed.

Property:

* Structure: `<key> <sep> <value>`, where `<sep>` is one of `=`/`==`/`!=`/`<>`.
* Key: unquoted or double-quoted. Unquoted keys must not contain `# = { }` or whitespace.
* Example:

```cwt
cost = int
acceleration = float
class = enum[shipsize_class]
```

Value:

* __Boolean__: `yes`/`no`.
* __Integer__: `10`, `-5` (leading minus and leading zeros are permitted).
* __Float__: `1.0`, `-0.5`.
* __String__:
  - Unquoted: must not contain `# = { }` or whitespace.
  - Double-quoted: supports escapes like `\"`.
* __Block__: see below.

Block:

* Enclosed by braces: `{ ... }`.
* Content may mix: properties, values, and option comments (see below). Both inline and standalone comments are allowed.
* Example:

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

* __Line comment__: starts with `#`; the whole line is a comment.
* __Option comment__: starts with `##`; declares metadata for the immediately following member (property/block/value).
  - Syntax mirrors properties: `<optionKey> <sep> <optionValue>`.
  - Common examples: `## cardinality = 0..1`, `## severity = warning`, `## push_scope = country`.
* __Documentation comment__: starts with `###`; provides human-readable docs for a member (shown in completion/tooltips).

Grammar highlights and example:

```cwt
# regular comment
## option_key = option_value    # option comment (applies to the next member)
### Documentation text          # doc comment

types = {
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
      Name = "$"
      ## required
      Desc = "$_desc"
    }
    images = {
      ## primary
      icon = "#icon"
    }
  }
}
```

Notes:

* An option comment applies to the immediately following member (and, depending on consumer semantics, may scope to its body).
* Avoid reserved characters in unquoted keys/strings; use double quotes for complex content.
* Both equal and not-equal separators are valid for properties and options; pick according to intended semantics.

## Paradox Script File

This section documents the surface syntax of Paradox Script and strives to match actual usage and toolchain behaviour.
<!-- impl: src/main/kotlin/icu/windea/pls/script/ParadoxScript.bnf, src/main/kotlin/icu/windea/pls/script/ParadoxScript.flex -->

Basics:

* __Members__: property, value, block.
* __Separators/relations__: `=`, `!=`, `<`, `>`, `<=`, `>=`, `?=`.
* __Comment__: single-line comment starts with `#`.

Property:

* Structure: `<key> <sep> <value>` where `<sep>` is one of the separators above.
* Key: unquoted or double-quoted. Keys can be parameterized.
* Examples: `enabled = yes`, `level >= 2`, `size ?= @var`.

Values:

* __Boolean__: `yes`/`no`.
* __Integer/Float__: e.g., `10`, `-5`, `1.25`.
* __String__: unquoted or double-quoted; quoted strings may embed parameters and inline-parameter-conditions.
* __Color__: `rgb{...}`, `hsv{...}`, `hsv360{...}`.
* __Block__: `{ ... }` with nested properties/values/variables.
* __Scripted variable reference__: `@name`.
* __Inline math__: `@[ <expr> ]` with `+ - * / %`, unary `+/-`, absolute `|x|`, and parentheses.

Scripted variables:

* Definition: `@<name> = <value>` where value can be boolean/int/float/string/inline-math.
* Reference: `@<name>`.

Parameters:

* Syntax: `$name$` or `$name|argument$`.
* Locations: may appear in variable names, variable references, keys, strings, and inline math.

Parameter conditions:

* Outer form: `[ [!]<parameter> <members...> ]`.
* Purpose: conditionally define members in declaration contexts.

Inline parameter condition:

* Within strings, fragments like: `"a[[cond]b]c"`.

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

  result = @[1 + 2 * 3]
}
```

Notes:

> [!warning]
> Parameters, parameter conditions and inline math are advanced features, typically meaningful only in specific definitions (e.g., scripted effects/triggers) or when evaluated by the engine.
* Avoid reserved characters in unquoted keys/strings; prefer quoted strings for complex text.

## Paradox Localisation File

This section documents the surface syntax of Paradox Localisation and strives to match actual usage and toolchain behaviour.
<!-- impl: src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.bnf, src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.flex, src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.Text.flex -->

File layout:

* Optional __locale header__ line(s): e.g., `l_english:` or `l_simp_chinese:` (multiple allowed to be compatible with `localisation/languages.yml`).
* Multiple __key-value__ entries: `<key>:<number?> "<text>"`, where `<number>` is an optional tracking number.
* Comments: single-line comments start with `#`.

Markup inside the quoted text:

* __Color__: `§X ... §!` (`X` is a single-character id).
* __Parameters__: `$name$` or `$name|argument$`. The `name` can be a localisation key, a command, or a scripted variable reference using `$@var$` (parse-equivalent).
* __Bracket commands__: `[text|argument]`, `text` can be parameterized; often used for `Get...` functions/context lookups.
* __Icon__: `£icon|frame£` (the `|frame` part is optional) to embed a GFX icon.
* __Concept command (Stellaris)__: `['concept' <rich text>]` for linking a concept and showing rich text.
* __Text format (CK3/Vic3)__: `#format ... #!` to style a text block; and __Text icon__: `@icon!`.

Example:

```paradox_localisation
l_english:
  my_key:0 "Hello §Y$target$§! [GetPlayerName] £alloys£"
  concept_key:0 "['pop_growth', §G+10%§!]"
```

Notes:

* The number after the colon can be omitted.
* Quoted text generally does not require escaping for quotes if kept balanced; avoid stray quotes.

> [!warning]
> `#format` and `@icon!` are advanced, game-specific constructs and only available in games that implement them. `['concept' ...]` is Stellaris-only.

## Paradox CSV File

Paradox CSV files are typically regular CSV with project-specific conventions:

* __Column separator__: semicolon `;`.
* __Comments__: single-line comments starting with `#`.
* __Strings__: double-quoted; inner quotes follow the regular CSV rule of doubling (`""`).

Example:

```paradox_csv
# comment
key;col1;col2
id1;"text with ; semicolon";42
id2;plain;"quoted"