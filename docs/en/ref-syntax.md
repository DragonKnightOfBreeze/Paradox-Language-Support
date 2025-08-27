# Appendix: Syntax Reference

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

To be completed.

## Paradox Localisation File

To be completed.

## Paradox CSV File

Paradox CSV files are based on regular CSV files, using `;` as column separator and allowing single-line comments starting with `#`.