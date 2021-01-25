package net.curiosprogrammer.javaexperiments.compiler;

import java.io.File;
import java.io.IOException;
import javax.tools.ToolProvider;

public class JavaCompiler {

    public static void main(String[] args) throws IOException {
        // see https://www.logicbig.com/tutorials/core-java-tutorial/java-se-compiler-api/java-compiler-api-intro.html
        final javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        long start = System.currentTimeMillis();
        int result = compiler.run(null, System.out, null,
                                  new File("src/main/java/net/curiosprogrammer/javaexperiments/compiler/JavaCompiler.java").getAbsolutePath());
        long end = System.currentTimeMillis();
        if (result == 0) {
            System.out.println("compilation done");
            // 500-600 ms on my machine
            System.out.println(end - start + " ms");
        }
    }
}
