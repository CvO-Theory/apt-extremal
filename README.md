Petri Net Synthesis with extremal regions
=========================================

This repositories contains the following additional modules for
[APT](https://github.com/CvO-Theory/apt):

- `regular_overapproximate`: Given a language (as a regular expression), this
  module produces the minimal Petri net whose language contains the given
  language.

- `lts_overapproximate`: Given a lts, this module produces the minimal Petri net
  whose reachability graph contains the lts.

In contrast to the synthesise-functionality available in APT, these modules can
produce unbounded Petri nets. For example, the regular expression `a+b` can be
solved exactly via unbounded Petri nets, but not with bounded Petri nets.

All these modules use the [polco
library](http://www.csb.ethz.ch/tools/software/polco.html) for finding the
extremal rays of a polyhedral cone. Due to the size of this library, these
modules are not part of APT itself, but provided as external modules.

Using these modules
-------------------

When running the `jar` target of the `build.xml` file, for example via `ant
jar`, two jar files are created.

- `apt-extremal.jar` contains the full contents of `apt.jar`.
- `apt-extremal-light.jar` depends on the file `lib/apt.jar`.

Both JARs use the normal module system of APT. Its use is explained in [APT's
README.md](https://github.com/CvO-Theory/apt/blob/master/README.md).
