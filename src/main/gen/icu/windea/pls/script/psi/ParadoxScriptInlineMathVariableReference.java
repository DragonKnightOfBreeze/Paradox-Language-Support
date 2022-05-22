// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import icu.windea.pls.script.reference.ParadoxScriptVariableReferenceReference;
import javax.swing.Icon;

public interface ParadoxScriptInlineMathVariableReference extends ParadoxScriptInlineMathFactor, IParadoxScriptVariableReference {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxScriptInlineMathVariableReference setName(@NotNull String name);

  @NotNull
  ParadoxScriptVariableReferenceReference getReference();

}
