package rs.ac.bg.etf.pp1;

import java_cup.runtime.Symbol;

%%

%{
	//ukljucivanje informacije o poziciji tokena
	private Symbol new_symbol(int type) {
		return new Symbol(type,yyline+1,yycolumn);
	}
	
	//ukljucivanje informacije o poziciji tokena
	private Symbol new_symbol(int type, Object value) {
		return new Symbol(type,yyline+1,yycolumn,value);
	}
%}

%cup
%line
%column

%xstate COMMENT

%eofval{
	return new_symbol(sym.EOF);
%eofval}

%%

" "    {}
"\b"   {}
"\t"   {}
"\r\n" {}
"\f"   {}

"program" {return new_symbol(sym.PROG, yytext());}
"print" {return new_symbol(sym.PRINT, yytext());}
"if" {return new_symbol(sym.IF, yytext());}
"else" {return new_symbol(sym.ELSE, yytext());}
"return" {return new_symbol(sym.RETURN, yytext());}
"void" {return new_symbol(sym.VOID, yytext());}
"const" {return new_symbol(sym.CONST, yytext());}
"new" {return new_symbol(sym.NEW, yytext());}
"read" {return new_symbol(sym.READ, yytext());}
"static" {return new_symbol(sym.STATIC, yytext());}
"true" {return new_symbol(sym.BOOLCONST, yytext());}
"false" {return new_symbol(sym.BOOLCONST, yytext());}
"range" {return new_symbol(sym.RANGE, yytext());}



"+" {return new_symbol(sym.PLUS, yytext());}
"-" {return new_symbol(sym.MINUS, yytext());}
"*" {return new_symbol(sym.MULT, yytext());}
"/" {return new_symbol(sym.DIV, yytext());}
"%" {return new_symbol(sym.MOD, yytext());}
"=" {return new_symbol(sym.EQUAL, yytext());}
"==" {return new_symbol(sym.EQ_TO, yytext());}
"!=" {return new_symbol(sym.NEQ_TO, yytext());}
">" {return new_symbol(sym.GR, yytext());}
">=" {return new_symbol(sym.GREQ, yytext());}
"<" {return new_symbol(sym.LE, yytext());}
"<=" {return new_symbol(sym.LEQ, yytext());}
"&&" {return new_symbol(sym.AND, yytext());}
"||" {return new_symbol(sym.OR, yytext());}
"++" {return new_symbol(sym.INC, yytext());}
"--" {return new_symbol(sym.DEC, yytext());}
";" {return new_symbol(sym.SEMI, yytext());}
":" {return new_symbol(sym.COLON, yytext());}
"," {return new_symbol(sym.COMMA, yytext());}
"(" {return new_symbol(sym.LPAREN, yytext());}
")" {return new_symbol(sym.RPAREN, yytext());}
"{" {return new_symbol(sym.LBRACE, yytext());}
"}" {return new_symbol(sym.RBRACE, yytext());}
"[" {return new_symbol(sym.LBRACKET, yytext());}
"]" {return new_symbol(sym.RBRACKET, yytext());}
"." {return new_symbol(sym.DOT, yytext());}


"//" {yybegin(COMMENT);}
<COMMENT> . {yybegin(COMMENT);}
<COMMENT> "\r\n" { yybegin(YYINITIAL);}

[0-9]+ {return new_symbol(sym.NUMCONST,Integer.parseInt(yytext()));}
"'" [^'\n\r'] "'" {return new_symbol(sym.CHARCONST,yytext().charAt(1));} 
[a-zA-Z][a-zA-Z0-9_]* {return new_symbol (sym.IDENT,yytext());}
. {System.err.println("Leksicka greska (" + yytext() + ") u liniji " + (yyline+1));}
