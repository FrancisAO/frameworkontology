package com.frameworkontology.sparql.shell;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import com.frameworkontology.sparql.service.OntologyService;

/**
 * Offers various commands for querying an in-memory ontology.
 * 
 * @author Francis Opoku
 */
@ShellComponent
public class RepoCommands {

	private static final Logger LOG = LoggerFactory.getLogger(RepoCommands.class);
	private File queryFile;
	private OntologyService ontologyService;

	public RepoCommands(OntologyService ontologyService) {
		this.ontologyService = ontologyService;
	}

	@ShellMethod("Set the ontology to query against")
	public String setOntology(String path) {
		return ontologyService.setOntology(path);
	}

	@ShellMethod("Set Sparql-Query-File ")
	public String setQueryFile(String path) {
		File queryFile = new File(path);
		if (!queryFile.exists() || !queryFile.canRead()) {
			return "Sparql-Query-File does not exist or reading is not allowed.";
		}

		this.queryFile = queryFile;

		return "File accepted";

	}

	@ShellMethod("Executes Sparql-Query from previously 'set-file' command.")
	public String execute() {
		if (queryFile == null) {
			return "You have to run the command 'set-file' first.";
		}

		String query = readQueryFile(queryFile.getPath());
		if (query == null) {
			return "Error occured.";
		}
		return ontologyService.query(query);

	}

	private String readQueryFile(String path) {
		String query = "";
		try {
			String lineSeparator = System.lineSeparator();
			query = Files.readAllLines(Paths.get(path)).stream().reduce("", (s1, s2) -> {
				return s1 + lineSeparator + s2;
			});
			LOG.info("Sparql-Query: {}{}", lineSeparator, query);
		} catch (IOException e) {
			return null;
		}
		return query;
	}

}
