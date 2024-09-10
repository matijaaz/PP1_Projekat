package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import java_cup.runtime.virtual_parse_stack;
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class SemanticPass extends VisitorAdaptor {
	boolean errorDetected = false;
	private boolean mainHappened = false;
	static Struct boolType = Tab.find("bool").getType();
	Logger log = Logger.getLogger(getClass());
	int nVars = 0;
	private Struct currentType = Tab.noType;
	private Obj currentMethod = Tab.noObj;

	public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" On line ").append(line);
		log.error(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" On line ").append(line);
		log.info(msg.toString());
	}

	@Override
	public void visit(ProgramName ProgramName) {
		ProgramName.obj = Tab.insert(Obj.Prog, ProgramName.getName(), Tab.noType);
		Tab.openScope();
		//Tab.insert(Obj.Var, "pomocna", Tab.intType);
	}

	@Override
	public void visit(Program Program) {
		if (!mainHappened) {
			report_error("void main() method is not defined", Program);
		}
		nVars = Tab.currentScope.getnVars();
		//System.out.println(nVars);
		Tab.chainLocalSymbols(Program.getProgramName().obj);
		Tab.closeScope();
	}

	@Override
	public void visit(Type Type) {
		Obj obj = Tab.find(Type.getI1());
		if (obj == Tab.noObj) {
			report_error("Type " + Type.getI1() + " not found in symbol table! ", null);
			Type.struct = Tab.noType; 
			currentType = Tab.noType;
		} else {

			if (Obj.Type == obj.getKind()) {
				Type.struct = obj.getType();
				currentType = Type.struct;
			} else {
				report_error("Error: Name " + Type.getI1() + " is not a type!", Type);
				Type.struct = Tab.noType;
				currentType = Tab.noType;
			}
		}

	}

	@Override
	public void visit(ConstDeclNum ConstDeclNum) {
		Obj tmp = Tab.find(ConstDeclNum.getI1());
		if (tmp != Tab.noObj) {
			report_error("Multiple definitions of constant '" + ConstDeclNum.getI1() + "'", ConstDeclNum);
		} else {

			if (Tab.intType.assignableTo(currentType)) {

				tmp = Tab.insert(Obj.Con, ConstDeclNum.getI1(), Tab.intType);
				tmp.setAdr(ConstDeclNum.getN2());
			} else {

				report_error("Forbidden assignment for '" + ConstDeclNum.getI1() + "'", ConstDeclNum);
			}
		}

	}

	@Override
	public void visit(ConstDeclChar ConstDeclChar) {
		Obj tmp = Tab.find(ConstDeclChar.getI1());
		if (tmp != Tab.noObj) {
			report_error("Multiple definitions of constant '" + ConstDeclChar.getI1() + "'", ConstDeclChar);
		} else {

			if (Tab.charType.assignableTo(currentType)) {

				tmp = Tab.insert(Obj.Con, ConstDeclChar.getI1(), Tab.charType);
				tmp.setAdr(ConstDeclChar.getC2());
			} else {

				report_error("Forbidden assignment for '" + ConstDeclChar.getI1() + "'", ConstDeclChar);
			}
		}
	}

	@Override
	public void visit(ConstDeclBool ConstDeclBool) {
		Obj tmp = Tab.find(ConstDeclBool.getIdent());
		if (tmp != Tab.noObj) {
			report_error("Multiple definitions of constant '" + ConstDeclBool.getIdent() + "'", ConstDeclBool);
		} else {

			if (boolType.assignableTo(currentType)) {

				tmp = Tab.insert(Obj.Con, ConstDeclBool.getIdent(), boolType);
				tmp.setAdr((ConstDeclBool.getBool().equals("true")) ? 1 : 0);
			} else {

				report_error("Forbidden assignment for '" + ConstDeclBool.getIdent() + "'", ConstDeclBool);
			}
		}
	}

	@Override
	public void visit(ErrorProneGlobalVarArray ErrorProneGlobalVarArray) {
		Obj tmp = Tab.find(ErrorProneGlobalVarArray.getI1());
		if (tmp != Tab.noObj) {
			report_error("Multiple definitions of variable '" + ErrorProneGlobalVarArray.getI1() + "'",
					ErrorProneGlobalVarArray);
		} else {

			tmp = Tab.insert(Obj.Var, ErrorProneGlobalVarArray.getI1(), new Struct(Struct.Array, currentType));

		}
	}

	@Override
	public void visit(ErrorProneGlobalVarType e) {
		Obj tmp = Tab.find(e.getI1());
		if (tmp != Tab.noObj) {
			report_error("Multiple definitions of variable '" + e.getI1() + "'", e);
		} else {

			tmp = Tab.insert(Obj.Var, e.getI1(), currentType);

		}
	}

	public boolean passed() {
		return !errorDetected;
	}

	@Override
	public void visit(MethodName m) {
		if (m.getI1().equals("main"))
			mainHappened = true;
		currentMethod = Tab.insert(Obj.Meth, m.getI1(), Tab.noType);
		m.obj = currentMethod;
		Tab.openScope();

	}

	@Override
	public void visit(MethodDeclNoReturn MethodDeclNoReturn) {
		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();
		currentMethod = null;
	}

	@Override
	public void visit(FormParElement f) {
		Obj temp = Tab.currentScope.findSymbol(f.getI2());
		if (temp != null) {

			report_error("Multiple definitions of local variable '" + f.getI2() + "'", f);
		} else {

			if (currentMethod.getName().equals("main")) {
				report_error("Main method cannot have formal parameters!", f);
			} else {
				Tab.insert(Obj.Var, f.getI2(), f.getType().struct);
				currentMethod.setLevel(currentMethod.getLevel() + 1);
			}

		}

	}

	@Override
	public void visit(FormParElementArray f) {
		Obj temp = Tab.currentScope.findSymbol(f.getI2());
		if (temp != null) {

			report_error("Multiple definitions of local variable '" + f.getI2() + "'", f);
		} else {

			if (currentMethod.getName().equals("main")) {
				report_error("Main method cannot have formal parameters!", f);
			} else {
				Tab.insert(Obj.Var, f.getI2(), new Struct(Struct.Array, f.getType().struct));
				currentMethod.setLevel(currentMethod.getLevel() + 1);
			}

		}
	}

	@Override
	public void visit(VarDeclParamElement f) {
		Obj temp = Tab.currentScope.findSymbol(f.getI1());
		if (temp != null) {

			report_error("Multiple definitions of local variable '" + f.getI1() + "'", f);
		} else {

			Tab.insert(Obj.Var, f.getI1(), currentType);
		}
	}

	@Override
	public void visit(VarDeclParamArray f) {
		Obj temp = Tab.currentScope.findSymbol(f.getI1());
		if (temp != null) {

			report_error("Multiple definitions of local variable '" + f.getI1() + "'", f);
		} else {

			Tab.insert(Obj.Var, f.getI1(), new Struct(Struct.Array, currentType));
		}
	}

	@Override
	public void visit(DesignatorIdent d) {
		Obj temp = Tab.find(d.getI1());
		if (temp == Tab.noObj) {

			report_error("Name " + d.getI1() + " is not declared! ", d);
		}
		d.obj = temp;
	}

	@Override
	public void visit(DesignatorArray d) {
		Obj temp = Tab.find(d.getArrIdent().getI1());

		if (temp == Tab.noObj) {

			report_error("Name " + d.getArrIdent().getI1() + " is not declared! ", d);
		}
		
		if (!(d.getExpr().struct == Tab.intType)) {
			report_error("Array can only be addresed with int", d);
			temp = Tab.noObj;
			
		}
		
		d.obj = new Obj(Obj.Elem,temp.getName(),temp.getType().getElemType());
		d.obj.setLevel(temp.getLevel());
		d.obj.setAdr(temp.getAdr());
		d.getArrIdent().obj = temp;//videcu ovo posto sam malo koristio u CodeGen
	}

	@Override
	public void visit(NumConst n) {
		n.struct = Tab.intType;
	}

	@Override
	public void visit(BoolConst b) {
		b.struct = boolType;
	}

	@Override
	public void visit(CharConst c) {
		c.struct = Tab.charType;
	}

	@Override
	public void visit(DesignatorFactor Df) {
		Df.struct = Df.getDesignator().obj.getType();
	}

	@Override
	public void visit(New n) {
		if (n.getExpr().struct != Tab.intType) {
			report_error("Int type is expected in the argument of new.", n);
			n.struct = Tab.noType;
		} else {
			
			n.struct = new Struct(Struct.Array, n.getType().struct);
		}

	}

	@Override
	public void visit(ExprParen e) {
		e.struct = e.getExpr().struct;
	}

	@Override
	public void visit(Range n) {
		if (n.getExpr().struct != Tab.intType) {
			report_error("Int type is expected in the argument of range.", n);
			n.struct = Tab.noType;
		} else {

			n.struct = new Struct(Struct.Array, Tab.intType);
		}
	}

	@Override
	public void visit(FactorTerm f) {
		f.struct = f.getFactor().struct;
	}

	@Override
	public void visit(AddTerm d) {
		Struct t = d.getFactor().struct;
		Struct te = d.getTerm().struct;
		//if(d.getLine() == 33) {
		//System.out.println(t.getKind() + " " +  te.getKind() + " " + te.getElemType().getKind());
	    //}
		if(!(t == Tab.intType && te == Tab.intType)) {
			report_error("(AddTerm).Both types need to be int.", d);
			d.struct = Tab.noType;
		}else {
			d.struct = Tab.intType;
		}
		
	}

	@Override
	public void visit(TermExpr t) {
		t.struct = t.getTerm().struct;
	}

	@Override
	public void visit(MinusTermExpr m) {
		if (m.getTerm().struct == Tab.intType) {

			m.struct = Tab.intType;
		}

		else {

			report_error("Int type is expected after -.", m);
			m.struct = Tab.noType;
		}
	}

	@Override
	public void visit(AddExpr d) {
		Struct t = d.getTerm().struct;
		Struct te = d.getExpr().struct;
		
		if(!(t == Tab.intType && te == Tab.intType)) {
			report_error("(AddExpr).Both types need to be int.", d);
			d.struct = Tab.noType;
		}else {
			d.struct = Tab.intType;
		}
	}

	@Override
	public void visit(DStatementDesignator d) {
		d.obj = d.getDesignator().obj;
		Struct t = d.getErrorProneExpr().struct;
		Struct te = d.getDesignator().obj.getType();
		
		
		if (d.getDesignator().obj.getKind() != Obj.Var && d.getDesignator().obj.getKind() != Obj.Elem) {
			report_error("Left side of expression needs to be variable", d);
			
		}else if(!t.compatibleWith(te)) {
			report_error("(Assignement)Incompatible types in assignment.", d);
		}	
				
		

	}

	@Override
	public void visit(DStatementInc d) {
		Obj obj = d.getDesignator().obj;
		d.obj = obj;
		if (!((obj.getKind() == Obj.Var && obj.getType() == Tab.intType) || (obj.getKind() == Obj.Elem && obj.getType() == Tab.intType))) {
			report_error("Variable(int) is expected or element(int) of array.", d);
		} 
			
		}

	@Override
	public void visit(DstatementDec d) {
		Obj obj = d.getDesignator().obj;
		d.obj = obj;
		
			
		if (!((obj.getKind() == Obj.Var && obj.getType() == Tab.intType) || (obj.getKind() == Obj.Elem && obj.getType() == Tab.intType))) {
			report_error("Variable(int) is expected or element(int) of array.", d);
		} 
			
		
		
	}

	@Override
	public void visit(RExpr r) {
		r.struct = r.getExpr().struct;
	}

	@Override
	public void visit(StatementRead r) {
		Obj obj = r.getDesignator().obj;
		if(obj.getKind() == Obj.Var && obj.getType().isRefType()) {
			report_error("(StatementRead) Read variable, '" + r.getDesignator().obj.getName() + "', is of wrong type.",
					r);
			return;
		}
		boolean flag = ((obj.getKind() == Obj.Var ||  obj.getKind() == Obj.Elem)
				&& (obj.getType() == Tab.charType || obj.getType() == Tab.intType || obj.getType() == boolType));

		if (!flag) {
				report_error("(StatementRead) Read variable, '" + r.getDesignator().obj.getName() + "', is of wrong type.",
						r);
		}

	}

	@Override
	public void visit(StatementPrintExpr r) {
		Struct obj = r.getExpr().struct;
		boolean flag1 = (obj == Tab.charType || obj == Tab.intType || obj == boolType);

		if (!flag1) {
			if (!(obj.isRefType() && (obj.getElemType() == Tab.charType || obj.getElemType() == Tab.intType
					|| obj.getElemType() == boolType))) {
				report_error(
						"(StatementPrintExpr) Incorrect expr in print in method '" + currentMethod.getName() + "'.", r);
			}
		}

	}

	@Override
	public void visit(StatementPrintExprComma r) {
		Struct obj = r.getExpr().struct;
		boolean flag1 = (obj == Tab.charType || obj == Tab.intType || obj == boolType);

		if (!flag1) {
			if (!(obj.isRefType() && (obj.getElemType() == Tab.charType || obj.getElemType() == Tab.intType
					|| obj.getElemType() == boolType))) {
				report_error(
						"(StatementPrintExpr) Incorrect expr in print in method '" + currentMethod.getName() + "'.", r);
			}
		}
	}
}
