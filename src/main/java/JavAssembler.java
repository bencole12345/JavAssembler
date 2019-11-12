import ast.structure.CompilationUnit;
import parser.ParserWrapper;

import java.io.IOException;

public class JavAssembler {

    public static void main(String[] args) throws IOException {
        if (args.length == 1) {
            String path = args[0];
            CompilationUnit unit = ParserWrapper.parseFile(path);
            int x = 1;
        }
    }

}
