// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.core.text.QuotePattern;
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyValue;
import icu.windea.pls.localisation.psi.ParadoxLocalisationRichText;
import icu.windea.pls.localisation.psi.ParadoxLocalisationVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ParadoxLocalisationPropertyValueImpl extends ASTWrapperPsiElement implements ParadoxLocalisationPropertyValue {

    public ParadoxLocalisationPropertyValueImpl(@NotNull ASTNode node) {
        super(node);
    }

    public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
        visitor.visitPropertyValue(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    public @Nullable PsiElement getTokenElement() {
        return ParadoxLocalisationPsiImplUtil.getTokenElement(this);
    }

    @Override
    public @NotNull List<@NotNull ParadoxLocalisationRichText> getRichTextList() {
        return ParadoxLocalisationPsiImplUtil.getRichTextList(this);
    }

    @Override
    public @NotNull QuotePattern getQuotePattern() {
        return ParadoxLocalisationPsiImplUtil.getQuotePattern(this);
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
