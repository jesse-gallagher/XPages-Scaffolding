Building the Framework
======================

The framework is a Maven project that uses Tycho, and there are a few requirements beyond just Maven for creating a build environment.

IBM Update Site
---------------

The first requirement for building is the [IBM Domino Update Site for Build Management](http://www.openntf.org/main.nsf/project.xsp?r=project/IBM%20Domino%20Update%20Site%20for%20Build%20Management) from OpenNTF. This project contains a p2 update site with the base XPages artifacts used as dependencies. To use it, extract the contents of the UpdateSite.zip file contained in the download and modify your Maven settings file to use a property named `notes-platform` containing a `file://` URL pointing to this extracted path. If you don't currently have a Maven settings file, create a file named `~/.m2/settings.xml` with content like this, modified for your extracted path:

    <?xml version="1.0"?>
    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
        <profiles>
            <profile>
                <id>main</id>
                <properties>
                    <notes-platform>file:///Users/jesse/Documents/Java/IBM/UpdateSite</notes-platform>
                </properties>
            </profile>
        </profiles>
        <activeProfiles>
            <activeProfile>main</activeProfile>
        </activeProfiles>
    </settings>

OpenNTF Domino API
------------------

The second requirement is the OpenNTF Domino API. To build the framework, the most straightforward method of getting ODA in a usable format is, currently, to download and `mvn install` it from the source, [available on GitHub](https://github.com/OpenNTF/org.openntf.domino).

Eclipse
-------

If opening the framework source in Eclipse, then Eclipse will need to be told about the project dependencies in its active Target Platform (via Preferences &rarr; Plug-in Development &rarr; Target Platform). It will need the IBM Update Site from above as well as ODA, an update site for which can be found, after building it, in `domino/org.openntf.domino.update/target/site` in that project.

Additionally, there is a folder in the framework repository containing additional plug-in dependencies, which can be added via `framework-target-platform/target/repository`.
