// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public interface ParadoxScriptNormalConditionalBlock extends ParadoxScriptConditionalBlock, ParadoxScriptStatement, ParadoxScriptMemberContainer, ParadoxScriptBoundMemberContainer {

    @Nullable
    ParadoxScriptConditionalExpression getConditionalExpression();

    @NotNull
    List<ParadoxScriptNormalConditionalBlock> getNormalConditionalBlockList();

    @NotNull
    List<ParadoxScriptProperty> getPropertyList();

    @NotNull
    List<ParadoxScriptValue> getValueList();

    @Nullable PsiElement getLeftBound();

    @Nullable PsiElement getRightBound();

    @NotNull List<@NotNull ParadoxScriptStatement> getComponents();

    @NotNull ParadoxScriptNormalConditionalBlock getMemberContainer();

    @NotNull List<@NotNull ParadoxScriptMember> getMembers();

    @NotNull Icon getIcon(@IconFlags int flags);

    @NotNull String getPresentableText();

    @NotNull GlobalSearchScope getResolveScope();

    @NotNull SearchScope getUseScope();

    @NotNull ItemPresentation getPresentation();

}
