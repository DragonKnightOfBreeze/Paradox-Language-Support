// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.core.psi.ParadoxScriptedVariableReference;
import com.intellij.openapi.util.Iconable.IconFlags;
import icu.windea.pls.core.references.ParadoxScriptedVariablePsiReference;
import javax.swing.Icon;

public interface ParadoxScriptInlineMathScriptedVariableReference extends ParadoxScriptInlineMathFactor, ParadoxScriptedVariableReference {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxScriptInlineMathScriptedVariableReference setName(@NotNull String name);

  @NotNull
  ParadoxScriptedVariablePsiReference getReference();

}
