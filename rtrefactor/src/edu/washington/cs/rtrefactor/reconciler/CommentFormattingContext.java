package edu.washington.cs.rtrefactor.reconciler;

 /*******************************************************************************
2  * Copyright (c) 2000, 2007 IBM Corporation and others.
3  * All rights reserved. This program and the accompanying materials
4  * are made available under the terms of the Eclipse Public License v1.0
5  * which accompanies this distribution, and is available at
6  * http://www.eclipse.org/legal/epl-v10.html
7  *
8  * Contributors:
9  * IBM Corporation - initial API and implementation
10  *******************************************************************************/

 import org.eclipse.jface.text.formatter.FormattingContext;

 import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

 /**
  * Formatting context for the comment formatter.
  *
  * @since 3.0
  */
 public class CommentFormattingContext extends FormattingContext {

     /*
      * @see org.eclipse.jface.text.formatter.IFormattingContext#getPreferenceKeys()
      */
     public String[] getPreferenceKeys() {
         return new String[] {
                 DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_BLOCK_COMMENT,
                 DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_JAVADOC_COMMENT,
                 DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_LINE_COMMENT,
                 DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_HEADER,
                 DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_SOURCE,
                 DefaultCodeFormatterConstants.FORMATTER_COMMENT_INDENT_PARAMETER_DESCRIPTION,
                 DefaultCodeFormatterConstants.FORMATTER_COMMENT_INDENT_ROOT_TAGS,
                 DefaultCodeFormatterConstants.FORMATTER_COMMENT_INSERT_NEW_LINE_FOR_PARAMETER,
                 DefaultCodeFormatterConstants.FORMATTER_COMMENT_INSERT_EMPTY_LINE_BEFORE_ROOT_TAGS,
                 DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH,
                 DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_BLOCK_COMMENT,
                 DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES_IN_JAVADOC_COMMENT,
                 DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_HTML }; }


     /*
      * @see org.eclipse.jface.text.formatter.IFormattingContext#isBooleanPreference(java.lang.String)
      */
     public boolean isBooleanPreference(String  key) {
         return !key.equals(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH);
     }

     /*
      * @see org.eclipse.jface.text.formatter.IFormattingContext#isIntegerPreference(java.lang.String)
      */
     public boolean isIntegerPreference(String  key) {
         return key.equals(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH);
     }
 }
