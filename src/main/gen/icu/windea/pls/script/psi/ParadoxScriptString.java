// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralValue;
import icu.windea.pls.script.reference.ParadoxScriptStringPropertyPsiReference;

public interface ParadoxScriptString extends ParadoxScriptStringValue, PsiLiteralValue {

  @Nullable
  PsiElement getQuotedStringToken();

  @Nullable
  PsiElement getStringToken();

  @NotNull
  String getValue();

  @NotNull
  PsiElement setValue(@NotNull String name);

  @NotNull
  ParadoxScriptStringPropertyPsiReference getReference();

  @NotNull
  String getStringValue();

  boolean isQuoted();

}
