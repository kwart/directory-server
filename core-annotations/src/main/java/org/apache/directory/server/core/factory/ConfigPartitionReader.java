/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.apache.directory.server.core.factory;


import javax.naming.directory.SearchControls;

import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.entry.ClonedServerEntry;
import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.xdbm.ForwardIndexEntry;
import org.apache.directory.server.xdbm.IndexCursor;
import org.apache.directory.server.xdbm.search.SearchEngine;
import org.apache.directory.shared.ldap.entry.Entry;
import org.apache.directory.shared.ldap.entry.EntryAttribute;
import org.apache.directory.shared.ldap.filter.PresenceNode;
import org.apache.directory.shared.ldap.message.AliasDerefMode;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A class used for reading the configuration present in a Partition
 * and instantiate the necessary objects like DirectoryService, Interceptors etc.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class ConfigPartitionReader
{

    /** the partition which holds the configuration data */
    private Partition configPartition;

    /** the search engine of the partition */
    private SearchEngine<ServerEntry> se;

    private static final Logger LOG = LoggerFactory.getLogger( ConfigPartitionReader.class );


    /**
     * 
     * Creates a new instance of ConfigPartitionReader.
     *
     * @param configPartition the non null config partition
     */
    public ConfigPartitionReader( LdifPartition configPartition )
    {
        if ( configPartition == null )
        {
            throw new IllegalArgumentException( "Config partition cannot be null" );
        }

        if ( !configPartition.isInitialized() )
        {
            throw new IllegalStateException( "the config partition is not initialized" );
        }

        this.configPartition = configPartition;
        se = configPartition.getSearchEngine();
    }


    /**
     * 
     * instantiates a DirectoryService based on the configuration present in the partition 
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void getDirectoryService() throws Exception
    {

        PresenceNode filter = new PresenceNode( "ads-directoryServiceId" );
        SearchControls controls = new SearchControls();
        controls.setSearchScope( SearchControls.SUBTREE_SCOPE );

        IndexCursor cursor = se.cursor( configPartition.getSuffixDn(), AliasDerefMode.NEVER_DEREF_ALIASES, filter,
            controls );

        if ( !cursor.next() )
        {
            throw new Exception( "No directoryService instance was configured under the DN "
                + configPartition.getSuffixDn() );
        }

        ForwardIndexEntry<Long, Long> forwardEntry = ( ForwardIndexEntry<Long, Long> ) cursor.get();
        cursor.close();

        ClonedServerEntry dsEntry = configPartition.lookup( forwardEntry.getValue() );

        LOG.debug( "dirServiceEntry {}", dsEntry );

        DirectoryService dirService = new DefaultDirectoryService();
        // MUST attributes
        dirService.setInstanceId( getString( "ads-directoryServiceId", dsEntry ) );
        dirService.setReplicaId( getInt( "ads-dsReplicaId", dsEntry ) );
        // dirService.setInterceptors( interceptors );

        // MAY attributes
        EntryAttribute acEnabledAttr = dsEntry.get( "ads-dsAccessControlEnabled" );
        if ( acEnabledAttr != null )
        {
            dirService.setAccessControlEnabled( Boolean.parseBoolean( acEnabledAttr.getString() ) );
        }

        EntryAttribute anonAccessAttr = dsEntry.get( "ads-dsAllowAnonymousAccess" );
        if ( anonAccessAttr != null )
        {
            dirService.setAllowAnonymousAccess( Boolean.parseBoolean( anonAccessAttr.getString() ) );
        }

        EntryAttribute changeLogAttr = dsEntry.get( "ads-dsChangeLog" );
        if ( changeLogAttr != null )
        {
            // configure CL
        }

        EntryAttribute denormAttr = dsEntry.get( "ads-dsDenormalizeOpAttrsEnabled" );
        if ( denormAttr != null )
        {
            dirService.setDenormalizeOpAttrsEnabled( Boolean.parseBoolean( denormAttr.getString() ) );
        }

        EntryAttribute journalAttr = dsEntry.get( "ads-dsJournal" );
        if ( journalAttr != null )
        {
            // dirService.setJournal(  );
        }

        EntryAttribute maxPduAttr = dsEntry.get( "ads-dsMaxPDUSize" );
        if ( maxPduAttr != null )
        {
            dirService.setMaxPDUSize( Integer.parseInt( maxPduAttr.getString() ) );
        }

        EntryAttribute passwordHidAttr = dsEntry.get( "ads-dsPasswordHidden" );
        if ( passwordHidAttr != null )
        {
            dirService.setPasswordHidden( Boolean.parseBoolean( passwordHidAttr.getString() ) );
        }

        EntryAttribute replAttr = dsEntry.get( "ads-dsReplication" );
        if ( replAttr != null )
        {
            // configure replication
        }

        EntryAttribute syncPeriodAttr = dsEntry.get( "ads-dsSyncPeriodMillis" );
        if ( syncPeriodAttr != null )
        {
            // FIXME the DirectoryService interface doesn't have this setter
            //dirService.setSyncPeriodMillis( Long.parseLong( syncPeriodAttr.getString() ) );
        }

        EntryAttribute testEntryAttr = dsEntry.get( "ads-dsTestEntries" );
        if ( testEntryAttr != null )
        {
            //process the test entries, should this be a FS location?
        }

        EntryAttribute enabledAttr = dsEntry.get( "ads-enabled" );
        if ( enabledAttr != null )
        {
            boolean enabled = Boolean.parseBoolean( enabledAttr.getString() );
            if ( !enabled )
            {
                // will only be useful if we ever allow more than one DS to be configured and
                // switch between them
                // decide which one to use based on this flag
            }
        }

        LdapDN interceptorsDN = new LdapDN( dsEntry.get( "ads-dsInterceptors" ).getString() );
        filter = new PresenceNode( "ads-interceptorId" );
        controls.setSearchScope( SearchControls.ONELEVEL_SCOPE );
        cursor = se.cursor( interceptorsDN, AliasDerefMode.NEVER_DEREF_ALIASES, filter, controls );
    }


    private boolean getBoolean( String attrName, Entry entry ) throws Exception
    {
        return Boolean.parseBoolean( entry.get( attrName ).getString() );
    }


    private String getString( String attrName, Entry entry ) throws Exception
    {
        return entry.get( attrName ).getString();
    }


    private int getInt( String attrName, Entry entry ) throws Exception
    {
        return Integer.parseInt( entry.get( attrName ).getString() );
    }

}
