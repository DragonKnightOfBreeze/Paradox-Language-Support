// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.localisation.psi.ParadoxLocalisationTypes.*;
import icu.windea.pls.localisation.psi.*;
import com.intellij.openapi.util.Iconable.IconFlags;
import icu.windea.pls.localisation.reference.ParadoxLocalisationCommandScopeReference;
import javax.swing.Icon;

public class ParadoxLocalisationCommandScopeImpl extends ParadoxLocalisationNamedElementImpl implements ParadoxLocalisationCommandScope {

  public ParadoxLocalisationCommandScopeImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitCommandScope(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getCommandScopeId() {
    return notNullChild(findChildByType(COMMAND_SCOPE_ID));
  }

  @Override
  @NotNull
  public Icon getIcon(@IconFlags int flags) {
    return ParadoxLocalisationPsiImplUtil.getIcon(this, flags);
  }

  @Override
  @NotNull
  public String getName() {
    return ParadoxLocalisationPsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public ParadoxLocalisationCommandScope setName(@NotNull String name) {
    return ParadoxLocalisationPsiImplUtil.setName(this, name);
  }

  @Override
  @NotNull
  public PsiElement getNameIdentifier() {
    return ParadoxLocalisationPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  @NotNull
  public ParadoxLocalisationCommandScopeReference getReference() {
    return ParadoxLocalisationPsiImplUtil.getReference(this);
  }

  @Override
  @Nullable
  public ParadoxLocalisationCommandIdentifier getPrevIdentifier() {
    return ParadoxLocalisationPsiImplUtil.getPrevIdentifier(this);
  }

  @Override
  @Nullable
  public ParadoxLocalisationCommandIdentifier getNextIdentifier() {
    return ParadoxLocalisationPsiImplUtil.getNextIdentifier(this);
  }

}
