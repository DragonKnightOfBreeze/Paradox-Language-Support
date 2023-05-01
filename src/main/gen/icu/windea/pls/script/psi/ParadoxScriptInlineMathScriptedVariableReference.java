// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.core.psi.ParadoxScriptedVariableReference;
import icu.windea.pls.core.psi.ParadoxParameterAwareElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.core.expression.ParadoxDataType;
import icu.windea.pls.core.references.ParadoxScriptedVariablePsiReference;
import javax.swing.Icon;

public interface ParadoxScriptInlineMathScriptedVariableReference extends ParadoxScriptInlineMathFactor, ParadoxScriptedVariableReference, ParadoxParameterAwareElement {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @Nullable
  String getName();

  @NotNull
  ParadoxScriptInlineMathScriptedVariableReference setName(@NotNull String name);

  @Nullable
  String getValue();

  @Nullable
  ParadoxScriptedVariablePsiReference getReference();

  @NotNull
  ParadoxDataType getType();

  @NotNull
  String getExpression();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
