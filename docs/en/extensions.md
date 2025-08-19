# Extensions

## Markdown {#md}

> [!NOTE]
> 
> Features in this section require the [Markdown](https://plugins.jetbrains.com/plugin/7793-markdown) plugin to be installed and enabled.

PLS extends integration with Markdown, covering links, inline code, code fences, and more.

### Links

By using specially formatted link texts with specific prefixes, PLS can resolve Markdown links to matched target references (definitions, localizations, etc.),
providing additional language features in the editor such as code navigation and quick documentation.

This also applies to links in other places, such as HTML links, and links in the raw text of quick documentation for navigation to PSI elements.

![](../images/extensions/md_link_1.png)

For different types of reference links, the formats and examples are as follows:

**CWT config** (currently limited support)

* Format
  * `cwt:{gameType}/{parts}`
* Examples
  * `cwt:stellaris:types/civic_or_origin`
  * `cwt:stellaris:types/civic_or_origin/origin`

**Scoped Variable**

* Format
  * `pdx.sv:{name}`
  * `pdx.sv:{gameType}/{name}`
* Examples
  * `pdx.sv:civic_default_random_weight`
  * `pdx.sv:stellaris:civic_default_random_weight`

**Definition**

* Format
  * `pdx.d:{typeExpression}/{name}`
  * `pdx.d:{gameType}/{typeExpression}/{name}`
* Examples
  * `pdx.d:origin_default`
  * `pdx.d:stellaris:origin_default`
  * `pdx.d:civic_or_origin.origin/origin_default`
  * `pdx.d:stellaris:civic_or_origin.origin/origin_default`

**Localization**

* Format
  * `pdx.l:{name}`
  * `pdx.l:{gameType}/{name}`
* Examples
  * `pdx.l:origin_default_desc`
  * `pdx.l:stellaris:origin_default_desc`

**File Path** (relative to game or mod directory)

* Format
  * `pdx.p:{path}`
  * `pdx.p:{gameType}/{path}`
* Examples
  * `pdx.p:common/governments/civics/00_origins.txt`
  * `pdx.p:stellaris:common/governments/civics/00_origins.txt`

**Modifier**

* Format
  * `pdx.m:{name}`
  * `pdx.m:{gameType}:{name}`
* Examples
  * `pdx.m:job_soldier_add`
  * `pdx.m:stellaris:job_soldier_add`

Notes:

* `{gameType}` - Game type ID. Currently supported values: `stellaris`, `ck2`, `ck3`, `eu4`, `hoi4`, `ir`, `vic2`, `vic3` (for shared config groups use `core`).
* `{typeExpression}` - Definition type expression, can be a base type (e.g., `civic_or_origin`) or include base type and subtypes separated by dots (e.g., `civic_or_origin.origin`).

### Inline Code

> [!NOTE]
>
> Features in this section require specific advanced settings to be enabled (`Advanced Settings > Paradox Language Support > Resolve Markdown inline codes`)

PLS can try to resolve Markdown inline codes to matched target references (definitions, localizations, etc.),
providing additional language features in the editor such as code navigation and quick documentation.

![](../images/extensions/md_inline_code_1.png)

![](../images/extensions/md_inline_code_2.png)

For different types of targets, the formats and examples are as follows:

**Scripted Variable**

* Format
  * `@{name}`
* Example
  * `@civic_default_random_weight`

**Definition**

* Format
  * `{name}`
* Example
  * `origin_default`

**Localisation**

* Format
  * `{name}`
* Example
  * `origin_default_desc`

Note:

* If the inline code can be parsed as a definition and a localization at the same time, the definition will be preferred.

### Code Fences

By injecting extra information after the language ID of a Markdown code fence, you can specify the game type and file path for script or localization file snippets.
PLS will use this information to match CWT configs and provide advanced language features as if you were editing an actual script or localization file.

![](../images/extensions/md_code_fence_1.png)

The format and examples for the injected information are as follows:

* Format
  * `path={gameType}:{path}`
* Example
  * `path=stellaris:common/armies/injected_defence_armies.txt`

A more complete example:

```paradox_script path=stellaris:common/armies/injected_defence_armies.txt
defense_army = {
    # ...
}
```

Notes:

* `{gameType}` - Game type ID. Currently supported values: `stellaris`, `ck2`, `ck3`, `eu4`, `hoi4`, `ir`, `vic2`, `vic3` (for shared config groups use `core`).
* `{path}` - Simulated file path relative to the game or mod directory. Must be a valid script or localization file path.

## Diagrams {#diagrams}

> [!NOTE]
>
> Features in this section require the Diagrams plugin to be installed and enabled.

> [!WARNING]
> 
> Features in this section and the Diagrams plugin are only available in IDE professional editions (e.g., IntelliJ IDEA Ultimate).

PLS provides several types of diagrams for visualizing the definitions and relationships of certain types of entities.

Currently, only event tree and technology tree diagrams are provided.

> [!TIP]
>
> You can also use the type hierarchy view to inspect event trees and technology trees.
>
> * When the caret is on a definition or its reference, go to `Navigate > Type Hierarchy` in the main menu to open the type hierarchy window.
> * In the hierarchy tool window, you can view all definitions of the same type in a collapsible tree view.
> * If the type is event or technology, you can also view the corresponding event tree or technology tree in the hierarchy view.
>
> ![](../images/extensions/diagram_hierarchy_1.png)

### Event Tree

You can open the event tree diagram in the following ways:

* In the project view, select the script file of an event or its parent directory (including the game or mod directory), then open the context menu, choose `Diagrams > Show Diagram...`, and select the desired diagram type for the event tree.
* In the editor, open the script file of an event, then open the context menu, choose `Diagrams > Show Diagram...`, and select the desired diagram type for the event tree.
* If available, you can also open it directly via a shortcut or the Search Everywhere feature (press `Shift + Shift`).

The event tree diagram displays key information about events and their invocation relationships. You can use the toolbar above to configure which node elements to display and filter the nodes to be shown.

![](../images/extensions/diagram_event_tree_1.png)

1. Set which node elements to display. From left to right: type, property, localized title.
2. Filter nodes to display by query scope. For example, only show nodes corresponding to events in opened files.
3. Open the diagram settings pages.

> [!WARNING]
>
> IDE needs some time to complete data loading and graph rendering. If there are too many nodes and node elements to render, this process may take a long time.

### Technology Tree

If the current game type is *Stellaris*, you can open the technology tree diagram in the following ways:

* In the project view, select the script file of a technology or its parent directory (including the game or mod directory), then open the context menu, choose `Diagrams > Show Diagram...`, and select the desired diagram type for the technology tree.
* In the editor, open the script file of a technology, then open the context menu, choose `Diagrams > Show Diagram...`, and select the desired diagram type for the technology tree.
* If available, you can also open it directly via a shortcut or the Search Everywhere feature (press `Shift + Shift`).

The technology tree diagram displays key information about technologies and their prerequisite relationships. You can use the toolbar above to configure which node elements to display and filter the nodes to be shown.

![](../images/extensions/diagram_tech_tree_1.png)

1. Set which node elements to display. From left to right: type, property, localized name, image (displayed as a technology card).
2. Filter nodes to display by query scope. For example, only show nodes corresponding to technologies in opened files.
3. Open the diagram settings pages.

> [!WARNING]
> 
> IDE needs some time to complete data loading and graph rendering. If there are too many nodes and node elements to render, this process may take a long time.

### Settings Page

In the IDE's settings page, go to `Languages & Frameworks > Paradox Language Support > Diagrams` to open the diagram settings page.

Here you can configure which nodes to display for each diagram according to various conditions.

![](../images/extensions/diagram_settings_1.png)