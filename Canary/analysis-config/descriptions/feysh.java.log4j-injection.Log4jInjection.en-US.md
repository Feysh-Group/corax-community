# Remote code injection in Log4j

Log4j versions prior to 2.16.0 are subject to a remote code execution vulnerability via the ldap JNDI parser.
As per [Apache's Log4j security guide](https://logging.apache.org/log4j/2.x/security.html): Apache Log4j2 <=2.14.1 JNDI features used in configuration, log messages, and parameters do not protect against attacker controlled LDAP and other JNDI related endpoints. An attacker who can control log messages or log message parameters can execute arbitrary code loaded from LDAP servers when message lookup substitution is enabled. From log4j 2.16.0, this behavior has been disabled by default.

Log4j version 2.15.0 contained an earlier fix for the vulnerability, but that patch did not disable attacker-controlled JNDI lookups in all situations. For more information, see the `Updated advice for version 2.16.0` section of this advisory.

# Impact

Logging untrusted or user controlled data with a vulnerable version of Log4J may result in Remote Code Execution (RCE) against your application. This includes untrusted data included in logged errors such as exception traces, authentication failures, and other unexpected vectors of user controlled input.

# Affected versions

Any Log4J version prior to v2.15.0 is affected to this specific issue.

The v1 branch of Log4J which is considered End Of Life (EOL) is vulnerable to other RCE vectors so the recommendation is to still update to 2.16.0 where possible.

## Security releases

Additional backports of this fix have been made available in versions 2.3.1, 2.12.2, and 2.12.3

## Affected packages

Only the `org.apache.logging.log4j:log4j-core` package is directly affected by this vulnerability. The `org.apache.logging.log4j:log4j-api` should be kept at the same version as the `org.apache.logging.log4j:log4j-core` package to ensure compatability if in use.

## Solution:

## Updated advice for version 2.16.0

The Apache Logging Services team provided updated mitigation advice upon the release of version 2.16.0, which [disables JNDI by default and completely removes support for message lookups](https://logging.apache.org/log4j/2.x).
Even in version 2.15.0, lookups used in layouts to provide specific pieces of context information will still recursively resolve, possibly triggering JNDI lookups. This problem is being tracked as [CVE-2021-45046](https://nvd.nist.gov/vuln/detail/CVE-2021-45046). More information is available on the [GitHub Security Advisory for CVE-2021-45046](https://github.com/advisories/GHSA-7rjr-3q55-vv33).

Users who want to avoid attacker-controlled JNDI lookups but cannot upgrade to 2.16.0 must [ensure that no such lookups resolve to attacker-provided data and ensure that the the JndiLookup class is not loaded](https://issues.apache.org/jira/browse/LOG4J2-3221).

Please note that Log4J v1 is End Of Life (EOL) and will not receive patches for this issue. Log4J v1 is also vulnerable to other RCE vectors and we recommend you migrate to Log4J 2.16.0 where possible.