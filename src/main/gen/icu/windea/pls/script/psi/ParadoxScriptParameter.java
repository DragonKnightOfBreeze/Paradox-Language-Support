// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import icu.windea.pls.script.reference.ParadoxParameterReference;
import javax.swing.Icon;

public interface ParadoxScriptParameter extends IParadoxScriptParameter {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @Nullable
  String getName();

  @NotNull
  ParadoxScriptParameter setName(@NotNull String name);

  @Nullable
  PsiElement getNameIdentifier();

  int getTextOffset();

  @NotNull
  String getValue();

  @Nullable
  String getDefaultValue();

  //WARNING: getValueType(...) is skipped
  //matching getValueType(ParadoxScriptParameter, ...)
  //methods are not found in ParadoxScriptPsiImplUtil

  @Nullable
  ParadoxParameterReference getReference();

}
