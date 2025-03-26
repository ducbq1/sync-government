package org.webflux.helper;

import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

@Slf4j
public class JavascriptHelper {
    public static String evaluate(String content) {
        try (Context context = Context.create("js")) {

            // Evaluate a simple JavaScript expression
//            Value result = context.eval("js", "2 + 3");
//            System.out.println(result.asInt()); // 5
//
//            // Evaluate a more complex JavaScript script
//            String script = "var x = 10; var y = 20; x + y;";
//            Value complexResult = context.eval("js", script);
//            log.debug(complexResult.asString());
//            System.out.println(complexResult.asString());

            return context.eval("js", content).asString();
        }
    }
}
