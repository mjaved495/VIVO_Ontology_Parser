# VIVO_Ontology_Parser

Muhammad Javed
Cornell University
Nov 20, 2015

- VIVO Ontology extends a number of higher-level ontologies such as BIBO, OBO, FOAF, VCARD, EVENT etc.
- The purpose of this project is to parse the vivo ontology file into its separate ontology modules.

Step 1: Convert .owl (or any other format) ontology file into .nt file.

Step 2: Manually Identify the namespaces you would like to capture and link them to an ontology module.
This is done by 
- printing out a the list of namepsaces first (method: browseNamespaces(Model model)). 
- exploring the master .nt file mannually and linking different namespaces to their specific ontology modules. (in java class Namespace.java)

Step 3: Running the process.
The ontology modules will be saved in resources/nt  or resources/rdf folder (depending upon the selected output format)
The unmapped triples will be saved in the resources/unmapped folder.

Step 4: Look at the unmapped triples manually (in file resources/unmapped/unmapped.txt) and add them in their specific ontology modules manually.
for example 
<http://purl.org/net/OCRe/study_design.owl> <http://www.w3.org/2000/01/rdf-schema#label> "OCRe study design" .
goes to the OCRE_SD.nt ontology module file.

Step 5: If needed, convert .nt files into .rdf using rdf2rdf-1.0.1-2.3.1.jar
Run the Java Application RDFModuleGenerator.java in package edu.cornell.vivo.rdfmodulebuilder


