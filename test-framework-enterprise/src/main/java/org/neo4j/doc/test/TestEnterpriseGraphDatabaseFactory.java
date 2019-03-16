/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j Enterprise Edition. The included source
 * code can be redistributed and/or modified under the terms of the
 * GNU AFFERO GENERAL PUBLIC LICENSE Version 3
 * (http://www.fsf.org/licensing/licenses/agpl-3.0.html) with the
 * Commons Clause, as found in the associated LICENSE.txt file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * Neo4j object code can be licensed independently from the source
 * under separate terms from the AGPL. Inquiries can be directed to:
 * licensing@neo4j.com
 *
 * More information is also available at:
 * https://neo4j.com/licensing/
 */
package org.neo4j.doc.test;

import com.neo4j.commercial.edition.CommercialEditionModule;

import java.io.File;

import org.neo4j.common.Edition;
import org.neo4j.configuration.Config;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.facade.ExternalDependencies;
import org.neo4j.graphdb.facade.GraphDatabaseDependencies;
import org.neo4j.graphdb.facade.GraphDatabaseFacadeFactory;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactoryState;
import org.neo4j.graphdb.factory.module.GlobalModule;
import org.neo4j.kernel.impl.factory.DatabaseInfo;
import org.neo4j.logging.LogProvider;
import org.neo4j.logging.internal.LogService;
import org.neo4j.logging.internal.SimpleLogService;

/**
 * Factory for test graph database.
 */
public class TestEnterpriseGraphDatabaseFactory extends TestGraphDatabaseFactory
{
    @Override
    protected GraphDatabaseBuilder.DatabaseCreator createDatabaseCreator( File storeDir,
                                                                          GraphDatabaseFactoryState state )
    {
        return new GraphDatabaseBuilder.DatabaseCreator()
        {
            @Override
            public GraphDatabaseService newDatabase( Config config )
            {
                return new GraphDatabaseFacadeFactory( DatabaseInfo.COMMERCIAL, CommercialEditionModule::new )
                {
                    @Override
                    protected GlobalModule createGlobalModule( File storeDir, Config config, ExternalDependencies dependencies )
                    {
                        return new GlobalModule( storeDir, config, databaseInfo, dependencies )
                        {
                            @Override
                            protected LogService createLogService( LogProvider userLogProvider )
                            {
                                if ( state instanceof TestGraphDatabaseFactoryState )
                                {
                                    LogProvider logProvider =
                                            ((TestGraphDatabaseFactoryState) state).getInternalLogProvider();
                                    if ( logProvider != null )
                                    {
                                        return new SimpleLogService( logProvider, logProvider );
                                    }
                                }
                                return super.createLogService( userLogProvider );
                            }
                        };
                    }
                }.newFacade( storeDir, config,
                        GraphDatabaseDependencies.newDependencies( state.databaseDependencies() ) );
            }
        };
    }

    @Override
    protected GraphDatabaseBuilder.DatabaseCreator createImpermanentDatabaseCreator( final File storeDir,
                                                                                     final TestGraphDatabaseFactoryState state )
    {
        return new GraphDatabaseBuilder.DatabaseCreator()
        {
            @Override
            public GraphDatabaseService newDatabase( Config config )
            {
                return new TestEnterpriseGraphDatabaseFacadeFactory( state, true ).newFacade( storeDir, config,
                        GraphDatabaseDependencies.newDependencies( state.databaseDependencies() ) );
            }
        };
    }

    static class TestEnterpriseGraphDatabaseFacadeFactory extends TestGraphDatabaseFacadeFactory
    {

        TestEnterpriseGraphDatabaseFacadeFactory( TestGraphDatabaseFactoryState state, boolean impermanent )
        {
            super( state, impermanent, DatabaseInfo.COMMERCIAL, CommercialEditionModule::new );
        }
    }

    @Override
    public String getEdition()
    {
        return Edition.COMMERCIAL.toString();
    }
}
