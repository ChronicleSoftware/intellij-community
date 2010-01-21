package com.jetbrains.python.validation;

import com.intellij.util.containers.HashSet;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.ParamHelper;

import java.util.Set;

/**
 * Checks for anomalies in parameter lists of function declarations.
 */
public class ParameterListAnnotator extends PyAnnotator {
  @Override
  public void visitPyParameterList(final PyParameterList paramlist) {
    final LanguageLevel languageLevel = ((PyFile)paramlist.getContainingFile()).getLanguageLevel();
    ParamHelper.walkDownParamArray(
      paramlist.getParameters(),
      new ParamHelper.ParamVisitor() {
        Set<String> parameterNames = new HashSet<String>();
        boolean hadPositionalContainer = false;
        boolean hadKeywordContainer = false;
        boolean hadDefaultValue = false;
        boolean hadSingleStar = false;
        boolean hadParamsAfterSingleStar = false;
        @Override
        public void visitNamedParameter(PyNamedParameter parameter, boolean first, boolean last) {
          if (parameterNames.contains(parameter.getName())) {
            markError(parameter, PyBundle.message("ANN.duplicate.param.name"));
          }
          parameterNames.add(parameter.getName());
          if (parameter.isPositionalContainer()) {
            if (hadKeywordContainer) {
              markError(parameter, PyBundle.message("ANN.starred.param.after.kwparam"));
            }
            hadPositionalContainer = true;
          }
          else if (parameter.isKeywordContainer()) {
            hadKeywordContainer = true;
            if (hadSingleStar && !hadParamsAfterSingleStar) {
              markError(parameter, PyBundle.message("ANN.named.arguments.after.star"));
            }
          }
          else {
            if (hadSingleStar) {
              hadParamsAfterSingleStar = true;
            }
            if (hadPositionalContainer && !languageLevel.isPy3K()) {
              markError(parameter, PyBundle.message("ANN.regular.param.after.vararg"));
            }
            else if (hadKeywordContainer) {
              markError(parameter, PyBundle.message("ANN.regular.param.after.keyword"));
            }
            if (parameter.getDefaultValue() != null) {
              hadDefaultValue = true;
            }
            else {
              if (hadDefaultValue && !hadSingleStar && (!languageLevel.isPy3K() || !hadPositionalContainer)) {
                markError(parameter, PyBundle.message("ANN.non.default.param.after.default"));
              }
            }
          }
        }

        @Override
        public void visitSingleStarParameter(PySingleStarParameter param, boolean first, boolean last) {
          hadSingleStar = true;
          if (last) {
            markError(param, PyBundle.message("ANN.named.arguments.after.star"));
          }
        }
      }
    );
  }
}
