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

package org.conqat.lib.simulink.targetlink;

import java_cup.runtime.*;
%%

%class TargetlinkDataScanner
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

WhiteSpace = {LineTerminator} | [ \t\f]

Struct = "struct"

/* Identifiers. */
Identifier = {IdentifierStart} ({IdentifierStart} | [0-9] | [.] | [*] )*
IdentifierStart = [a-zA-Z_#$@]

/* integer literals */
DecIntegerLiteral = -? 0 |-? [1-9][0-9]* 
DecLongLiteral    = {DecIntegerLiteral} [lL]

HexIntegerLiteral = 0 [xX] 0* {HexDigit} {1,8}
HexLongLiteral    = 0 [xX] 0* {HexDigit} {1,16} [lL]
HexDigit          = [0-9a-fA-F]

OctIntegerLiteral = 0+ [1-3]? {OctDigit} {1,15}
OctLongLiteral    = 0+ 1? {OctDigit} {1,21} [lL]
OctDigit          = [0-7]
    
/* floating point literals */        
FloatLiteral  = ({FLit1}|{FLit2}|{FLit3}) {Exponent}? [fF]
DoubleLiteral = ({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = -? [0-9]+ \. [0-9]* 
FLit2    = -? \. [0-9]+ 
FLit3    = -? [0-9]+ 
Exponent = [\^eE] [+-]? [0-9]+

/* string and character literals 
StringLiteral = "'"~"'" */
StringCharacter = [^\n']

%state STRING

%%

<YYINITIAL> {
 
 
  
  
  /* separators */
  "("                            { return symbol(SymbolConstants.LPAREN); }
  ")"                            { return symbol(SymbolConstants.RPAREN); }
  "["                            { return symbol(SymbolConstants.LBRACK); }
  "]"                            { return symbol(SymbolConstants.RBRACK); }
  "{"                            { return symbol(SymbolConstants.LBRACE); }
  "{{"                            { return symbol(SymbolConstants.DLBRACE); }
  "}"                            { return symbol(SymbolConstants.RBRACE); }
  "}}"                            { return symbol(SymbolConstants.DRBRACE); }
  ","                            { return symbol(SymbolConstants.COMMA); }
  ";"                            { return symbol(SymbolConstants.SEMICOLON); }
  "+"                            { return symbol(SymbolConstants.PLUS); }
  "*"                            { return symbol(SymbolConstants.MULT); }
  "-"                            { return symbol(SymbolConstants.MINUS); }
  "/"                            { return symbol(SymbolConstants.DIV); }
  
  
  /* struct literal */
  {Struct}                       { return symbol(SymbolConstants.STRUCT); }

  
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

  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }

  /* identifiers */ 
  {Identifier}                   { return symbol(SymbolConstants.IDENTIFIER, yytext());}  
  
  /* string literal */
  "'"                            { yybegin(STRING); string.setLength(0); }
}

<STRING> {
  "'"                            { yybegin(YYINITIAL); 
                                   return symbol(SymbolConstants.STRING_LITERAL, string.toString()); }
  
  {StringCharacter}+             { string.append( yytext() ); }
  
  /* escape sequences */
  "''"                          { string.append( "'" ); }                                 
  
  /* error cases */
  \\.                            { throw new RuntimeException("Illegal Escape :"+yytext()+" at line "+yyline+", column "+yycolumn); }
  {LineTerminator}               { throw new RuntimeException("Unterminated string literal :"+yytext()+ " at line "+yyline+", column "+yycolumn); }
}



/* error fallback */
.|\n                             { throw new RuntimeException("Illegal Character :"+yytext()+" at line "+yyline+", column "+yycolumn); }
<<EOF>>                          { return symbol(SymbolConstants.EOF); }
