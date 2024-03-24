# PebbleTemplate
Jenkins global pipeline library to use [Pebble template engine](https://pebbletemplates.io/) in pipeline builds.

## Example usage
Assuming you named the library `Template` in Jenkins settings, you can use it in your build like this:
```groovy
@Library('Template')
import org.avalancs.template.*

node() {
    StringTemplate template = new StringTemplate()
    
    String output = template.apply('<p>{{something}}</p>', [something: "Test"])
    echo(output) // outputs <p>Test</p>
}
```

If you need to configure the template engine (e.g.: change the characters for the markup to be @@) you can pass a closure like this:

```groovy
StringTemplate template = StringTemplate.withSyntax { syntax ->
    syntax.setPrintOpenDelimiter('@{')      // instead of {{
    syntax.setPrintCloseDelimiter('}@')     // instead of }}
    syntax.setExecuteOpenDelimiter('[[')    // instead of {#
    syntax.setExecuteCloseDelimiter(']]')   // instead of }#
}

String output = template.apply('@{something}@</br>[[ if something == "Test" ]]@{ something }@[[ endif ]]', [something: "Test"])
```

For all options on the builder see `Pebble Engine Settings` [here](https://pebbletemplates.io/wiki/guide/installation/)