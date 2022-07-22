// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import icu.windea.pls.model.ParadoxValueType;
import javax.swing.Icon;

public interface ParadoxScriptParameter extends ParadoxScriptValue, IParadoxScriptParameter {

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

  @NotNull
  ParadoxValueType getValueType();

}
