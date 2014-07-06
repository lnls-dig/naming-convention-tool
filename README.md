# Overview
This project includes all Naming Convention Tool sources.

All projects are Eclipse projects and are configured to use and to be build with maven. Projects are interdependent.

## Projects
- NamingConventionTool
    Main web-base application to manage naming convention of a particle accelerator facility.  

- NamingConventionTool-JAXB  
	Provides JAXB implementation of POJOs and Resource definitions used by NamingConventionTool webservice NamingConventionTool-Client.

- NamingConventionTool-Client  
	The client api to access the NamingConventionTool webservice. It provides the ability to retrieve names data registered in NamingConventionTool.
	
# Build Projects

All projects are configured to be build by maven. The projects extend the ess-java-config pom.xml, which has to be available in the maven repository, before RBAC can be build. You can get it here:  
[https://bitbucket.org/ess_ics/ess-java-config](https://bitbucket.org/ess_ics/ess-java-config)  
First, install the ess-java-config by running command **mvn install**. This will install the ess-java-config pom.xml into your maven repository. Now you can build the Naming Convention Tool code. You can do that by executing **mvn install** in each individual project, or execute **mvn install** in the Naming Convention Tool root. The root pom.xml contains references to all Naming Convention Tool projects and will build and install them in proper order. The output of the build is located in the *target* folder of each individual project.
