package parser;

import ast.types.AccessModifier;

public class AccessModifierVisitor extends JavaFileBaseVisitor<AccessModifier> {

    @Override
    public AccessModifier visitAccessModifier(JavaFileParser.AccessModifierContext ctx) {
        AccessModifier modifier = AccessModifier.DEFAULT;
        if (ctx == null)
            return modifier;
        switch (ctx.modifier.getType()) {
            case JavaFileParser.PUBLIC:
                modifier = AccessModifier.PUBLIC;
                break;
            case JavaFileParser.PRIVATE:
                modifier = AccessModifier.PRIVATE;
                break;
            case JavaFileParser.PROTECTED:
                modifier = AccessModifier.PROTECTED;
        }
        return modifier;
    }
}
