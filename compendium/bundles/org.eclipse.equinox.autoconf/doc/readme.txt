******************************************************************
* Implementation of Auto Configuration Spec in R4 MEG.
******************************************************************

Current implementation is not sophicificated at all.

The classes in the package org.eclipse.equinox.autoconf.parser
are implemented referring org.eclipse.equinox.metatype plugin.
This needs library of equinox.metatype plugin jar which a patch is applied
to version 1.0.0 (as of 2007.Feb.19) in lib/ directory.
The patch is stored in doc/ directory. 
  
This implementatin must be refined.

Although the implementation passed TCK provided by OSGi alliance ( as of 2006 Oct 10th),
remember that the TCK checks only several (5) tests for Auto Configuration
(very simple ones).

The initial implementation is inmature.

 