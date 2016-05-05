package apoc.algo.pagerank;

import apoc.algo.Algo;
import apoc.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.test.TestGraphDatabaseFactory;

import static org.junit.Assert.assertEquals;

public class PageRankAlgoTest
{
    private GraphDatabaseService db;

    public static final String COMPANY_RESULT_QUERY = "MATCH (c:Company) " +
                                                      "WHERE c.name = {name} " +
                                                      "RETURN id(c) AS id, " +
                                                      "c.pagerank AS pagerank";

    public static final String COMPANIES_QUERY = "CREATE (a:Company {name:'a'})\n" +
                                                 "CREATE (b:Company {name:'b'})\n" +
                                                 "CREATE (c:Company {name:'c'})\n" +
                                                 "CREATE (d:Company {name:'d'})\n" +
                                                 "CREATE (e:Company {name:'e'})\n" +
                                                 "CREATE (f:Company {name:'f'})\n" +
                                                 "CREATE (g:Company {name:'g'})\n" +
                                                 "CREATE (h:Company {name:'h'})\n" +
                                                 "CREATE (i:Company {name:'i'})\n" +
                                                 "CREATE (j:Company {name:'j'})\n" +
                                                 "CREATE (k:Company {name:'k'})\n" +

                                                 "CREATE\n" +
                                                 "  (b)-[:SIMILAR {score:0.80}]->(c),\n" +
                                                 "  (c)-[:SIMILAR {score:0.80}]->(b),\n" +
                                                 "  (d)-[:SIMILAR {score:0.80}]->(a),\n" +
                                                 "  (e)-[:SIMILAR {score:0.80}]->(b),\n" +
                                                 "  (e)-[:SIMILAR {score:0.80}]->(d),\n" +
                                                 "  (e)-[:SIMILAR {score:0.80}]->(f),\n" +
                                                 "  (f)-[:SIMILAR {score:0.80}]->(b),\n" +
                                                 "  (f)-[:SIMILAR {score:0.80}]->(e),\n" +
                                                 "  (g)-[:SIMILAR {score:0.80}]->(b),\n" +
                                                 "  (g)-[:SIMILAR {score:0.80}]->(e),\n" +
                                                 "  (h)-[:SIMILAR {score:0.80}]->(b),\n" +
                                                 "  (h)-[:SIMILAR {score:0.80}]->(e),\n" +
                                                 "  (i)-[:SIMILAR {score:0.80}]->(b),\n" +
                                                 "  (i)-[:SIMILAR {score:0.80}]->(e),\n" +
                                                 "  (j)-[:SIMILAR {score:0.80}]->(e),\n" +
                                                 "  (k)-[:SIMILAR {score:0.80}]->(e)\n";

    public static final double EXPECTED = 2.87711;
    static ExecutorService pool = PageRankUtils.createPool( 2, 50 );

    @Before
    public void setUp() throws Exception
    {
        db = new TestGraphDatabaseFactory().newImpermanentDatabase();
        TestUtil.registerProcedure( db, Algo.class );
    }

    @After
    public void tearDown()
    {
        db.shutdown();
    }

    @Test
    public void shouldGetPageRankArrayStorageSPI() throws IOException
    {
        db.execute( COMPANIES_QUERY ).close();
        PageRank pageRank = new PageRankArrayStorageParallelSPI( db, pool );
        pageRank.compute( 20 );
        long id = (long) getEntry( "b" ).get( "id" );
        assertEquals( EXPECTED, pageRank.getResult( id ), 0.1D );

        for ( int i = 0; i < pageRank.numberOfNodes(); i++ )
        {
            System.out.println( pageRank.getResult( i ) );
        }
    }

    private Map<String,Object> getEntry( String name )
    {
        try ( Result result = db
                .execute( COMPANY_RESULT_QUERY, Collections.<String,Object>singletonMap( "name", name ) ) )
        {
            return result.next();
        }
    }
}

