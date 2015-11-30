package edu.cornell.vivo.rdfmodulebuilder;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

public class RDFModuleGenerator {

	public static void main(String[] args) {
		String inputFolder = "/Users/mj495/Documents/GitHub/VIVO_Ontology_Parser/vivoontologyparserproject/resources/nt";
		String outputFolder= "/Users/mj495/Documents/GitHub/VIVO_Ontology_Parser/vivoontologyparserproject/resources/final-rdf/";
		RDFModuleGenerator builder = new RDFModuleGenerator();

		File folder = new File(inputFolder);
		if(folder.isDirectory()){
			File files[] = folder.listFiles();
			for(File file: files){
				if(Files.getFileExtension(file.getAbsolutePath()).equals("nt")){
					String outputFile = outputFolder+Files.getNameWithoutExtension(file.getName())+".rdf";
					//System.out.println(outputFolder+outputFile+".rdf");
					builder.buildRDFModules(file.getAbsolutePath(), outputFile);
				}
			}
		}
	}

	public void buildRDFModules(String inputPath, String outputPath){
		String filePath =  "resources/jar/rdf2rdf-1.0.1-2.3.1.jar"; //where your jar is located.
		try {
			Runtime.getRuntime().exec(" java -jar " +filePath+" "+ inputPath + " "+ outputPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
