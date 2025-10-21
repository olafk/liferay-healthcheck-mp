Run an automated checklist for your systems health. This checklist can help to detect unusual or unsupported setups, as well as systems restored in a different environment. Notable features include:

* Running VerifyProperties for your version at runtime
* Check the age of your installation, takes your preference of LTS or not into account (some of these features DXP-specific, desired age configurable)
* User Password Hashing checks, including a configurable sample amount of users to be checked for using an up-to-date hashing configuration strength
* Checks for correct configuration of boolean and numeric properties
* Detects if a backup is restored on a different system - e.g. when PRD is restored in UAT, which might require an admin to make sure to reconfigure other aspects too (e.g. Client Extensions, Data Providers, Web Hooks).
* Detect proper configuration of redirection rules for accessed host names
* Shows the remaining Premium Support period (DXP-only) and alerts you at a configurable time before it runs out and others
* Allows you to specify the minimum number of nodes that you expect to be live in a healthy cluster
* Check validity of certificates of systems that you connect to - e.g. through Client Extensions, Data Providers, Web Hooks or a manually configured additional list of hosts
* Optional "relaxed" settings for development systems (host names need to be explicitly specified for these tests to run) to make your development experience smoother
* Detects if your search index is out-of-date and needs to be reindexed (by sampling the number of indexed users)
* Detects if you're running an unsupported configuration (e.g. HSQL or Elasticsearch-Sidecar)
* Detects if your server started with incomplete components (smoke-test, beta) and needs a restart to be fully available
* New in 1.0.5: Detects if ImageMagick is configured and working properly to enable AVIF and WEBP image formats, including Adaptive Images and Previews
* New in 1.0.6: Relaxed settings default to "localhost", assuming that a system accessed under this name is a development system where comfort is more important. Can be configured in System- and Instance-Settings
* New in 1.0.6: Fragments are validated for potential SPA-related problems. Includes optional whitelistings through checksum and JS-comments.
* New in 1.0.6: Supports the Jakarta-EE release 2025-Q3
* Bugfix in 1.0.6: Ignoring expired TLS-certificates now only applies to a specific certificate's not-valid-after, no longer to all future certificates for the same domain