// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import static com.intellij.lang.WhitespacesBinders.GREEDY_LEFT_BINDER;
import static com.intellij.lang.WhitespacesBinders.GREEDY_RIGHT_BINDER;
import static icu.windea.pls.script.parser.ParadoxScriptParserUtil.*;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class ParadoxScriptParser implements PsiParser, LightPsiParser {

    public ASTNode parse(IElementType t, PsiBuilder b) {
        parseLight(t, b);
        return b.getTreeBuilt();
    }

    public void parseLight(IElementType t, PsiBuilder b) {
        boolean r;
        b = adapt_builder_(t, b, this, EXTENDS_SETS_);
        Marker m = enter_section_(b, 0, _COLLAPSE_, null);
        r = parse_root_(t, b);
        exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
    }

    protected boolean parse_root_(IElementType t, PsiBuilder b) {
        return parse_root_(t, b, 0);
    }

    static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
        boolean r;
        if (t == INLINE_MATH_ROOT) {
            r = inline_math_root(b, l + 1);
        } else {
            r = root(b, l + 1);
        }
        return r;
    }

    public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[]{
        create_token_set_(INLINE_MATH_BINARY_EXPRESSION, INLINE_MATH_EXPRESSION, INLINE_MATH_FACTOR, INLINE_MATH_GROUPING_EXPRESSION,
            INLINE_MATH_NUMBER, INLINE_MATH_PARAMETER, INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE, INLINE_MATH_UNARY_EXPRESSION),
        create_token_set_(BLOCK, BOOLEAN, COLOR, FLOAT,
            INLINE_MATH, INT, SCRIPTED_VARIABLE_REFERENCE, STRING,
            VALUE),
    };

    /* ********************************************************** */
    // LEFT_BRACE block_item * RIGHT_BRACE
    public static boolean block(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "block")) return false;
        if (!nextTokenIs(b, LEFT_BRACE)) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_, BLOCK, null);
        r = consumeToken(b, LEFT_BRACE);
        p = r; // pin = 1
        r = r && report_error_(b, block_1(b, l + 1));
        r = p && consumeToken(b, RIGHT_BRACE) && r;
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    // block_item *
    private static boolean block_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "block_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!block_item(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "block_1", c)) break;
        }
        return true;
    }

    /* ********************************************************** */
    // COMMENT | block_value | property | scripted_variable | normal_conditional_block
    static boolean block_item(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "block_item")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_);
        r = consumeToken(b, COMMENT);
        if (!r) r = block_value(b, l + 1);
        if (!r) r = property(b, l + 1);
        if (!r) r = scripted_variable(b, l + 1);
        if (!r) r = normal_conditional_block(b, l + 1);
        exit_section_(b, l, m, r, false, block_item_auto_recover_);
        return r;
    }

    /* ********************************************************** */
    // scripted_variable_reference | boolean | int | float | string | color | block | inline_math
    static boolean block_value(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "block_value")) return false;
        boolean r;
        r = scripted_variable_reference(b, l + 1);
        if (!r) r = boolean_$(b, l + 1);
        if (!r) r = int_$(b, l + 1);
        if (!r) r = float_$(b, l + 1);
        if (!r) r = string(b, l + 1);
        if (!r) r = color(b, l + 1);
        if (!r) r = block(b, l + 1);
        if (!r) r = inline_math(b, l + 1);
        return r;
    }

    /* ********************************************************** */
    // BOOLEAN_TOKEN
    public static boolean boolean_$(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "boolean_$")) return false;
        if (!nextTokenIs(b, BOOLEAN_TOKEN)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, BOOLEAN_TOKEN);
        exit_section_(b, m, BOOLEAN, r);
        return r;
    }

    /* ********************************************************** */
    // COLOR_TOKEN
    public static boolean color(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "color")) return false;
        if (!nextTokenIs(b, COLOR_TOKEN)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, COLOR_TOKEN);
        exit_section_(b, m, COLOR, r);
        return r;
    }

    /* ********************************************************** */
    // NOT_SIGN ? conditional_parameter
    public static boolean conditional_expression(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "conditional_expression")) return false;
        if (!nextTokenIs(b, "<conditional expression>", CONDITION_PARAMETER_TOKEN, NOT_SIGN)) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, CONDITIONAL_EXPRESSION, "<conditional expression>");
        r = conditional_expression_0(b, l + 1);
        r = r && conditional_parameter(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // NOT_SIGN ?
    private static boolean conditional_expression_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "conditional_expression_0")) return false;
        consumeToken(b, NOT_SIGN);
        return true;
    }

    /* ********************************************************** */
    // NESTED_LEFT_BRACKET conditional_expression NESTED_RIGHT_BRACKET
    static boolean conditional_header(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "conditional_header")) return false;
        if (!nextTokenIs(b, NESTED_LEFT_BRACKET)) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_);
        r = consumeToken(b, NESTED_LEFT_BRACKET);
        p = r; // pin = 1
        r = r && report_error_(b, conditional_expression(b, l + 1));
        r = p && consumeToken(b, NESTED_RIGHT_BRACKET) && r;
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    /* ********************************************************** */
    // CONDITION_PARAMETER_TOKEN
    public static boolean conditional_parameter(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "conditional_parameter")) return false;
        if (!nextTokenIs(b, CONDITION_PARAMETER_TOKEN)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, CONDITION_PARAMETER_TOKEN);
        exit_section_(b, m, CONDITIONAL_PARAMETER, r);
        return r;
    }

    /* ********************************************************** */
    // FLOAT_TOKEN
    public static boolean float_$(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "float_$")) return false;
        if (!nextTokenIs(b, FLOAT_TOKEN)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, FLOAT_TOKEN);
        exit_section_(b, m, FLOAT, r);
        return r;
    }

    /* ********************************************************** */
    // LEFT_BRACKET <<processInlineConditionalBlock>> conditional_header inline_conditional_block_item RIGHT_BRACKET
    public static boolean inline_conditional_block(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_conditional_block")) return false;
        if (!nextTokenIs(b, LEFT_BRACKET)) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_, INLINE_CONDITIONAL_BLOCK, null);
        r = consumeToken(b, LEFT_BRACKET);
        r = r && processInlineConditionalBlock(b, l + 1);
        p = r; // pin = 2
        r = r && report_error_(b, conditional_header(b, l + 1));
        r = p && report_error_(b, inline_conditional_block_item(b, l + 1)) && r;
        r = p && consumeToken(b, RIGHT_BRACKET) && r;
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    /* ********************************************************** */
    // <<processInlineConditionalBlockItem>> inline_conditional_block_snippet <<postProcessInlineConditionalBlockItem>>
    static boolean inline_conditional_block_item(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_conditional_block_item")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = processInlineConditionalBlockItem(b, l + 1);
        r = r && inline_conditional_block_snippet(b, l + 1);
        r = r && postProcessInlineConditionalBlockItem(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // ARGUMENT_TOKEN | normal_parameter | inline_conditional_block
    static boolean inline_conditional_block_item_part(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_conditional_block_item_part")) return false;
        boolean r;
        r = consumeToken(b, ARGUMENT_TOKEN);
        if (!r) r = normal_parameter(b, l + 1);
        if (!r) r = inline_conditional_block(b, l + 1);
        return r;
    }

    /* ********************************************************** */
    // inline_conditional_block_item_part (<<processPart>> inline_conditional_block_item_part) *
    static boolean inline_conditional_block_snippet(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_conditional_block_snippet")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = inline_conditional_block_item_part(b, l + 1);
        r = r && inline_conditional_block_snippet_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (<<processPart>> inline_conditional_block_item_part) *
    private static boolean inline_conditional_block_snippet_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_conditional_block_snippet_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!inline_conditional_block_snippet_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inline_conditional_block_snippet_1", c)) break;
        }
        return true;
    }

    // <<processPart>> inline_conditional_block_item_part
    private static boolean inline_conditional_block_snippet_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_conditional_block_snippet_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = processPart(b, l + 1);
        r = r && inline_conditional_block_item_part(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // INLINE_MATH_START INLINE_MATH_TOKEN INLINE_MATH_END
    public static boolean inline_math(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math")) return false;
        if (!nextTokenIs(b, INLINE_MATH_START)) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_, INLINE_MATH, null);
        r = consumeTokens(b, 1, INLINE_MATH_START, INLINE_MATH_TOKEN, INLINE_MATH_END);
        p = r; // pin = 1
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    /* ********************************************************** */
    // LABS_SIGN inline_math_expr RABS_SIGN
    static boolean inline_math_abs_expr(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_abs_expr")) return false;
        if (!nextTokenIs(b, LABS_SIGN)) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_);
        r = consumeToken(b, LABS_SIGN);
        p = r; // pin = 1
        r = r && report_error_(b, inline_math_expr(b, l + 1));
        r = p && consumeToken(b, RABS_SIGN) && r;
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    /* ********************************************************** */
    // inline_math_add_op inline_math_expr_factor
    public static boolean inline_math_add_expr(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_add_expr")) return false;
        if (!nextTokenIs(b, "<inline math add expr>", MINUS_SIGN, PLUS_SIGN)) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _LEFT_, INLINE_MATH_BINARY_EXPRESSION, "<inline math add expr>");
        r = inline_math_add_op(b, l + 1);
        p = r; // pin = 1
        r = r && inline_math_expr_factor(b, l + 1);
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    /* ********************************************************** */
    // PLUS_SIGN | MINUS_SIGN
    static boolean inline_math_add_op(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_add_op")) return false;
        if (!nextTokenIs(b, "", MINUS_SIGN, PLUS_SIGN)) return false;
        boolean r;
        r = consumeToken(b, PLUS_SIGN);
        if (!r) r = consumeToken(b, MINUS_SIGN);
        return r;
    }

    /* ********************************************************** */
    // inline_math_add_expr | inline_math_mul_expr
    public static boolean inline_math_binary_expression(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_binary_expression")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _COLLAPSE_, INLINE_MATH_BINARY_EXPRESSION, "<inline math binary expression>");
        r = inline_math_add_expr(b, l + 1);
        if (!r) r = inline_math_mul_expr(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // inline_math_expr_term
    static boolean inline_math_expr(PsiBuilder b, int l) {
        return inline_math_expr_term(b, l + 1);
    }

    /* ********************************************************** */
    // inline_math_expr_unary inline_math_mul_expr *
    static boolean inline_math_expr_factor(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_expr_factor")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = inline_math_expr_unary(b, l + 1);
        r = r && inline_math_expr_factor_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // inline_math_mul_expr *
    private static boolean inline_math_expr_factor_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_expr_factor_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!inline_math_mul_expr(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inline_math_expr_factor_1", c)) break;
        }
        return true;
    }

    /* ********************************************************** */
    // inline_math_grouping_expression | inline_math_factor
    static boolean inline_math_expr_primary(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_expr_primary")) return false;
        boolean r;
        r = inline_math_grouping_expression(b, l + 1);
        if (!r) r = inline_math_factor(b, l + 1);
        return r;
    }

    /* ********************************************************** */
    // inline_math_expr_factor inline_math_add_expr *
    static boolean inline_math_expr_term(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_expr_term")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = inline_math_expr_factor(b, l + 1);
        r = r && inline_math_expr_term_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // inline_math_add_expr *
    private static boolean inline_math_expr_term_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_expr_term_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!inline_math_add_expr(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inline_math_expr_term_1", c)) break;
        }
        return true;
    }

    /* ********************************************************** */
    // inline_math_unary_expression | inline_math_expr_primary
    static boolean inline_math_expr_unary(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_expr_unary")) return false;
        boolean r;
        r = inline_math_unary_expression(b, l + 1);
        if (!r) r = inline_math_expr_primary(b, l + 1);
        return r;
    }

    /* ********************************************************** */
    // inline_math_binary_expression | inline_math_unary_expression | inline_math_grouping_expression | inline_math_factor
    public static boolean inline_math_expression(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_expression")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _COLLAPSE_, INLINE_MATH_EXPRESSION, "<inline math expression>");
        r = inline_math_binary_expression(b, l + 1);
        if (!r) r = inline_math_unary_expression(b, l + 1);
        if (!r) r = inline_math_grouping_expression(b, l + 1);
        if (!r) r = inline_math_factor(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // inline_math_number | inline_math_scripted_variable_reference | inline_math_parameter
    public static boolean inline_math_factor(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_factor")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _COLLAPSE_, INLINE_MATH_FACTOR, "<inline math factor>");
        r = inline_math_number(b, l + 1);
        if (!r) r = inline_math_scripted_variable_reference(b, l + 1);
        if (!r) r = inline_math_parameter(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // inline_math_par_expr | inline_math_abs_expr
    public static boolean inline_math_grouping_expression(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_grouping_expression")) return false;
        if (!nextTokenIs(b, "<inline math grouping expression>", LABS_SIGN, LP_SIGN)) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, INLINE_MATH_GROUPING_EXPRESSION, "<inline math grouping expression>");
        r = inline_math_par_expr(b, l + 1);
        if (!r) r = inline_math_abs_expr(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // inline_math_mul_op inline_math_expr_unary
    public static boolean inline_math_mul_expr(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_mul_expr")) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _LEFT_, INLINE_MATH_BINARY_EXPRESSION, "<inline math mul expr>");
        r = inline_math_mul_op(b, l + 1);
        p = r; // pin = 1
        r = r && inline_math_expr_unary(b, l + 1);
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    /* ********************************************************** */
    // TIMES_SIGN | DIV_SIGN | MOD_SIGN
    static boolean inline_math_mul_op(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_mul_op")) return false;
        boolean r;
        r = consumeToken(b, TIMES_SIGN);
        if (!r) r = consumeToken(b, DIV_SIGN);
        if (!r) r = consumeToken(b, MOD_SIGN);
        return r;
    }

    /* ********************************************************** */
    // INT_NUMBER_TOKEN | FLOAT_NUMBER_TOKEN
    public static boolean inline_math_number(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_number")) return false;
        if (!nextTokenIs(b, "<inline math number>", FLOAT_NUMBER_TOKEN, INT_NUMBER_TOKEN)) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, INLINE_MATH_NUMBER, "<inline math number>");
        r = consumeToken(b, INT_NUMBER_TOKEN);
        if (!r) r = consumeToken(b, FLOAT_NUMBER_TOKEN);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // LP_SIGN inline_math_expr RP_SIGN
    static boolean inline_math_par_expr(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_par_expr")) return false;
        if (!nextTokenIs(b, LP_SIGN)) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_);
        r = consumeToken(b, LP_SIGN);
        p = r; // pin = 1
        r = r && report_error_(b, inline_math_expr(b, l + 1));
        r = p && consumeToken(b, RP_SIGN) && r;
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    /* ********************************************************** */
    // PARAMETER_START parameter_name parameter_argument_part ? PARAMETER_END
    public static boolean inline_math_parameter(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_parameter")) return false;
        if (!nextTokenIs(b, PARAMETER_START)) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_, INLINE_MATH_PARAMETER, null);
        r = consumeToken(b, PARAMETER_START);
        p = r; // pin = 1
        r = r && report_error_(b, parameter_name(b, l + 1));
        r = p && report_error_(b, inline_math_parameter_2(b, l + 1)) && r;
        r = p && consumeToken(b, PARAMETER_END) && r;
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    // parameter_argument_part ?
    private static boolean inline_math_parameter_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_parameter_2")) return false;
        parameter_argument_part(b, l + 1);
        return true;
    }

    /* ********************************************************** */
    // inline_math_expr
    public static boolean inline_math_root(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_root")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, INLINE_MATH_ROOT, "<inline math root>");
        r = inline_math_expr(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // inline_math_scripted_variable_reference_part <<postProcessFirstPart>> (<<processPart>> inline_math_scripted_variable_reference_part) *
    static boolean inline_math_scripted_variable_parts(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_scripted_variable_parts")) return false;
        if (!nextTokenIs(b, "", PARAMETER_START, SCRIPTED_VARIABLE_REFERENCE_TOKEN)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = inline_math_scripted_variable_reference_part(b, l + 1);
        r = r && postProcessFirstPart(b, l + 1);
        r = r && inline_math_scripted_variable_parts_2(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (<<processPart>> inline_math_scripted_variable_reference_part) *
    private static boolean inline_math_scripted_variable_parts_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_scripted_variable_parts_2")) return false;
        while (true) {
            int c = current_position_(b);
            if (!inline_math_scripted_variable_parts_2_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "inline_math_scripted_variable_parts_2", c)) break;
        }
        return true;
    }

    // <<processPart>> inline_math_scripted_variable_reference_part
    private static boolean inline_math_scripted_variable_parts_2_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_scripted_variable_parts_2_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = processPart(b, l + 1);
        r = r && inline_math_scripted_variable_reference_part(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // AT ? inline_math_scripted_variable_parts
    public static boolean inline_math_scripted_variable_reference(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_scripted_variable_reference")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _COLLAPSE_, INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE, "<inline math scripted variable reference>");
        r = inline_math_scripted_variable_reference_0(b, l + 1);
        r = r && inline_math_scripted_variable_parts(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    // AT ?
    private static boolean inline_math_scripted_variable_reference_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_scripted_variable_reference_0")) return false;
        consumeToken(b, AT);
        return true;
    }

    /* ********************************************************** */
    // SCRIPTED_VARIABLE_REFERENCE_TOKEN | inline_math_parameter
    static boolean inline_math_scripted_variable_reference_part(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_scripted_variable_reference_part")) return false;
        if (!nextTokenIs(b, "", PARAMETER_START, SCRIPTED_VARIABLE_REFERENCE_TOKEN)) return false;
        boolean r;
        r = consumeToken(b, SCRIPTED_VARIABLE_REFERENCE_TOKEN);
        if (!r) r = inline_math_parameter(b, l + 1);
        return r;
    }

    /* ********************************************************** */
    // inline_math_unary_op inline_math_unary_factor
    public static boolean inline_math_unary_expression(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_unary_expression")) return false;
        if (!nextTokenIs(b, "<inline math unary expression>", MINUS_SIGN, PLUS_SIGN)) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _COLLAPSE_, INLINE_MATH_UNARY_EXPRESSION, "<inline math unary expression>");
        r = inline_math_unary_op(b, l + 1);
        p = r; // pin = 1
        r = r && inline_math_unary_factor(b, l + 1);
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    /* ********************************************************** */
    // inline_math_grouping_expression | inline_math_factor
    static boolean inline_math_unary_factor(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_unary_factor")) return false;
        boolean r;
        r = inline_math_grouping_expression(b, l + 1);
        if (!r) r = inline_math_factor(b, l + 1);
        return r;
    }

    /* ********************************************************** */
    // PLUS_SIGN | MINUS_SIGN
    static boolean inline_math_unary_op(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "inline_math_unary_op")) return false;
        if (!nextTokenIs(b, "", MINUS_SIGN, PLUS_SIGN)) return false;
        boolean r;
        r = consumeToken(b, PLUS_SIGN);
        if (!r) r = consumeToken(b, MINUS_SIGN);
        return r;
    }

    /* ********************************************************** */
    // INT_TOKEN
    public static boolean int_$(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "int_$")) return false;
        if (!nextTokenIs(b, INT_TOKEN)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, INT_TOKEN);
        exit_section_(b, m, INT, r);
        return r;
    }

    /* ********************************************************** */
    // LEFT_BRACKET conditional_header normal_conditional_block_item * RIGHT_BRACKET
    public static boolean normal_conditional_block(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "normal_conditional_block")) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_, NORMAL_CONDITIONAL_BLOCK, "<normal conditional block>");
        r = consumeToken(b, LEFT_BRACKET);
        p = r; // pin = 1
        r = r && report_error_(b, conditional_header(b, l + 1));
        r = p && report_error_(b, normal_conditional_block_2(b, l + 1)) && r;
        r = p && consumeToken(b, RIGHT_BRACKET) && r;
        exit_section_(b, l, m, r, p, normal_conditional_block_auto_recover_);
        return r || p;
    }

    // normal_conditional_block_item *
    private static boolean normal_conditional_block_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "normal_conditional_block_2")) return false;
        while (true) {
            int c = current_position_(b);
            if (!normal_conditional_block_item(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "normal_conditional_block_2", c)) break;
        }
        return true;
    }

    /* ********************************************************** */
    // COMMENT | normal_conditional_block_value | property | normal_conditional_block
    static boolean normal_conditional_block_item(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "normal_conditional_block_item")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_);
        r = consumeToken(b, COMMENT);
        if (!r) r = normal_conditional_block_value(b, l + 1);
        if (!r) r = property(b, l + 1);
        if (!r) r = normal_conditional_block(b, l + 1);
        exit_section_(b, l, m, r, false, normal_conditional_block_item_auto_recover_);
        return r;
    }

    /* ********************************************************** */
    // scripted_variable_reference | boolean | int | float | string | color | block | inline_math
    static boolean normal_conditional_block_value(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "normal_conditional_block_value")) return false;
        boolean r;
        r = scripted_variable_reference(b, l + 1);
        if (!r) r = boolean_$(b, l + 1);
        if (!r) r = int_$(b, l + 1);
        if (!r) r = float_$(b, l + 1);
        if (!r) r = string(b, l + 1);
        if (!r) r = color(b, l + 1);
        if (!r) r = block(b, l + 1);
        if (!r) r = inline_math(b, l + 1);
        return r;
    }

    /* ********************************************************** */
    // PARAMETER_START parameter_name parameter_argument_part ? PARAMETER_END
    public static boolean normal_parameter(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "normal_parameter")) return false;
        if (!nextTokenIs(b, PARAMETER_START)) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_, NORMAL_PARAMETER, null);
        r = consumeToken(b, PARAMETER_START);
        p = r; // pin = 1
        r = r && report_error_(b, parameter_name(b, l + 1));
        r = p && report_error_(b, normal_parameter_2(b, l + 1)) && r;
        r = p && consumeToken(b, PARAMETER_END) && r;
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    // parameter_argument_part ?
    private static boolean normal_parameter_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "normal_parameter_2")) return false;
        parameter_argument_part(b, l + 1);
        return true;
    }

    /* ********************************************************** */
    // ARGUMENT_TOKEN
    public static boolean parameter_argument(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "parameter_argument")) return false;
        if (!nextTokenIs(b, ARGUMENT_TOKEN)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, ARGUMENT_TOKEN);
        exit_section_(b, m, PARAMETER_ARGUMENT, r);
        return r;
    }

    /* ********************************************************** */
    // PIPE parameter_argument ?
    static boolean parameter_argument_part(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "parameter_argument_part")) return false;
        if (!nextTokenIs(b, PIPE)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, PIPE);
        r = r && parameter_argument_part_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // parameter_argument ?
    private static boolean parameter_argument_part_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "parameter_argument_part_1")) return false;
        parameter_argument(b, l + 1);
        return true;
    }

    /* ********************************************************** */
    // PARAMETER_TOKEN
    static boolean parameter_name(PsiBuilder b, int l) {
        return consumeToken(b, PARAMETER_TOKEN);
    }

    /* ********************************************************** */
    // property_key property_separator property_value
    public static boolean property(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "property")) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_, PROPERTY, "<property>");
        r = property_key(b, l + 1);
        p = r; // pin = 1
        r = r && report_error_(b, property_separator(b, l + 1));
        r = p && property_value(b, l + 1) && r;
        exit_section_(b, l, m, r, p, property_auto_recover_);
        return r || p;
    }

    /* ********************************************************** */
    // property_key_quoted | property_key_unquoted
    public static boolean property_key(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "property_key")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, PROPERTY_KEY, "<property key>");
        r = property_key_quoted(b, l + 1);
        if (!r) r = property_key_unquoted(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // PROPERTY_KEY_TOKEN | normal_parameter | inline_conditional_block
    static boolean property_key_part(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "property_key_part")) return false;
        boolean r;
        r = consumeToken(b, PROPERTY_KEY_TOKEN);
        if (!r) r = normal_parameter(b, l + 1);
        if (!r) r = inline_conditional_block(b, l + 1);
        return r;
    }

    /* ********************************************************** */
    // property_key_part (<<processPart>> property_key_part)
    static boolean property_key_parts(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "property_key_parts")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = property_key_part(b, l + 1);
        r = r && property_key_parts_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // <<processPart>> property_key_part
    private static boolean property_key_parts_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "property_key_parts_1")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = processPart(b, l + 1);
        r = r && property_key_part(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // LEFT_QUOTE property_key_parts ? RIGHT_QUOTE ?
    static boolean property_key_quoted(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "property_key_quoted")) return false;
        if (!nextTokenIs(b, LEFT_QUOTE)) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_);
        r = consumeToken(b, LEFT_QUOTE);
        p = r; // pin = 1
        r = r && report_error_(b, property_key_quoted_1(b, l + 1));
        r = p && property_key_quoted_2(b, l + 1) && r;
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    // property_key_parts ?
    private static boolean property_key_quoted_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "property_key_quoted_1")) return false;
        property_key_parts(b, l + 1);
        return true;
    }

    // RIGHT_QUOTE ?
    private static boolean property_key_quoted_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "property_key_quoted_2")) return false;
        consumeToken(b, RIGHT_QUOTE);
        return true;
    }

    /* ********************************************************** */
    // property_key_parts RIGHT_QUOTE ?
    static boolean property_key_unquoted(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "property_key_unquoted")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = property_key_parts(b, l + 1);
        r = r && property_key_unquoted_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // RIGHT_QUOTE ?
    private static boolean property_key_unquoted_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "property_key_unquoted_1")) return false;
        consumeToken(b, RIGHT_QUOTE);
        return true;
    }

    /* ********************************************************** */
    // EQUAL_SIGN | LT_SIGN | GT_SIGN | LE_SIGN | GE_SIGN | NOT_EQUAL_SIGN | SAFE_ASSIGN_SIGN | SAFE_CALL_ASSIGN_SIGN
    static boolean property_separator(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "property_separator")) return false;
        boolean r;
        r = consumeToken(b, EQUAL_SIGN);
        if (!r) r = consumeToken(b, LT_SIGN);
        if (!r) r = consumeToken(b, GT_SIGN);
        if (!r) r = consumeToken(b, LE_SIGN);
        if (!r) r = consumeToken(b, GE_SIGN);
        if (!r) r = consumeToken(b, NOT_EQUAL_SIGN);
        if (!r) r = consumeToken(b, SAFE_ASSIGN_SIGN);
        if (!r) r = consumeToken(b, SAFE_CALL_ASSIGN_SIGN);
        return r;
    }

    /* ********************************************************** */
    // scripted_variable_reference | boolean | int | float | string | color | block | inline_math
    static boolean property_value(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "property_value")) return false;
        boolean r;
        r = scripted_variable_reference(b, l + 1);
        if (!r) r = boolean_$(b, l + 1);
        if (!r) r = int_$(b, l + 1);
        if (!r) r = float_$(b, l + 1);
        if (!r) r = string(b, l + 1);
        if (!r) r = color(b, l + 1);
        if (!r) r = block(b, l + 1);
        if (!r) r = inline_math(b, l + 1);
        return r;
    }

    /* ********************************************************** */
    // root_block ?
    static boolean root(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "root")) return false;
        root_block(b, l + 1);
        return true;
    }

    /* ********************************************************** */
    // root_block_item +
    public static boolean root_block(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "root_block")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, ROOT_BLOCK, "<root block>");
        r = root_block_item(b, l + 1);
        while (r) {
            int c = current_position_(b);
            if (!root_block_item(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "root_block", c)) break;
        }
        register_hook_(b, WS_BINDERS, GREEDY_LEFT_BINDER, GREEDY_RIGHT_BINDER);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // COMMENT | root_block_value | property | scripted_variable | normal_conditional_block
    static boolean root_block_item(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "root_block_item")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, COMMENT);
        if (!r) r = root_block_value(b, l + 1);
        if (!r) r = property(b, l + 1);
        if (!r) r = scripted_variable(b, l + 1);
        if (!r) r = normal_conditional_block(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // scripted_variable_reference | boolean | int | float | string | color | block | inline_math
    static boolean root_block_value(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "root_block_value")) return false;
        boolean r;
        r = scripted_variable_reference(b, l + 1);
        if (!r) r = boolean_$(b, l + 1);
        if (!r) r = int_$(b, l + 1);
        if (!r) r = float_$(b, l + 1);
        if (!r) r = string(b, l + 1);
        if (!r) r = color(b, l + 1);
        if (!r) r = block(b, l + 1);
        if (!r) r = inline_math(b, l + 1);
        return r;
    }

    /* ********************************************************** */
    // scripted_variable_name scripted_variable_separator scripted_variable_value
    public static boolean scripted_variable(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "scripted_variable")) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_, SCRIPTED_VARIABLE, "<scripted variable>");
        r = scripted_variable_name(b, l + 1);
        p = r; // pin = 1
        r = r && report_error_(b, scripted_variable_separator(b, l + 1));
        r = p && scripted_variable_value(b, l + 1) && r;
        exit_section_(b, l, m, r, p, scripted_variable_auto_recover_);
        return r || p;
    }

    /* ********************************************************** */
    // AT scripted_variable_name_parts
    public static boolean scripted_variable_name(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "scripted_variable_name")) return false;
        if (!nextTokenIs(b, AT)) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_, SCRIPTED_VARIABLE_NAME, null);
        r = consumeToken(b, AT);
        p = r; // pin = 1
        r = r && scripted_variable_name_parts(b, l + 1);
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    /* ********************************************************** */
    // SCRIPTED_VARIABLE_NAME_TOKEN | normal_parameter | inline_conditional_block
    static boolean scripted_variable_name_part(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "scripted_variable_name_part")) return false;
        boolean r;
        r = consumeToken(b, SCRIPTED_VARIABLE_NAME_TOKEN);
        if (!r) r = normal_parameter(b, l + 1);
        if (!r) r = inline_conditional_block(b, l + 1);
        return r;
    }

    /* ********************************************************** */
    // scripted_variable_name_part (<<processPart>> scripted_variable_name_part) *
    static boolean scripted_variable_name_parts(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "scripted_variable_name_parts")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = scripted_variable_name_part(b, l + 1);
        r = r && scripted_variable_name_parts_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // (<<processPart>> scripted_variable_name_part) *
    private static boolean scripted_variable_name_parts_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "scripted_variable_name_parts_1")) return false;
        while (true) {
            int c = current_position_(b);
            if (!scripted_variable_name_parts_1_0(b, l + 1)) break;
            if (!empty_element_parsed_guard_(b, "scripted_variable_name_parts_1", c)) break;
        }
        return true;
    }

    // <<processPart>> scripted_variable_name_part
    private static boolean scripted_variable_name_parts_1_0(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "scripted_variable_name_parts_1_0")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = processPart(b, l + 1);
        r = r && scripted_variable_name_part(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // AT scripted_variable_reference_parts
    public static boolean scripted_variable_reference(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "scripted_variable_reference")) return false;
        if (!nextTokenIs(b, AT)) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = consumeToken(b, AT);
        r = r && scripted_variable_reference_parts(b, l + 1);
        exit_section_(b, m, SCRIPTED_VARIABLE_REFERENCE, r);
        return r;
    }

    /* ********************************************************** */
    // SCRIPTED_VARIABLE_REFERENCE_TOKEN | normal_parameter | inline_conditional_block
    static boolean scripted_variable_reference_part(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "scripted_variable_reference_part")) return false;
        boolean r;
        r = consumeToken(b, SCRIPTED_VARIABLE_REFERENCE_TOKEN);
        if (!r) r = normal_parameter(b, l + 1);
        if (!r) r = inline_conditional_block(b, l + 1);
        return r;
    }

    /* ********************************************************** */
    // scripted_variable_reference_part <<postProcessFirstPart>> (<<processPart>> scripted_variable_reference_part)
    static boolean scripted_variable_reference_parts(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "scripted_variable_reference_parts")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = scripted_variable_reference_part(b, l + 1);
        r = r && postProcessFirstPart(b, l + 1);
        r = r && scripted_variable_reference_parts_2(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // <<processPart>> scripted_variable_reference_part
    private static boolean scripted_variable_reference_parts_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "scripted_variable_reference_parts_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = processPart(b, l + 1);
        r = r && scripted_variable_reference_part(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // EQUAL_SIGN
    static boolean scripted_variable_separator(PsiBuilder b, int l) {
        return consumeToken(b, EQUAL_SIGN);
    }

    /* ********************************************************** */
    // boolean | int | float | string | inline_math
    static boolean scripted_variable_value(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "scripted_variable_value")) return false;
        boolean r;
        r = boolean_$(b, l + 1);
        if (!r) r = int_$(b, l + 1);
        if (!r) r = float_$(b, l + 1);
        if (!r) r = string(b, l + 1);
        if (!r) r = inline_math(b, l + 1);
        return r;
    }

    /* ********************************************************** */
    // string_quoted | string_unquoted
    public static boolean string(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "string")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _NONE_, STRING, "<string>");
        r = string_quoted(b, l + 1);
        if (!r) r = string_unquoted(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    /* ********************************************************** */
    // STRING_TOKEN | normal_parameter | inline_conditional_block
    static boolean string_part(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "string_part")) return false;
        boolean r;
        r = consumeToken(b, STRING_TOKEN);
        if (!r) r = normal_parameter(b, l + 1);
        if (!r) r = inline_conditional_block(b, l + 1);
        return r;
    }

    /* ********************************************************** */
    // string_part <<postProcessFirstPart>> (<<processPart>> string_part)
    static boolean string_parts(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "string_parts")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = string_part(b, l + 1);
        r = r && postProcessFirstPart(b, l + 1);
        r = r && string_parts_2(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // <<processPart>> string_part
    private static boolean string_parts_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "string_parts_2")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = processPart(b, l + 1);
        r = r && string_part(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    /* ********************************************************** */
    // LEFT_QUOTE string_parts ? RIGHT_QUOTE ?
    static boolean string_quoted(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "string_quoted")) return false;
        if (!nextTokenIs(b, LEFT_QUOTE)) return false;
        boolean r, p;
        Marker m = enter_section_(b, l, _NONE_);
        r = consumeToken(b, LEFT_QUOTE);
        p = r; // pin = 1
        r = r && report_error_(b, string_quoted_1(b, l + 1));
        r = p && string_quoted_2(b, l + 1) && r;
        exit_section_(b, l, m, r, p, null);
        return r || p;
    }

    // string_parts ?
    private static boolean string_quoted_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "string_quoted_1")) return false;
        string_parts(b, l + 1);
        return true;
    }

    // RIGHT_QUOTE ?
    private static boolean string_quoted_2(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "string_quoted_2")) return false;
        consumeToken(b, RIGHT_QUOTE);
        return true;
    }

    /* ********************************************************** */
    // string_parts RIGHT_QUOTE ?
    static boolean string_unquoted(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "string_unquoted")) return false;
        boolean r;
        Marker m = enter_section_(b);
        r = string_parts(b, l + 1);
        r = r && string_unquoted_1(b, l + 1);
        exit_section_(b, m, null, r);
        return r;
    }

    // RIGHT_QUOTE ?
    private static boolean string_unquoted_1(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "string_unquoted_1")) return false;
        consumeToken(b, RIGHT_QUOTE);
        return true;
    }

    /* ********************************************************** */
    // scripted_variable_reference | boolean | int | float | string | color | block | inline_math
    public static boolean value(PsiBuilder b, int l) {
        if (!recursion_guard_(b, l, "value")) return false;
        boolean r;
        Marker m = enter_section_(b, l, _COLLAPSE_, VALUE, "<value>");
        r = scripted_variable_reference(b, l + 1);
        if (!r) r = boolean_$(b, l + 1);
        if (!r) r = int_$(b, l + 1);
        if (!r) r = float_$(b, l + 1);
        if (!r) r = string(b, l + 1);
        if (!r) r = color(b, l + 1);
        if (!r) r = block(b, l + 1);
        if (!r) r = inline_math(b, l + 1);
        exit_section_(b, l, m, r, false, null);
        return r;
    }

    static final Parser block_item_auto_recover_ = (b, l) -> !nextTokenIsFast(b, AT, BOOLEAN_TOKEN,
        COLOR_TOKEN, COMMENT, FLOAT_TOKEN, INLINE_MATH_START, INT_TOKEN, LEFT_BRACE,
        LEFT_BRACKET, LEFT_QUOTE, PARAMETER_START, PROPERTY_KEY_TOKEN, RIGHT_BRACE, RIGHT_BRACKET, STRING_TOKEN);
    static final Parser normal_conditional_block_auto_recover_ = block_item_auto_recover_;
    static final Parser normal_conditional_block_item_auto_recover_ = block_item_auto_recover_;
    static final Parser property_auto_recover_ = block_item_auto_recover_;
    static final Parser scripted_variable_auto_recover_ = block_item_auto_recover_;
}
