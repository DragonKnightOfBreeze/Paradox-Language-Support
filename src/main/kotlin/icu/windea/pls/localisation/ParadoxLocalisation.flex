// Copyright (c) 2021 DragonKnightOfBreeze Windea <dk_breeze@qq.com>
// All rights reserved.

// Lexer for Paradox Localisation.
// Notes:
// - Use trailing context for high-priority rules.

package icu.windea.pls.localisation.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import icu.windea.pls.model.ParadoxGameType;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*;

%%

%{
    private ParadoxGameType gameType;

    public _ParadoxLocalisationLexer() {
        this((java.io.Reader)null);
        this.gameType = null;
    }

    public _ParadoxLocalisationLexer(ParadoxGameType gameType) {
        this((java.io.Reader)null);
        this.gameType = gameType;
    }

    public ParadoxGameType getGameType() {
        return this.gameType;
    }

    private IElementType handleLocaleToken() {
        // Locale headers may be absent or appear multiple times (e.g. in `localisation/languages.yml`).
        // This rule matched: ^ {LOCALE_TOKEN} ":" (no trailing part). So, we now check the remainder of the line.
        // Heuristic:
        // - If it's at line start, and there are no characters or only whitespaces until EOL/EOF, treat as a locale.
        // - Otherwise, treat as a property key.

        try {
            // start scanning right after the matched text (token + `:`)
            int i = zzCurrentPos + 1 + yylength();
            int length = zzBuffer.length();
            boolean onlyWhitespaceToEol = true;
            while (i < length) {
                char c = zzBuffer.charAt(i);
                if (c == '\n' || c == '\r') break;
                if (!Character.isWhitespace(c)) { onlyWhitespaceToEol = false; break; }
                i++;
            }

            if (onlyWhitespaceToEol) {
                yybegin(IN_LOCALE_COLON);
                return LOCALE_TOKEN;
            } else {
                // not a locale header: interpret as a property key
                yybegin(IN_PROPERTY_COLON);
                return PROPERTY_KEY_TOKEN;
            }
        } catch (Exception e) {
            // be lenient on unexpected conditions: assume a locale header
            yybegin(IN_LOCALE_COLON);
            return LOCALE_TOKEN;
        }
    }

    private IElementType handleRightQuote() {
        // Double quotes inside localisation text do not need escaping.
        // Heuristic:
        //  - If there is another `"` ahead on the same line, the current `"` is part of the text (not closing).
        //  - Otherwise, treat the current `"` as the closing quote, even if a trailing comment (e.g. `# ...`) exists.

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
%s IN_PROPERTY_NUMBER_TOKEN
%s IN_PROPERTY_VALUE
%s IN_PROPERTY_END

%unicode

Eol = \s*\R\s*
WhiteSpace = [\s&&[^\r\n]]+
Blank = \s+

Comment = #[^\r\n]*

Colon = ":"
Quote = \"

LocaleToken = [a-z_]+ // lowercase letters and underscore only
PropertyKeyToken = [A-Za-z0-9_.\-']+ // `.-'` are allowed additionally
PropertyNumberToken = \d+ // integer characters only
PropertyValueToken = [^\"\r\n]+ // it's unnecessary to escape double quotes in loc text in fact

// lazy-scanning localisation text (see `ParadoxLocaliation.Text.flex`)

%%

// common rules

<YYINITIAL> {
    {Blank} { return WHITE_SPACE; }
    {Comment} { return COMMENT; }
    ^ {LocaleToken} / {Colon} { return handleLocaleToken(); }
    {PropertyKeyToken} { yybegin(IN_PROPERTY_COLON); return PROPERTY_KEY_TOKEN; }
}
<IN_LOCALE_COLON>{
    {WhiteSpace} { return WHITE_SPACE; }
    {Eol} { yybegin(YYINITIAL); return WHITE_SPACE; }
    {Comment} { yybegin(YYINITIAL); return COMMENT; }
    {Colon} { yybegin(IN_LOCALE_END); return COLON; }
}
<IN_LOCALE_END>{
    {WhiteSpace} { return WHITE_SPACE; }
    {Eol} { yybegin(YYINITIAL); return WHITE_SPACE; }
    {Comment} { yybegin(YYINITIAL); return COMMENT; }
}
<IN_PROPERTY_COLON>{
    {WhiteSpace} { return WHITE_SPACE; }
    {Eol} { yybegin(YYINITIAL); return WHITE_SPACE; }
    {Comment} { yybegin(YYINITIAL); return COMMENT; }
    {Colon} { yybegin(IN_PROPERTY_NUMBER_TOKEN); return COLON; }
}
<IN_PROPERTY_NUMBER_TOKEN>{
    {WhiteSpace} { return WHITE_SPACE; }
    {Eol} { yybegin(YYINITIAL); return WHITE_SPACE; }
    {Comment} { yybegin(YYINITIAL); return COMMENT; }
    {PropertyNumberToken} { return PROPERTY_NUMBER_TOKEN; }
    {Quote} { yybegin(IN_PROPERTY_VALUE); return LEFT_QUOTE; }
    [^] { yypushback(1); yybegin(IN_PROPERTY_VALUE); } // 3.0.2 compatible with missing opening quote
}
<IN_PROPERTY_VALUE> {
    {Eol} { yybegin(YYINITIAL); return WHITE_SPACE; }
    {PropertyValueToken} { return PROPERTY_VALUE_TOKEN; }
    {Quote} { return handleRightQuote(); }
}
<IN_PROPERTY_END>{
    {WhiteSpace} { return WHITE_SPACE; }
    {Eol} { yybegin(YYINITIAL); return WHITE_SPACE; }
    {Comment} { yybegin(YYINITIAL); return COMMENT; }
}

// fallback

[^] { return BAD_CHARACTER; }
