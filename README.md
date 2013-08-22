# MIDEaaS

[MIDEaaS](http://cored.cs.tut.fi/#mideaas) (Mobile IDE as a Service) is an experimental web-based environment for creating Vaadin web apps.

It's still in a research state. Not to be used for anything real.

The current version requires Vaadin 7.1+ and Java 7.

Lots of features a still missing.


## QuickStart

To run MIDEaaS, you need Java 7 SDK and [Maven](http://maven.apache.org/).

1. Edit config file at `mideaas-app/src/main/resources/mideaas.properties` to setup the project dirs etc. See the file for more instructions.
2. In the mideaas root folder run `mvn install`.
3. To run MIDEaaS, go to `mideaas-app` directory and type `mvn jetty:run`. Now it should be available `http://localhost:8080/mideaas/`.

4. (Optional) To use VisualDesigner, you must run the VisualDesigner ([here](https://collab.nokia.com/svn/SME)) somewhere and configure its address in `mideaas-app/src/main/resources/mideaas.properties`.

---

[![Build Status](https://travis-ci.org/ahn/mideaas.png)](https://travis-ci.org/ahn/mideaas)
