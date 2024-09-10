package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class MJParserTest {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void main(String[] args) throws Exception {
		
		Logger log = Logger.getLogger(MJParserTest.class);
		
		Reader br = null;
		try {
			File sourceCode = new File("test/test301.mj");
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());
			
			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);
			
			MJParser p = new MJParser(lexer);
	        Symbol s = p.parse();  //pocetak parsiranja
	        
	        Program prog = (Program)(s.value); 
			// ispis sintaksnog stabla
			log.info(prog.toString(""));
			log.info("===================================");
			
			if (!p.errorDetected) {
				
				Tab.init();
				Obj boolObj = Tab.insert(Obj.Type, "bool", new Struct(Struct.Bool));
				boolObj.setLevel(-1);
				boolObj.setAdr(-1);
	
				SemanticPass semanticCheck = new SemanticPass();
				
				prog.traverseBottomUp(semanticCheck);
				log.info("===================================");
				Tab.dump();
				
				if (semanticCheck.passed()) {
					// File
					File objFile = new File("test/program.obj");
		        	log.info("Generating bytecode file: " + objFile.getAbsolutePath());
		        	if (objFile.exists()) objFile.delete();
		        	// Code generation...
		        	CodeGenerator codeGenerator = new CodeGenerator();
		        	prog.traverseBottomUp(codeGenerator);
		        	Code.dataSize = semanticCheck.nVars;
		        	Code.mainPc = codeGenerator.getMainPc();
		        	Code.write(new FileOutputStream(objFile));
					log.info("Parsing ended successfully! Bytecode generated.");
				}else {
					
					log.error("Semantic ERRORS detected! Bytecode not generated.");
				}
			} 
			
			else {
				
				log.error("Syntax errors detected, aborting...");
			}
			
			// ispis prepoznatih programskih konstrukcija
			//RuleVisitor v = new RuleVisitor();
			//prog.traverseBottomUp(v); 
	      
			//log.info(" Print count calls = " + v.printCallCount);

			//log.info(" Deklarisanih promenljivih ima = " + v.varDeclCount);
			
		} 
		finally {
			if (br != null) try { br.close(); } catch (IOException e1) { log.error(e1.getMessage(), e1); }
		}

	}
	
	
}
