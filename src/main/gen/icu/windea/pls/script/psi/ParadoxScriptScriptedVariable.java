// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.lang.psi.ParadoxTypedElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.model.ParadoxType;
import javax.swing.Icon;

public interface ParadoxScriptScriptedVariable extends ParadoxScriptNamedElement, ParadoxTypedElement, StubBasedPsiElement<ParadoxScriptScriptedVariableStub> {

  @NotNull
  ParadoxScriptScriptedVariableName getScriptedVariableName();

  @Nullable
  ParadoxScriptValue getScriptedVariableValue();

  @NotNull Icon getIcon(@IconFlags int flags);

  @Nullable String getName();

  @NotNull ParadoxScriptScriptedVariable setName(@NotNull String name);

  @Nullable PsiElement getNameIdentifier();

  int getTextOffset();

  @Nullable String getValue();

  @Nullable String getUnquotedValue();

  @NotNull ParadoxType getType();

  boolean isEquivalentTo(@NotNull PsiElement another);

  @NotNull ItemPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
