/*
 * Copyright (c) 2002-2019 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.doc.server.rest;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import org.neo4j.helpers.collection.Pair;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;
import org.neo4j.logging.NullLogProvider;
import org.neo4j.server.database.CypherExecutor;
import org.neo4j.server.database.Database;
import org.neo4j.server.database.WrappedDatabase;
import org.neo4j.server.rest.management.console.CypherSession;
import org.neo4j.test.TestGraphDatabaseFactory;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CypherSessionDocTest
{
    @Test
    public void shouldReturnASingleNode() throws Throwable
    {
        GraphDatabaseFacade graphdb = (GraphDatabaseFacade) new TestGraphDatabaseFactory().newImpermanentDatabase();
        Database database = new WrappedDatabase( graphdb );
        CypherExecutor executor = new CypherExecutor( database, NullLogProvider.getInstance() );
        executor.start();
        try
        {
            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            when(httpServletRequest.getScheme()).thenReturn("http");
            when(httpServletRequest.getHeader("User-Agent")).thenReturn("user-agent");
            when(httpServletRequest.getRemoteAddr()).thenReturn("remote-addr");
            when(httpServletRequest.getRemotePort()).thenReturn(1234);
            when(httpServletRequest.getServerName()).thenReturn("server-name");
            when(httpServletRequest.getServerPort()).thenReturn(6789);
            CypherSession session = new CypherSession( executor, NullLogProvider.getInstance(), httpServletRequest);
            Pair<String, String> result = session.evaluate( "create (a) return a" );
            assertThat( result.first(), containsString( "Node[0]" ) );
        }
        finally
        {
            graphdb.shutdown();
        }
    }
}