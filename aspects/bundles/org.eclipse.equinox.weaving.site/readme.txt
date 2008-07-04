This archive contains the AspectJ Equinox Framework Extension
feature (http://www.eclipse.org/equinox/incubator/aspects/). 
It should be used by anyone who cannot make use of the update site for 
the Aspects Equinox Incubator (http://download.eclipse.org/tools/aspectj/dev/update/).

To install it, please unzip it in your eclipse install directory (the same
directory in which you can find eclipse.exe) - it will correctly place the various
components in the right directories below this install directory and next time
you start Eclipse, you will have OSGi/Aspects integration support.

Version 1.1.0
- Bug 161202 "[Aspects] Byte-code Caching for non-IBM VMs" fixed
- Bug 165481 "[Aspects] Can't define aspects using aop.xml" fixed
- Bug 183237 "[aspects] Need to updated bundle names for Aspect bundles" fixed
- Bug 215177 "[aspects] OSGiWeavingAdaptor.weaveClass() has to be adapted to the updated super class signature" fixed
- Bug 215761 "[aspects] org.aspectj.osgi.service.weaving shouldn't depend on org.eclipse.core.runtime" fixed
- Bug 216397 "[aspects] improve footprint of standard caching service" fixed
- Bug 217037 "[aspects] Supplementing does not work properly" fixed
- Bug 217442 "[aspects] Eclipse-Supplement supplements more bundles than it should" fixed

Version 1.0.4
- Folder layout for feature in download archive now correct
- Bug 166777 "[Aspects] No weaving unless configuration area cleared" fixed

Version 1.0.3
- Includes J9 byte-code caching service

Version 1.0.1
- No longer requires replacement org.eclipse.osgi for co-opt model

Bugs
- Bug 166776 "[Aspects] Can't weave fragments"
