package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.AddExpr;
import rs.ac.bg.etf.pp1.ast.AddTerm;
import rs.ac.bg.etf.pp1.ast.ArrIdent;
import rs.ac.bg.etf.pp1.ast.BoolConst;
import rs.ac.bg.etf.pp1.ast.CharConst;
import rs.ac.bg.etf.pp1.ast.DStatementDesignator;
import rs.ac.bg.etf.pp1.ast.DStatementInc;
import rs.ac.bg.etf.pp1.ast.DesignatorArray;
import rs.ac.bg.etf.pp1.ast.DesignatorFactor;
import rs.ac.bg.etf.pp1.ast.DesignatorIdent;
import rs.ac.bg.etf.pp1.ast.Div;
import rs.ac.bg.etf.pp1.ast.DstatementDec;
import rs.ac.bg.etf.pp1.ast.MethodDeclNoReturn;
import rs.ac.bg.etf.pp1.ast.MethodName;
import rs.ac.bg.etf.pp1.ast.MinusTermExpr;
import rs.ac.bg.etf.pp1.ast.Mult;
import rs.ac.bg.etf.pp1.ast.New;
import rs.ac.bg.etf.pp1.ast.NumConst;
import rs.ac.bg.etf.pp1.ast.NumberConstPrint;
import rs.ac.bg.etf.pp1.ast.Plus;
import rs.ac.bg.etf.pp1.ast.Print;
import rs.ac.bg.etf.pp1.ast.Range;
import rs.ac.bg.etf.pp1.ast.StatementPrintExpr;
import rs.ac.bg.etf.pp1.ast.StatementPrintExprComma;
import rs.ac.bg.etf.pp1.ast.StatementRead;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class CodeGenerator extends VisitorAdaptor {
	
	private boolean enteredDesignatorArray = false;
	private boolean printEntered = false;
	
	private int width;
	
	public CodeGenerator() {

		/********** len **********/
		Obj obj = Tab.find("len");
		obj.setAdr(Code.pc);
		Code.put(Code.enter);
		Code.put(obj.getLevel());
		Code.put(obj.getLocalSymbols().size());
		Code.put(Code.load_n);
		Code.put(Code.arraylength);
		Code.put(Code.exit);
		Code.put(Code.return_);

		/********** chr **********/
		obj = Tab.find("chr");
		obj.setAdr(Code.pc);
		Code.put(Code.enter);
		Code.put(obj.getLevel());
		Code.put(obj.getLocalSymbols().size());
		Code.put(Code.load_n);
		Code.put(Code.exit);
		Code.put(Code.return_);

		/********** ord **********/
		obj = Tab.find("ord");
		obj.setAdr(Code.pc);
		Code.put(Code.enter);
		Code.put(obj.getLevel());
		Code.put(obj.getLocalSymbols().size());
		Code.put(Code.load_n);
		Code.put(Code.exit);
		Code.put(Code.return_);
	}

	private int mainPC;
	
	
	int getMainPc() {
		return mainPC;
	}
	
	@Override
	public void visit(MethodName m) {
		if(m.getI1().equalsIgnoreCase("main")) mainPC = Code.pc;
		m.obj.setAdr(Code.pc);
		Code.put(Code.enter);
		Code.put(m.obj.getLevel());
		Code.put(m.obj.getLocalSymbols().size());
		
		
		
		
	}
	
	@Override
	public void visit(DesignatorArray a) {
		if(a.getParent().getClass() == DesignatorFactor.class) {
			Code.load(a.obj);
		}
		
		
		
	}
	
	@Override
	public void visit(ArrIdent a) {
		
		Code.load(a.obj);
	}
	
	@Override
	public void visit(DesignatorIdent d) {
		if(d.getParent().getClass() == DesignatorFactor.class) {
			Code.load(d.obj);
		}
	}
	
	@Override
	public void visit(DStatementDesignator d) {
		Code.store(d.obj);
	}
	
	@Override
	public void visit(NumConst n) {
		Code.loadConst(n.getN1());
	}
	
	@Override
	public void visit(CharConst c) {
		Code.loadConst(c.getC1());
	}
	
    @Override
    public void visit(BoolConst b) {
    	if(b.getB1().equals("true")) {
    		Code.loadConst(1);
    	}else {
    		Code.loadConst(0);
    	}
    }
	
	
	@Override
	public void visit(MethodDeclNoReturn MethodDeclNoReturn) {
		Code.put(Code.exit);
		Code.put(Code.return_);
		
	}
	
	@Override
	public void visit(New n) { //nakon ovoga se na expr stacku nalazi adresa napravljenog niza;
		Struct type = n.getType().struct;
		if(type == Tab.intType) {
			Code.put(Code.newarray);
			Code.put(1);
		}else {
			Code.put(Code.newarray);
			Code.put(0);
		}
	}
	
	@Override
	public void visit(Range r) {
		int patchAdr = 0;
		int jump = 0;
		Code.put(Code.dup);
		Code.put(Code.newarray);
		Code.put(1);
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		jump = Code.pc;
		Code.loadConst(1);
		Code.put(Code.sub);
		Code.loadConst(0);
		Code.put(Code.dup2);
		//proveri uslov
		Code.putFalseJump(Code.ge, 0);
		patchAdr = Code.pc - 2;
		Code.put(Code.pop);
		Code.put(Code.dup2);
		Code.put(Code.dup);
		Code.put(Code.astore);
		Code.putJump(jump);
		Code.fixup(patchAdr);
		//ovde se skace ako uslov nije zadovoljen i samo se cisti stek (samo se ne sklanja adrssa da bi se to storovalo)
		Code.put(Code.pop);
		Code.put(Code.pop);
		
	}
	
	
	@Override
	public void visit(AddExpr addExpr) {
		if(addExpr.getAddop() instanceof Plus) {
			Code.put(Code.add);
		}else {
			Code.put(Code.sub);
		}
		
	}
	
	
	@Override
	public void visit(AddTerm addTerm) {
		if(addTerm.getMulop() instanceof Mult) {
			Code.put(Code.mul);
		}else if(addTerm.getMulop() instanceof Div) {
			Code.put(Code.div);
		}else {
			Code.put(Code.rem);
		}
	}
	
	
	
	@Override
	public void visit(MinusTermExpr minusTermExpr) {
		Code.put(Code.neg);
		
	}
	
	@Override
	public void visit(DStatementInc dStatementInc) {

		if(dStatementInc.getDesignator() instanceof DesignatorArray) {
			Code.put(Code.dup2);
			Code.load(new Obj(Obj.Elem,dStatementInc.obj.getName(),dStatementInc.obj.getType()));
			Code.loadConst(1);
			Code.put(Code.add);
			Code.store(new Obj(Obj.Elem,dStatementInc.obj.getName(),dStatementInc.obj.getType()));
		}else {
			Code.load(dStatementInc.getDesignator().obj);
			Code.loadConst(1);
			Code.put(Code.add);
			Code.store(dStatementInc.getDesignator().obj);
		}
		

	}
	
	@Override
	public void visit(DstatementDec dStatementDec) {
		if(dStatementDec.getDesignator() instanceof DesignatorArray) {
			Code.put(Code.dup2);
			Code.load(new Obj(Obj.Elem,dStatementDec.obj.getName(),dStatementDec.obj.getType()));
			Code.loadConst(1);
			Code.put(Code.sub);
			Code.store(new Obj(Obj.Elem,dStatementDec.obj.getName(),dStatementDec.obj.getType()));
		}else {
			Code.load(dStatementDec.getDesignator().obj);
			Code.loadConst(1);
			Code.put(Code.sub);
			Code.store(dStatementDec.getDesignator().obj);
		}
	}
	
	@Override
	public void visit(StatementPrintExpr s) {
		if (s.getExpr().struct == Tab.intType || s.getExpr().struct == SemanticPass.boolType) {
			Code.loadConst(5);
			Code.put(Code.print);
		}else if(s.getExpr().struct == Tab.charType){
			Code.loadConst(1);
			Code.put(Code.bprint);
		}else {
			boolean flagAload = true;
			int patchAdr = 0;
			int jmpAdr = 0;
			if(s.getExpr().struct.isRefType() && s.getExpr().struct.getElemType() == Tab.charType) flagAload = false;
			Code.put(Code.dup);
			Code.put(Code.arraylength);
			Code.loadConst(0);
			jmpAdr = Code.pc;
			Code.put(Code.dup2);
			Code.putFalseJump(Code.ne, 0);
			patchAdr = Code.pc - 2;
			Code.put(Code.dup_x2);
			Code.put(Code.pop);
			Code.put(Code.pop);
			Code.put(Code.dup_x1);
			Code.put(Code.pop);
			Code.put(Code.dup2);
			if(flagAload) {
				Code.put(Code.aload);
				Code.loadConst(5);
				Code.put(Code.print);
			} else {
				Code.put(Code.baload);
				Code.loadConst(1);
				Code.put(Code.bprint);
			}
			Code.loadConst(1);
			Code.put(Code.add);
			Code.put(Code.dup_x1);
			Code.put(Code.pop);
			Code.put(Code.dup);
			Code.put(Code.arraylength);
			Code.put(Code.dup_x2);
			Code.put(Code.pop);
			Code.put(Code.dup_x2);
			Code.put(Code.pop);
			Code.putJump(jmpAdr);
			Code.fixup(patchAdr);
			//skidanje onog sto je ostalo na steku
			Code.put(Code.pop); //indeks
			Code.put(Code.pop); //len
			Code.put(Code.pop); //addr niza
			
		}
		
	}
	
	
	@Override
	public void visit(StatementPrintExprComma s) {
		
		Obj con = Tab.find("width");
		System.out.println(con.getAdr() + "ovdeee");
		if (s.getExpr().struct == Tab.intType || s.getExpr().struct == SemanticPass.boolType) {
			Code.put(Code.print);
		}else if(s.getExpr().struct == Tab.charType){
			Code.put(Code.bprint);
		}else {
			boolean flagAload = true;
			int patchAdr = 0;
			int jmpAdr = 0;
			if(s.getExpr().struct.isRefType() && s.getExpr().struct.getElemType() == Tab.charType) flagAload = false;
			Code.put(Code.pop);
			Code.put(Code.dup);
			Code.put(Code.arraylength);
			Code.loadConst(0);
			jmpAdr = Code.pc;
			Code.put(Code.dup2);
			Code.putFalseJump(Code.ne, 0);
			patchAdr = Code.pc - 2;
			Code.put(Code.dup_x2);
			Code.put(Code.pop);
			Code.put(Code.pop);
			Code.put(Code.dup_x1);
			Code.put(Code.pop);
			Code.put(Code.dup2);
			if(flagAload) {
				Code.put(Code.aload);
				Code.loadConst(width);
				Code.put(Code.print);
			} else {
				Code.put(Code.baload);
				Code.loadConst(width);
				Code.put(Code.bprint);
			}
			Code.loadConst(1);
			Code.put(Code.add);
			Code.put(Code.dup_x1);
			Code.put(Code.pop);
			Code.put(Code.dup);
			Code.put(Code.arraylength);
			Code.put(Code.dup_x2);
			Code.put(Code.pop);
			Code.put(Code.dup_x2);
			Code.put(Code.pop);
			Code.putJump(jmpAdr);
			Code.fixup(patchAdr);
			//skidanje onog sto je ostalo na steku
			Code.put(Code.pop);
			Code.put(Code.pop);
			Code.put(Code.pop);
			
		}
		
		printEntered = false;
	}
	
	
	@Override
	public void visit(Print print) {
		printEntered = true;
	}
	
	@Override
	public void visit(NumberConstPrint n) {
		Code.loadConst(n.getN1());
		if(printEntered) {
			width = n.getN1();
		}
		
	}
	
	
	
	@Override
	public void visit(StatementRead r) {
		Obj o = r.getDesignator().obj;
		if (r.getDesignator().obj.getType() == Tab.charType) {
			Code.put(Code.bread);
			
			Code.store(new Obj(Obj.Elem,o.getName(),o.getType()));
		}else {
			Code.put(Code.read);
		}	
			
		Code.store(o);
		
	}
}
