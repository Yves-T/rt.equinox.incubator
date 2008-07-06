This is an experimental bundle that uses -showWeaveInfo and a custom message
handler to help with testing aspects. When a bundle is woven the weaving
messages are saved. The JUnit testcase author my then use assertions to determine
whether or not certain classes have been woven.