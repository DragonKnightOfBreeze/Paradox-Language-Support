// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.cwt.psi.CwtValue;
import icu.windea.pls.cwt.psi.CwtVisitor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public abstract class CwtValueImpl extends ASTWrapperPsiElement implements CwtValue {

    public CwtValueImpl(@NotNull ASTNode node) {
        super(node);
    }

    public void accept(@NotNull CwtVisitor visitor) {
        visitor.visitValue(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof CwtVisitor) accept((CwtVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    public @NotNull Icon getIcon(@IconFlags int flags) {
        return CwtPsiImplUtil.getIcon(this, flags);
    }

    @Override
    public @NotNull String getName() {
        return CwtPsiImplUtil.getName(this);
    }

    @Override
    public @NotNull String getValue() {
        return CwtPsiImplUtil.getValue(this);
    }

    @Override
    public @NotNull CwtValue setValue(@NotNull String value) {
        return CwtPsiImplUtil.setValue(this, value);
    }

    @Override
    public @NotNull CwtValue setContent(@NotNull String content, @NotNull TextRange range) {
        return CwtPsiImplUtil.setContent(this, content, range);
    }

    @Override
    public @NotNull String getPresentableText() {
        return CwtPsiImplUtil.getPresentableText(this);
    }

    @Override
    public @NotNull GlobalSearchScope getResolveScope() {
        return CwtPsiImplUtil.getResolveScope(this);
    }

    @Override
    public @NotNull SearchScope getUseScope() {
        return CwtPsiImplUtil.getUseScope(this);
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
        return CwtPsiImplUtil.getPresentation(this);
    }

    @Override
    public @NotNull String toString() {
        return CwtPsiImplUtil.toString(this);
    }

}
