// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat;
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormatText;
import icu.windea.pls.localisation.psi.ParadoxLocalisationVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ParadoxLocalisationTextFormatImpl extends ParadoxLocalisationRichTextImpl implements ParadoxLocalisationTextFormat {

    public ParadoxLocalisationTextFormatImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
        visitor.visitTextFormat(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @Nullable
    public ParadoxLocalisationTextFormatText getTextFormatText() {
        return PsiTreeUtil.getChildOfType(this, ParadoxLocalisationTextFormatText.class);
    }

    @Override
    public @Nullable PsiElement getIdElement() {
        return ParadoxLocalisationPsiImplUtil.getIdElement(this);
    }

    @Override
    public @NotNull Icon getIcon(@IconFlags int flags) {
        return ParadoxLocalisationPsiImplUtil.getIcon(this, flags);
    }

    @Override
    public @Nullable String getName() {
        return ParadoxLocalisationPsiImplUtil.getName(this);
    }

    @Override
    public @NotNull ParadoxLocalisationTextFormat setName(@NotNull String name) {
        return ParadoxLocalisationPsiImplUtil.setName(this, name);
    }

    @Override
    public @NotNull String getPresentableText() {
        return ParadoxLocalisationPsiImplUtil.getPresentableText(this);
    }

    @Override
    public @Nullable PsiReference getReference() {
        return ParadoxLocalisationPsiImplUtil.getReference(this);
    }

    @Override
    public @NotNull PsiReference @NotNull [] getReferences() {
        return ParadoxLocalisationPsiImplUtil.getReferences(this);
    }

    @Override
    public @NotNull GlobalSearchScope getResolveScope() {
        return ParadoxLocalisationPsiImplUtil.getResolveScope(this);
    }

    @Override
    public @NotNull SearchScope getUseScope() {
        return ParadoxLocalisationPsiImplUtil.getUseScope(this);
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
        return ParadoxLocalisationPsiImplUtil.getPresentation(this);
    }

    @Override
    public @NotNull String toString() {
        return ParadoxLocalisationPsiImplUtil.toString(this);
    }

}
