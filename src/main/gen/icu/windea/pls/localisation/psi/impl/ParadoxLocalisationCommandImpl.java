// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommand;
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommandArgument;
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommandText;
import icu.windea.pls.localisation.psi.ParadoxLocalisationVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ParadoxLocalisationCommandImpl extends ParadoxLocalisationRichTextImpl implements ParadoxLocalisationCommand {

    public ParadoxLocalisationCommandImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
        visitor.visitCommand(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor) visitor);
        else super.accept(visitor);
    }

    @Override
    @Nullable
    public ParadoxLocalisationCommandText getCommandText() {
        return PsiTreeUtil.getChildOfType(this, ParadoxLocalisationCommandText.class);
    }

    @Override
    public @Nullable ParadoxLocalisationCommandArgument getArgumentElement() {
        return ParadoxLocalisationPsiImplUtil.getArgumentElement(this);
    }

    @Override
    public @NotNull Icon getIcon(@IconFlags int flags) {
        return ParadoxLocalisationPsiImplUtil.getIcon(this, flags);
    }

    @Override
    public @NotNull String getPresentableText() {
        return ParadoxLocalisationPsiImplUtil.getPresentableText(this);
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
