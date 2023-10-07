package com.frameworkontology.sparql.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.Table;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;
import org.springframework.shell.table.TableModelBuilder;
import org.springframework.stereotype.Service;

import com.frameworkontology.sparql.repo.InMemoryRepoFactory;

/**
 * Offers operations for querying an in-memory ontology.
 * 
 * @author Francis Opoku
 */
@Service
public class OntologyService {

	private InMemoryRepoFactory repoFactory;
	private SailRepository repo;

	public OntologyService(InMemoryRepoFactory repoFactory) {
		this.repoFactory = repoFactory;
	}

	/**
	 * Sets the current ontology to query against.
	 * 
	 * @param path
	 * @return descriptive message about success or failure
	 */
	public String setOntology(String path) {
		File f = new File(path);
		if (!f.exists() || !f.canRead()) {
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

	/**
	 * Executes a SPARQL-Query against the in-memory ontology and returns the result
	 * in tabular format.
	 * 
	 * @param query SPARQL-Query
	 * @return result as table
	 */
	public String query(String query) {
		TableModelBuilder tableModelBuilder = new TableModelBuilder<String>();

		try (RepositoryConnection con = repo.getConnection()) {
			TupleQuery tupleQuery = con.prepareTupleQuery(query);
			try (TupleQueryResult result = tupleQuery.evaluate()) {
				List<String> bindingNames = result.getBindingNames();
				addHeaderRowToTable(tableModelBuilder, bindingNames);
				addValueRowsToTable(tableModelBuilder, bindingNames, result);
				TableModel tableModel = tableModelBuilder.build();
				Table table = new TableBuilder(tableModel).addFullBorder(BorderStyle.fancy_light).build();

				return table.render(100);
			}
		}
	}

	private void addHeaderRowToTable(TableModelBuilder tableModelBuilder, List<String> bindingNames) {
		TableModelBuilder row = tableModelBuilder.addRow();
		for (String header : bindingNames) {
			row.addValue(header);
		}
	}

	private void addValueRowsToTable(TableModelBuilder builder, List<String> bindingNames, TupleQueryResult result) {
		builder.addRow();
		for (BindingSet bindingSet : result) {
			for (String bindingName : bindingNames) {
				builder.addValue(bindingSet.getValue(bindingName));
			}
		}
	}

}
