// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.IElementType;
import icu.windea.pls.lang.psi.ParadoxDefinitionElement;
import icu.windea.pls.script.psi.stubs.ParadoxScriptPropertyStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public interface ParadoxScriptProperty extends ParadoxScriptNamedElement, ParadoxScriptMember, ParadoxDefinitionElement, StubBasedPsiElement<ParadoxScriptPropertyStub> {

  @Nullable ParadoxScriptBlock getMemberContainer();

  @Nullable List<@NotNull ParadoxScriptMember> getMembers();

  @Nullable ParadoxScriptBlock getBlock();

  @NotNull
  ParadoxScriptPropertyKey getPropertyKey();

  @Nullable
  ParadoxScriptValue getPropertyValue();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull ParadoxScriptProperty setName(@NotNull String name);

  @Nullable PsiElement getNameIdentifier();

  @Nullable String getValue();

  @NotNull String getPresentableText();

  @NotNull IElementType getIElementType();

  boolean isEquivalentTo(@NotNull PsiElement another);

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
