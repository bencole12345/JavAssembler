package codegen.generators;

import ast.literals.*;
import codegen.CodeEmitter;

public class LiteralGenerator {

    private static LiteralGenerator INSTANCE;

    public static LiteralGenerator getInstance() {
        if (INSTANCE == null)
            INSTANCE = new LiteralGenerator();
        return INSTANCE;
    }

    private CodeEmitter emitter;

    private LiteralGenerator() {}

    public void setCodeEmitter(CodeEmitter emitter) {
        this.emitter = emitter;
    }

    public void compileLiteralValue(LiteralValue literal) {
        if (literal instanceof IntLiteral) {
            compileIntLiteral((IntLiteral) literal);
        } else if (literal instanceof ShortLiteral) {
            compileShortLiteral((ShortLiteral) literal);
        } else if (literal instanceof ByteLiteral) {
            compileByteLiteral((ByteLiteral) literal);
        } else if (literal instanceof LongLiteral) {
            compileLongLiteral((LongLiteral) literal);
        } else if (literal instanceof FloatLiteral) {
            compileFloatLiteral((FloatLiteral) literal);
        } else if (literal instanceof DoubleLiteral) {
            compileDoubleLiteral((DoubleLiteral) literal);
        } else if (literal instanceof BooleanLiteral) {
            compileBooleanLiteral((BooleanLiteral) literal);
        } else if (literal instanceof CharLiteral) {
            compileCharLiteral((CharLiteral) literal);
        }
    }

    private void compileIntLiteral(IntLiteral literal) {
        emitter.emitLine("i32.const " + literal.getValue());
    }

    private void compileShortLiteral(ShortLiteral literal) {
        emitter.emitLine("i32.const " + literal.getValue());
    }

    private void compileByteLiteral(ByteLiteral literal) {
        emitter.emitLine("i32.const " + literal.getValue());
    }

    private void compileLongLiteral(LongLiteral literal) {
        emitter.emitLine("i64.const " + literal.getValue());
    }

    private void compileFloatLiteral(FloatLiteral literal) {
        emitter.emitLine("f32.const " + literal.getValue());
    }

    private void compileDoubleLiteral(DoubleLiteral literal) {
        emitter.emitLine("f64.const " + literal.getValue());
    }

    private void compileBooleanLiteral(BooleanLiteral literal) {
        int value = literal.getValue() ? 1 : 0;
        emitter.emitLine("i32.const " + value);
    }

    private void compileCharLiteral(CharLiteral literal) {
        emitter.emitLine("i32.const " + literal.getValue());
    }
}
