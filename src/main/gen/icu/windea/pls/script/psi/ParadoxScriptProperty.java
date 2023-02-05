// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;

import icu.windea.pls.lang.model.*;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.core.psi.ParadoxTypedElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.SmartPsiElementPointer;
import icu.windea.pls.core.expression.ParadoxDataType;
import icu.windea.pls.core.psi.ParadoxParameter;
import java.util.Map;
import javax.swing.Icon;
import kotlin.Pair;

public interface ParadoxScriptProperty extends ParadoxScriptNamedElement, ParadoxTypedElement, ParadoxScriptDefinitionElement, StubBasedPsiElement<ParadoxScriptPropertyStub> {

  @NotNull
  ParadoxScriptPropertyKey getPropertyKey();

  @Nullable
  ParadoxScriptValue getPropertyValue();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxScriptProperty setName(@NotNull String name);

  @Nullable
  PsiElement getNameIdentifier();

  @Nullable
  String getValue();

  int getDepth();

  @Nullable
  ParadoxScriptBlock getBlock();

  @Nullable
  ParadoxDataType getType();

  @Nullable
  String getConfigExpression();

  @NotNull
  String getExpression();

  @Nullable
  String getPathName();

  @NotNull
  String getOriginalPathName();

  @NotNull
  Map<String, ParadoxParameterInfo> getParameters();

  @Nullable
  ItemPresentation getPresentation();

  boolean isEquivalentTo(@NotNull PsiElement another);

}
