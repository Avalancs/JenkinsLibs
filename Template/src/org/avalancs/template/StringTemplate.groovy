package org.avalancs.template

@Grab('io.pebbletemplates:pebble:3.2.2')
import io.pebbletemplates.pebble.*
import io.pebbletemplates.pebble.loader.StringLoader

class StringTemplate extends Template {
    StringTemplate(Closure<PebbleEngine.Builder> engineConfigurer = null) {
        PebbleEngine.Builder builder = new PebbleEngine.Builder();
        builder.loader(new StringLoader()); // load Pebble template from String, not files
        if(engineConfigurer != null) {
            engineConfigurer.call(builder);
        }
        engine = builder.build();
    }

    String apply(String template, Map<String, Object> tokens) {
        StringWriter writer = new StringWriter();
        engine.getTemplate(template).evaluate(writer, tokens);
        return writer.toString();
    }

    String apply(Map args) {
        return apply(
            args.get('template') as String,
            args.get('tokens') as Map<String, Object>,
        );
    }
}
