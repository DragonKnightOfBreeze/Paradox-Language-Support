---
created: 2026-11-03
updated: 2026-11-03
---

## General

- Write doc comments for Java and Kotlin in Chinese by default, unless explicitly requested, or need to be consistent with the context.
- Write doc comments for other languages in English by default, unless explicitly requested, or need to be consistent with the context.
- Write normal comments in Chinese or English, based on the context.
- Prefer KDoc style for Kotlin.

## Tips

- When referencing types like `PsiElement` in KDoc, prefer KDoc links like `[PsiElement]`.
- Prefer natural-language descriptions over embedding literal code snippets, reference key code locations instead (e.g., via `@see`) when necessary.
- Avoid documenting member properties individually. Fold them into the containing class/interface KDoc (using `@property`) unless a property truly needs detailed documentation.
- Avoid overly long parameter-by-parameter docs (`@param`) unless truly necessary. Prefer describing the method as a whole and referencing parameters inline (e.g., `[param]`) when needed.