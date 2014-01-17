# MIDEaaS

[MIDEaaS](http://cored.cs.tut.fi/#mideaas) (Mobile IDE as a Service) is an experimental web-based environment for creating Vaadin web apps.

It's still in a research state. Not to be used for anything real. See  [cored.cs.tut.fi](http://cored.cs.tut.fi) for related information.

The current version requires Vaadin 7.1+ and Java 7.

Lots of features a still missing.

Source code license: [Apache 2.0](https://raw.github.com/ahn/mideaas/master/LICENSE)

## QuickStart

To run MIDEaaS, you need Java 7 JDK and [Maven](http://maven.apache.org/). For all the Java features to work you need to set JAVA_HOME to point to a **JDK**, not JRE. Or if you use Eclipse, configure the project to use JDK ([instructions](http://stackoverflow.com/a/4440223)).

When the prerequisites are set, do the following:

1. Edit config file at `mideaas-app/src/main/resources/mideaas.properties` to setup the project dirs etc. See the file for more instructions.
2. In the mideaas root folder run `mvn install`.
3. To run MIDEaaS, go to `mideaas-app` directory and type `mvn jetty:run`. Now it should be available `http://localhost:8080/mideaas/`.

4. (Optional) To use VisualDesigner, you must run the VisualDesigner ([here](https://collab.nokia.com/SME/browser/sme/VisualDesigner)) somewhere and configure its address in `mideaas-app/src/main/resources/mideaas.properties`.

---

[![Build Status](https://travis-ci.org/ahn/mideaas.png)](https://travis-ci.org/ahn/mideaas)
