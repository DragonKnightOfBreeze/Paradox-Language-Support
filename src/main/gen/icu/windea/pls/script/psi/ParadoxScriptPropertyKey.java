// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.script.reference.ParadoxScriptPropertyKeyReference;

public interface ParadoxScriptPropertyKey extends PsiElement {

  @Nullable
  PsiElement getPropertyKeyId();

  @Nullable
  PsiElement getQuotedPropertyKeyId();

  @NotNull
  String getValue();

  @NotNull
  ParadoxScriptPropertyKey setValue(@NotNull String value);

  @NotNull
  ParadoxScriptPropertyKeyReference getReference();

}
