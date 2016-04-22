/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (C) 1998-2004  Gerwin Klein <lsf@jflex.de>                    *
 * All rights reserved.                                                    *
 *                                                                         *
 * This program is free software; you can redistribute it and/or modify    *
 * it under the terms of the GNU General Public License. See the file      *
 * COPYRIGHT for more information.                                         *
 *                                                                         *
 * This program is distributed in the hope that it will be useful,         *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of          *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           *
 * GNU General Public License for more details.                            *
 *                                                                         *
 * You should have received a copy of the GNU General Public License along *
 * with this program; if not, write to the Free Software Foundation, Inc., *
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA                 *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */


   
/* $Author: deissenb $
   $Revision: 5448 $ */

package org.conqat.lib.simulink.builder;

import java_cup.runtime.*;
%%

%class MDLScanner
%cup
%unicode
%line
%column
%public

%{

    private StringBuilder string = new StringBuilder();
        
    /* To create a new java_cup.runtime.Symbol with information about
       the current token, the token will have no value in this
       case. */
    private Symbol symbol(int type) {
        //System.out.println("- '"+yytext()+"' ("+type+")");  
        return new Symbol(type, yyline, yycolumn);
    }
    
    /* Also creates a new java_cup.runtime.Symbol with information
       about the current token, but this object has a value. */
    private Symbol symbol(int type, Object value) {
        //System.out.println("- '"+yytext()+"' ("+type+")");  
        return new Symbol(type, yyline, yycolumn, value);
    }
%}


/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = [ \t\f]

/* comments */
EndOfLineComment = "#" {InputCharacter}* {LineTerminator}?


/* Identifiers. */
Identifier = {IdentifierStart} ({IdentifierStart} | [0-9] | [-.])*
IdentifierStart = [a-zA-Z_#$@]

/* integer literals */
DecIntegerLiteral = (-? 0 |-? [1-9][0-9]*) [uU]?
DecLongLiteral    = {DecIntegerLiteral} [lL]

HexIntegerLiteral = 0 [xX] 0* {HexDigit} {1,8} [uU]?
HexLongLiteral    = 0 [xX] 0* {HexDigit} {1,16} [lL]
HexDigit          = [0-9a-fA-F]

OctIntegerLiteral = 0+ [1-3]? {OctDigit} {1,15} [uU]?
OctLongLiteral    = 0+ 1? {OctDigit} {1,21} [lL]
OctDigit          = [0-7]
    
/* floating point literals */        
FloatLiteral  = ({FLit1}|{FLit2}|{FLit3}) {Exponent}? [fF]
DoubleLiteral = ({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = -? [0-9]+ \. [0-9]* 
FLit2    = -? \. [0-9]+ 
FLit3    = -? [0-9]+ 
Exponent = [eE] [+-]? [0-9]+

/* string and character literals */
StringCharacter = [^\n\"\\]
SingleCharacter = [^\n\'\\]

%state STRING

%%

<YYINITIAL> {
 
  /* boolean literals */
  "on"                         { return symbol(SymbolConstants.BOOLEAN_LITERAL, yytext()); }
  "off"                        { return symbol(SymbolConstants.BOOLEAN_LITERAL, yytext()); }
  
  
  /* separators */
  "{"                            { return symbol(SymbolConstants.LBRACE); }
  "}"                            { return symbol(SymbolConstants.RBRACE); }
  "["                            { return symbol(SymbolConstants.LBRACK); }
  "]"                            { return symbol(SymbolConstants.RBRACK); }
  ","                            { return symbol(SymbolConstants.COMMA); }
  ";"                            { return symbol(SymbolConstants.SEMICOLON); }
  
  
  
  /* string literal */
  \"                             { yybegin(STRING); string.setLength(0); }

  
  /* numeric literals */
  
   
  {DecIntegerLiteral}            { return symbol(SymbolConstants.INT_LITERAL, yytext()); }
  {DecLongLiteral}               { return symbol(SymbolConstants.INT_LITERAL, yytext()); }
  
  {HexIntegerLiteral}            { return symbol(SymbolConstants.INT_LITERAL, yytext()); }
  {HexLongLiteral}               { return symbol(SymbolConstants.INT_LITERAL, yytext()); }
 
  {OctIntegerLiteral}            { return symbol(SymbolConstants.INT_LITERAL, yytext()); }  
  {OctLongLiteral}               { return symbol(SymbolConstants.INT_LITERAL, yytext()); }
  
  {FloatLiteral}                 { return symbol(SymbolConstants.FLOAT_LITERAL, yytext()); }
  {DoubleLiteral}                { return symbol(SymbolConstants.FLOAT_LITERAL, yytext()); }
  {DoubleLiteral}[dD]            { return symbol(SymbolConstants.FLOAT_LITERAL, yytext()); }
  
  /* comments */
  {EndOfLineComment}{LineTerminator}+  { /* ignore */ }

  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }

  /* Preserve new lines. */
  {LineTerminator}+              { return symbol(SymbolConstants.NEWLINE, yytext());}

  /* identifiers */ 
  {Identifier}                   { return symbol(SymbolConstants.IDENTIFIER, yytext());}  
}

<STRING> {
  \"                             { yybegin(YYINITIAL); 
                                   return symbol(SymbolConstants.STRING_LITERAL, string.toString()); }
  
  {StringCharacter}+             { string.append( yytext() ); }
  
  /* escape sequences */
  "\\b"                          { string.append( "\\b" ); }
  "\\t"                          { string.append( "\\t" ); }
  "\\n"                          { string.append( "\\n" ); }
  "\\f"                          { string.append( "\\f" ); }
  "\\r"                          { string.append( "\\r" ); }
  "\\\""                         { string.append( "\\\"" ); }
  "\\'"                          { string.append( "\\'"); }
  "\\\\"                         { string.append( "\\\\" ); }

  {LineTerminator}               { string.append( yytext() ); }
                                 
  
  /* error cases */
  \\.                            { throw new RuntimeException("Illegal Escape :"+yytext()+" at line "+yyline); }
}



/* error fallback */
.|\n                             { throw new RuntimeException("Illegal Character :"+yytext()+" at line "+yyline); }
<<EOF>>                          { return symbol(SymbolConstants.EOF); }
