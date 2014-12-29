##Support library for the Bonaparte persistence noSQL DSL

The classes defined within this repository are supporting the generated noSQL entity classes of the DSL.

The projects have the following contents:
  * noSQL-base:	Parent project, initiates the build of the child projects.
  * noSQL-bom:	Bill of materials / dependency management. Import this into other projects.
  * noSQL-aerospike:	(work in progress) support for the Aerospike noSQL database.
  * noSQL-aerospike-test:	(work in progress) test cases for the noSQL-aerospike project.

###Building

This project uses maven3 as a build tool. Just run

    (cd noSQL-base && mvn install)

