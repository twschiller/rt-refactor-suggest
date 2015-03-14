# Code Clone Detectors #

In the future, the plugin will expose an API for attaching custom code clone detectors.

The plugin currently comes packaged with 3 code clone detectors: JDDC, Checkstyle, and Simian. JDDC and Simian are highly customizable, and can be configured via Eclipse's Preferences window.


## Checkstyle ##

[Checkstyle](http://checkstyle.sourceforge.net/) is a tool for building / enforcing Java coding standards; it includes a [duplicate code](http://checkstyle.sourceforge.net/config_duplicates.html) detector that performs a strict textual comparison between lines of code.

Custom code clone detectors can plug into the framework via a API --- the Simian detector (described below) uses this API.

## JCCD ##

The http://jccd.sourceforge.net/ Java Code Clone Detection API is a customizable AST-based pipeline for building clone detectors.

Users can select which [AST generalization options](http://jccd.sourceforge.net/operators.html) to perform on the AST fragments during detection. For example, the ` AcceptAdditiveOperators ` operation ignores the difference between plus and minus operators when comparing code.

## Simian ##

[Simian](http://www.harukizaemon.com/simian/index.html) is a tool for finding similarities in text files, including Java source code files.

**Simian is only free for non-commercial projects.** See [Simian's licensing page](http://www.harukizaemon.com/simian/get_it_now.html) for more information.