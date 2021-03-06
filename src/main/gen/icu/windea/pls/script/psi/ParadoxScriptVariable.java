// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import javax.swing.Icon;

public interface ParadoxScriptVariable extends ParadoxScriptNamedElement, StubBasedPsiElement<ParadoxScriptVariableStub> {

  @NotNull
  ParadoxScriptVariableName getVariableName();

  @Nullable
  ParadoxScriptVariableValue getVariableValue();

  @NotNull
  String getName();

  @NotNull
  ParadoxScriptVariable setName(@NotNull String name);

  @NotNull
  PsiElement getNameIdentifier();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @Nullable
  String getValue();

  @Nullable
  String getUnquotedValue();

}
