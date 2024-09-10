package rs.ac.bg.etf.pp1.loggers;
import java_cup.runtime.Symbol;

public class SyntaxErrorMJLogger extends MJLogger<Symbol> {

    public enum SyntaxErrorKind {
        INV_GLOBAL_VAR_DECL, INV_CLASS_INHERITANCE, INV_CLASS_FIELD_DECL, INV_FORM_PAR, INV_ASSIGNMENT, INV_IF_STMT_COND, FATAL_ERROR, INV_DECL,
    }

    public SyntaxErrorMJLogger() {
        super(MJLoggerKind.ERROR_LOGER, "Syntax error");
    }

    @Override
    protected String messageBody(Symbol loggedObject, Object... context) {
        SyntaxErrorKind syntaxErrorKind = (SyntaxErrorKind) context[0];
        String message = switch (syntaxErrorKind) {
            case INV_GLOBAL_VAR_DECL -> "Invalid global variable declaration. Parsing continued";
            case INV_CLASS_INHERITANCE -> "Invalid class inheritance declaration. Parsing continued";
            case INV_CLASS_FIELD_DECL -> "Invalid class field declaration. Parsing continued";
            case INV_FORM_PAR -> "Invalid formal parameter declaration. Parsing continued";
            case INV_ASSIGNMENT -> "Invalid assignment statement. Parsing continued";
            case INV_IF_STMT_COND -> "Invalid if-statement condition. Parsing continued";
            case FATAL_ERROR -> "Fatal syntax error. Parsing aborted";
            case INV_DECL -> "Invalid declaration. Parsing continued";
        };
        return message;
    }

}