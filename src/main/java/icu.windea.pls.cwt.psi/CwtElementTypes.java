package icu.windea.pls.cwt.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import icu.windea.pls.cwt.psi.impl.*;

public interface CwtElementTypes {
    IElementType BLOCK = new CwtElementType("BLOCK");
    IElementType BOOLEAN = new CwtElementType("BOOLEAN");
    IElementType DOC_COMMENT = new CwtElementType("DOC_COMMENT");
    IElementType FLOAT = new CwtElementType("FLOAT");
    IElementType INT = new CwtElementType("INT");
    IElementType OPTION_COMMENT = new CwtOptionCommentElementType("OPTION_COMMENT");
    IElementType PROPERTY = new CwtElementType("PROPERTY");
    IElementType PROPERTY_KEY = new CwtElementType("PROPERTY_KEY");
    IElementType ROOT_BLOCK = new CwtElementType("ROOT_BLOCK");
    IElementType STRING = new CwtElementType("STRING");
    IElementType VALUE = new CwtElementType("VALUE");

    IElementType OPTION = new CwtElementType("OPTION");
    IElementType OPTION_KEY = new CwtElementType("OPTION_KEY");

    IElementType BOOLEAN_TOKEN = new CwtTokenType("BOOLEAN_TOKEN");
    IElementType COMMENT = new CwtTokenType("COMMENT");
    IElementType DOC_COMMENT_TOKEN = new CwtTokenType("DOC_COMMENT_TOKEN");
    IElementType EQUAL_SIGN = new CwtTokenType("EQUAL_SIGN");
    IElementType FLOAT_TOKEN = new CwtTokenType("FLOAT_TOKEN");
    IElementType INT_TOKEN = new CwtTokenType("INT_TOKEN");
    IElementType LEFT_BRACE = new CwtTokenType("LEFT_BRACE");
    IElementType NOT_EQUAL_SIGN = new CwtTokenType("NOT_EQUAL_SIGN");
    IElementType OPTION_COMMENT_START = new CwtTokenType("OPTION_COMMENT_START");
    IElementType OPTION_COMMENT_TOKEN = new CwtTokenType("OPTION_COMMENT_TOKEN");
    IElementType PROPERTY_KEY_TOKEN = new CwtTokenType("PROPERTY_KEY_TOKEN");
    IElementType RIGHT_BRACE = new CwtTokenType("RIGHT_BRACE");
    IElementType STRING_TOKEN = new CwtTokenType("STRING_TOKEN");

    IElementType OPTION_KEY_TOKEN = new CwtTokenType("OPTION_KEY_TOKEN");

    class Factory {
        public static PsiElement createElement(ASTNode node) {
            IElementType type = node.getElementType();
            if (type == BLOCK) {
                return new CwtBlockImpl(node);
            } else if (type == BOOLEAN) {
                return new CwtBooleanImpl(node);
            } else if (type == DOC_COMMENT) {
                return new CwtDocCommentImpl(node);
            } else if (type == FLOAT) {
                return new CwtFloatImpl(node);
            } else if (type == INT) {
                return new CwtIntImpl(node);
            } else if (type == OPTION_COMMENT) {
                return new CwtOptionCommentImpl(node);
            } else if (type == PROPERTY) {
                return new CwtPropertyImpl(node);
            } else if (type == PROPERTY_KEY) {
                return new CwtPropertyKeyImpl(node);
            } else if (type == ROOT_BLOCK) {
                return new CwtRootBlockImpl(node);
            } else if (type == STRING) {
                return new CwtStringImpl(node);
            } else if (type == OPTION) {
                return new CwtOptionImpl(node);
            } else if (type == OPTION_KEY) {
                return new CwtOptionKeyImpl(node);
            }
            throw new AssertionError("Unknown element type: " + type);
        }
    }
}
