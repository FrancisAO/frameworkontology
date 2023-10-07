package com.frameworkontology.sparql.shell;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.table.TableModelBuilder;
import org.springframework.shell.table.TableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.Table;
import org.springframework.shell.table.TableBuilder;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

import com.frameworkontology.sparql.repo.InMemoryRepoFactory;

@ShellComponent
public class RepoCommands {
	
	private InMemoryRepoFactory repoFactory;
	private SailRepository repo;

	private static final Logger LOG = LoggerFactory.getLogger(RepoCommands.class);
	private File queryFile;
	
	public RepoCommands(InMemoryRepoFactory repoFactory) {
		this.repoFactory = repoFactory;
	}
	
	@ShellMethod("Set the ontology to query against")
	public String setOntology(String path) {
		File f = new File(path);
		if(!f.exists() || !f.canRead() || !f.canWrite()) {
			return "Ontology does not exist or reading is not allowed.";
		}
		repo = repoFactory.createRepo();
		try {
			addOntologyDataToRepo(f);
		} catch (RDFParseException | RepositoryException | IOException e) {
			return e.getLocalizedMessage();
		}
		return "In memory repo created.";
	}

	private void addOntologyDataToRepo(File f) throws IOException {
		SailRepositoryConnection connection = repo.getConnection();
		connection.add(f);
		connection.commit();
		connection.close();
	}
	
	@ShellMethod("Set Sparql-Query-File ")
	public String setQueryFile(String path) {
		File queryFile = new File(path);
		if(!queryFile.exists() || !queryFile.canRead()) {
			return "Sparql-Query-File does not exist or reading is not allowed.";
		}
		
		this.queryFile = queryFile;
		
		return "File accepted";
		
	}
	
	@ShellMethod("Executes Sparql-Query from previously 'set-file' command.")
	public String execute() {
		if(queryFile == null) {
			return "You have to run the command 'set-file' first.";
		}
		
		String query = readQueryFile(queryFile.getPath());
		if(query == null) {
			return "Error occured.";
		}
		
		TableModelBuilder tableModelBuilder = new TableModelBuilder<String>();
		
		try(RepositoryConnection con = repo.getConnection()) {
			TupleQuery tupleQuery = con.prepareTupleQuery(query);
			try(TupleQueryResult result = tupleQuery.evaluate()) {
				List<String> bindingNames = result.getBindingNames();
				addHeaderRowToTable(tableModelBuilder, bindingNames);
				addValueRowsToTable(tableModelBuilder, bindingNames, result);
				TableModel tableModel = tableModelBuilder.build();
				Table table = new TableBuilder(tableModel).addFullBorder(BorderStyle.fancy_light).build();
				
				return table.render(100);
			}
		}
	}

	private String readQueryFile(String path) {
		String query = "";
		try {
			query = Files.readAllLines(Paths.get(path)).stream().reduce("", (s1,s2) -> s1+s2);
			LOG.info("Sparql-Query: {}", query);
		} catch (IOException e) {
			return null;
		}
		return query;
	}

	private void addHeaderRowToTable(TableModelBuilder tableModelBuilder, List<String> bindingNames) {
		TableModelBuilder row = tableModelBuilder.addRow();
		for(String header : bindingNames) {
			row.addValue(header);
		}
	}

	private void addValueRowsToTable(TableModelBuilder builder, List<String> bindingNames, TupleQueryResult result) {
		builder.addRow();
		for(BindingSet bindingSet : result) {
			for(String bindingName : bindingNames) {
				builder.addValue(bindingSet.getValue(bindingName));
			}
		}
	}


}
