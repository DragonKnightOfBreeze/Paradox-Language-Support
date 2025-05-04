// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.cwt.psi.CwtElementTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import icu.windea.pls.cwt.psi.*;
import icu.windea.pls.cwt.psi.util.CwtPsiImplUtil;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.SearchScope;

public class CwtOptionCommentRootImpl extends ASTWrapperPsiElement implements CwtOptionCommentRoot {

  public CwtOptionCommentRootImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  @Nullable
  public CwtOption getOption() {
    return findChildByClass(CwtOption.class);
  }

  @Override
  @Nullable
  public CwtValue getOptionValue() {
    return findChildByClass(CwtValue.class);
  }

  @Override
  public @NotNull ItemPresentation getPresentation() {
    return CwtPsiImplUtil.getPresentation(this);
  }

  @Override
  public @NotNull SearchScope getUseScope() {
    return CwtPsiImplUtil.getUseScope(this);
  }

}
