package icu.windea.pls.localisation.lexer;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import icu.windea.pls.model.ParadoxGameType;
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.core.StdlibExtensionsKt.*;
import static icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*;

// Lexer for Paradox Localisation (headers, keys, numbers, quoted values).
// Notes:
// - Public interface is stable: do NOT rename %class, token names, or ElementTypes.
// - Locale header vs property key is distinguished by scanning after ':' on the same line.
// - Right-quote heuristic: if another '"' exists before EOL, current '"' is text; otherwise it closes the value.
%%

%{
    public _ParadoxLocalisationLexer() {
        this((java.io.Reader)null);
    }

    private IElementType handleLocaleToken() {
        // Locale headers may be absent or appear multiple times (e.g. localisation/languages.yml).
        // This rule matched: ^ {LOCALE_TOKEN} ":" (no trailing part). We now check the remainder of the line.
        // If only whitespace remains until EOL/EOF, treat as a locale header; otherwise, treat as a property key.

        try {
            // Start scanning right after the matched text (token + ':').
            int i = zzCurrentPos + yylength();
            int length = zzBuffer.length();
            boolean onlyWhitespaceToEol = true;
            while (i < length) {
                char c = zzBuffer.charAt(i);
                if (c == '\n' || c == '\r') break;
                if (!Character.isWhitespace(c)) { onlyWhitespaceToEol = false; break; }
                i++;
            }

            // Push back just ':' so it can be emitted next as COLON.
            yypushback(1);
            if (onlyWhitespaceToEol) {
                yybegin(IN_LOCALE_COLON);
                return LOCALE_TOKEN;
            } else {
                // Not a locale header: interpret as a property key followed by ':'
                yybegin(IN_PROPERTY_COLON);
                return PROPERTY_KEY_TOKEN;
            }
        } catch (Exception e) {
            // Be lenient on unexpected conditions: assume a locale header.
            yypushback(1);
            yybegin(IN_LOCALE_COLON);
            return LOCALE_TOKEN;
        }
    }

    private IElementType handleRightQuote() {
        // Double quotes inside localisation text do not need escaping.
        // Heuristic used by vanilla files and editors:
        //  - If there is another '"' ahead on the same line, the current '"' is part of the text (not closing).
        //  - Otherwise, treat the current '"' as the closing quote, even if a trailing comment (e.g. '# ...') exists.

        try {
            int i = zzCurrentPos + yylength(); // position right after current match
            int length = zzBuffer.length();
            while (i < length) {
                char c = zzBuffer.charAt(i);
                if (c == '\n' || c == '\r') break; // reached EOL
                if (c == '"') return PROPERTY_VALUE_TOKEN; // another quote exists -> current is not closing
                i++;
            }
        } catch (Exception e) {
            // ignored
        }

        yybegin(IN_PROPERTY_END);
        return RIGHT_QUOTE;
    }
%}

%public
%class _ParadoxLocalisationLexer
%implements FlexLexer
%function advance
%type IElementType

%s IN_LOCALE_COLON
%s IN_LOCALE_END
%s IN_PROPERTY_COLON
%s IN_PROPERTY_NUMBER
%s IN_PROPERTY_VALUE
%s IN_PROPERTY_END
%s CHECK_RIGHT_QUOTE

%unicode

EOL=\s*\R
WHITE_SPACE=[\s&&[^\r\n]]+
COMMENT=#[^\r\n]*

LOCALE_TOKEN=[a-z_]+
PROPERTY_NUMBER=\d+
PROPERTY_KEY_TOKEN=[A-Za-z0-9_.\-']+
PROPERTY_VALUE_TOKEN=[^\"\r\n]+ // it's unnecessary to escape double quotes in loc text in fact

%%

// core rules

<YYINITIAL> {
    {WHITE_SPACE} { return WHITE_SPACE; }
    {COMMENT} { return COMMENT; }
    // Locale header candidate: start-of-line locale id followed by ':'
    ^ {LOCALE_TOKEN} ":" { return handleLocaleToken(); }
    {PROPERTY_KEY_TOKEN} { yybegin(IN_PROPERTY_COLON); return PROPERTY_KEY_TOKEN; }
}
<IN_LOCALE_COLON>{
    {WHITE_SPACE} { return WHITE_SPACE; }
    {COMMENT} { return COMMENT; }
    ":" { yybegin(IN_LOCALE_END); return COLON; }
}
<IN_LOCALE_END>{
    {WHITE_SPACE} { return WHITE_SPACE; }
    {COMMENT} { return COMMENT; }
}
<IN_PROPERTY_COLON>{
    {WHITE_SPACE} { return WHITE_SPACE; }
    {COMMENT} { return COMMENT; }
    ":" { yybegin(IN_PROPERTY_NUMBER); return COLON; }
}
<IN_PROPERTY_NUMBER>{
    {WHITE_SPACE} { return WHITE_SPACE; }
    {COMMENT} { return COMMENT; }
    {PROPERTY_NUMBER} { return PROPERTY_NUMBER; }
    \" { yybegin(IN_PROPERTY_VALUE); return LEFT_QUOTE; }
}
<IN_PROPERTY_VALUE> {
    {PROPERTY_VALUE_TOKEN} { return PROPERTY_VALUE_TOKEN; }
    \" { return handleRightQuote(); }
}
<IN_PROPERTY_END>{
    {WHITE_SPACE} { return WHITE_SPACE; }
    {COMMENT} { return COMMENT; }
}

{EOL} { yybegin(YYINITIAL); return WHITE_SPACE; }

[^] { return BAD_CHARACTER; }
