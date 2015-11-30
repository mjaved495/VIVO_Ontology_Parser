# VIVO_Ontology_Parser

Step 1: Manually Identify the namespaces you would like to capture and link them to an ontology module.
This is done by 
1) printing out a the list of namepsaces first (method: browseNamespaces(Model mdoel)). 
2) exploring the master .nt file mannually and linking different namespaces to their specific ontology modules. (in java class Namespace.java)

Step 2: Running the process.
The ontology modules will be saved in resources/nt  or resources/rdf folder (depending upon the selected output format)
The unmapped triples will be saved in the resources/unmapped folder.

Step 3: 
Look at the unmapped triples manually (in file resources/unmapped/unmapped.txt) and add them in their specific ontology modules manually.
for example 
<http://purl.org/net/OCRe/study_design.owl> <http://www.w3.org/2000/01/rdf-schema#label> "OCRe study design" .
goes to the OCRE_SD.nt ontology module file.

Step 4: 
If needed, convert .nt files into .rdf using rdf2rdf-1.0.1-2.3.1.jar
Run the Java Application RDFModuleGenerator.java in package edu.cornell.vivo.rdfmodulebuilder


