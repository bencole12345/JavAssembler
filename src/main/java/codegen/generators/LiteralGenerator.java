package codegen.generators;

import ast.literals.*;
import codegen.CodeEmitter;

public class LiteralGenerator {

    public static void compileLiteralValue(LiteralValue literal, CodeEmitter emitter) {
        if (literal instanceof IntLiteral) {
            compileIntLiteral((IntLiteral) literal, emitter);
        } else if (literal instanceof ShortLiteral) {
            compileShortLiteral((ShortLiteral) literal, emitter);
        } else if (literal instanceof ByteLiteral) {
            compileByteLiteral((ByteLiteral) literal, emitter);
        } else if (literal instanceof LongLiteral) {
            compileLongLiteral((LongLiteral) literal, emitter);
        } else if (literal instanceof FloatLiteral) {
            compileFloatLiteral((FloatLiteral) literal, emitter);
        } else if (literal instanceof DoubleLiteral) {
            compileDoubleLiteral((DoubleLiteral) literal, emitter);
        } else if (literal instanceof BooleanLiteral) {
            compileBooleanLiteral((BooleanLiteral) literal, emitter);
        } else if (literal instanceof CharLiteral) {
            compileCharLiteral((CharLiteral) literal, emitter);
        }
    }

    private static void compileIntLiteral(IntLiteral literal, CodeEmitter emitter) {
        emitter.emitLine("i32.const " + literal.getValue());
    }

    private static void compileShortLiteral(ShortLiteral literal, CodeEmitter emitter) {
        emitter.emitLine("i32.const " + literal.getValue());
    }

    private static void compileByteLiteral(ByteLiteral literal, CodeEmitter emitter) {
        emitter.emitLine("i32.const " + literal.getValue());
    }

    private static void compileLongLiteral(LongLiteral literal, CodeEmitter emitter) {
        emitter.emitLine("i64.const " + literal.getValue());
    }

    private static void compileFloatLiteral(FloatLiteral literal, CodeEmitter emitter) {
        emitter.emitLine("f32.const " + literal.getValue());
    }

    private static void compileDoubleLiteral(DoubleLiteral literal, CodeEmitter emitter) {
        emitter.emitLine("f64.const " + literal.getValue());
    }

    private static void compileBooleanLiteral(BooleanLiteral literal, CodeEmitter emitter) {
        int value = literal.getValue() ? 1 : 0;
        emitter.emitLine("i32.const " + value);
    }

    private static void compileCharLiteral(CharLiteral literal, CodeEmitter emitter) {
        emitter.emitLine("i32.const " + literal.getValue());
    }
}
