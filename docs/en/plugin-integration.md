# Plugin Integration

## Integrate **Translation** Plugin

When [Translation](https://github.com/YiiGuxing/TranslationPlugin) plugin is also installed and enabled,
PLS will provide bellow additional functions:

### Translate documentation

When the cursor is over a definition name, localized key or CWT expression (property key & value),
can translate documentation content by click and select `Translate Documentation` icon button in quick documentation popup window,
or click `Translate Documentation` item in right-click menu.

![](../assets/images/plugin-integration/translate-documentation.gif)

### Intention: Copy localisation(s) to the clipboard for locale...

When the cursor selection scope involves localization(s),
this intention can copy all involved localization(s) to the clipboard,
and try to translate the localisation text into the specified locale before doing so.

![](../assets/images/plugin-integration/intention_copy_loc_for_locale.gif)

**Note:* There is no guarantee that special markers (such as color text markers) will be preserved correctly after translation.