// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.lang.psi.ParadoxDefinitionElement;
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement;
import com.intellij.psi.StubBasedPsiElement;
import icu.windea.pls.script.psi.stubs.ParadoxScriptPropertyStub;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.IElementType;
import javax.swing.Icon;

public interface ParadoxScriptProperty extends ParadoxScriptNamedElement, ParadoxScriptMember, ParadoxDefinitionElement, PsiPresentableTextAwareElement, StubBasedPsiElement<ParadoxScriptPropertyStub> {

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

  @NotNull IElementType getIElementType();

  boolean isEquivalentTo(@Nullable PsiElement another);

  @NotNull String getPresentableText();

  @NotNull ParadoxScriptElementPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
