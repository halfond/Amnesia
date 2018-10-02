/*
 * Created on Sep 20, 2005
 *
 * Copyright 2004
 * Georgia Tech Research Corporation
 * Atlanta, GA  30332-0415
 * All Rights Reserved
 * 
 */
package amnesia.lexer;

import java.util.List;

public interface Lexer {

   public abstract List lexQuery(String s) throws SQLLexerException;

   public abstract String getLexedQueryString();
}