package com.api.graphql.bookdetails;

import com.google.common.base.*;
import com.google.common.io.Resources;
import graphql.*;
import graphql.schema.*;
import graphql.schema.idl.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

import javax.annotation.*;
import java.io.*;
import java.net.*;

import static graphql.schema.idl.TypeRuntimeWiring.*;

@Component
public class GraphQLProvider {
    @Autowired
    GraphQLDataFetchers graphQLDataFetchers;

    private GraphQL graphQL;

    @PostConstruct
    public void init() throws IOException {
        URL url = Resources.getResource("schema.graphqls");
        String sdl = Resources.toString(url, Charsets.UTF_8);
        GraphQLSchema graphQLSchema = buildSchema(sdl);
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    private GraphQLSchema buildSchema(String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                            .type(newTypeWiring("Query")
                                              .dataFetcher("bookById", graphQLDataFetchers.getBookByIdDataFetcher()))
                            .type(newTypeWiring("Book")
                                              .dataFetcher("author", graphQLDataFetchers.getAuthorDataFetcher()))
                            .build();
    }

    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }
}
