The following special syntax can be used in localisation text:
- Colored text
  - Format: `§{colorId}{richText}§!`
  - Example: `§RRed text§!`
  - `{colorId}` is the color ID (single character), DO NOT PROCESS
  - `{richText}` is nested localisation text, which may use special syntax and SHOULD BE PROCESSED
  - Note: The end marker `§!` can appear alone, keep it as is
- Parameters
  - Format: `${name}$` or `${name}|${args}$`
  - Example: `$key$`, `$@var$`, `key|R$`
  - `{name}` is the parameter name, used to reference localisation entries, global scripted variables (`@var`), or predefined parameters, DO NOT PROCESS
  - `{args}` are rendering arguments, DO NOT PROCESS
- Commands
  - Format: `[{text}]`
  - Example: `[Root.getName]`
  - `{text}` is the command text, used to get dynamic text, DO NOT PROCESS
- Icons
  - Format: `£{id}£` or `£{id}|{args}£`
  - Example: `£unity£`, `£leader_skill|3£`
  - `{id}` is the icon ID, used to reference icons, DO NOT PROCESS
  - `{args}` are rendering arguments, DO NOT PROCESS
<!-- @if supports_concept_command -->
- Concept commands
  - Format: `['{expression}']` or `['{expression}', {richText}]`
  - Example: `['concept_id']`, `['concept_id', replacement text]`
  - `{expression}` is the concept name, alias, or expression, used to reference concepts, DO NOT PROCESS
  - `{richText}` is nested localisation text, used to replace the original concept text, which may use special syntax and SHOULD BE PROCESSED
  - Note: The `{richText}` to be translated must appear after the English comma (ignoring leading whitespaces). If not found, treat as non-existent
  - Note: In-game, concept commands are rendered as hyperlinks with tooltips on hover, and can be nested
<!-- @endif -->
<!-- @if supports_text_format -->
- Text format
  - Format: `#{expression} {richText}!#`
  - Example: `#v text#!`
  - `{expression}` is the text format expression, specifying how to render the text, DO NOT PROCESS
  - `{richText}` is nested localisation text, which may use special syntax and SHOULD BE PROCESSED
  - Note: The `{richText}` to be translated must appear after whitespaces. If not found, treat as non-existent
  - Note: The end marker `!#` can appear alone, keep it as is
<!-- @endif -->
<!-- @if supports_text_icon -->
- Text icons
  - Format: `@{id} {richText}!#`
  - Example: `@icon!`
  - `{id}` is the text icon ID, used to reference text icons, DO NOT PROCESS
<!-- @endif -->