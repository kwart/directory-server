/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.server.core.configuration;


import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.naming.directory.Attributes;

import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.authn.AnonymousAuthenticator;
import org.apache.directory.server.core.authn.AuthenticationService;
import org.apache.directory.server.core.authn.SimpleAuthenticator;
import org.apache.directory.server.core.authn.StrongAuthenticator;
import org.apache.directory.server.core.authz.AuthorizationService;
import org.apache.directory.server.core.authz.DefaultAuthorizationService;
import org.apache.directory.server.core.collective.CollectiveAttributeService;
import org.apache.directory.server.core.event.EventService;
import org.apache.directory.server.core.exception.ExceptionService;
import org.apache.directory.server.core.normalization.NormalizationService;
import org.apache.directory.server.core.operational.OperationalAttributeService;
import org.apache.directory.server.core.referral.ReferralService;
import org.apache.directory.server.core.schema.SchemaService;
import org.apache.directory.server.core.subtree.SubentryService;
import org.apache.directory.server.core.trigger.TriggerService;
import org.apache.directory.shared.ldap.ldif.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A {@link Configuration} that starts up ApacheDS.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class StartupConfiguration extends Configuration
{
    private static final Logger log = LoggerFactory.getLogger( StartupConfiguration.class );

    /** Speedup for logs */
    private static final boolean IS_DEBUG = log.isDebugEnabled();

    private static final long serialVersionUID = 4826762196566871677L;

    public static final int MAX_THREADS_DEFAULT = 4;
    public static final int MAX_SIZE_LIMIT_DEFAULT = 100;
    public static final int MAX_TIME_LIMIT_DEFAULT = 10000;

    private File workingDirectory = new File( "server-work" );
    private boolean exitVmOnShutdown = true; // allow by default
    private boolean shutdownHookEnabled = true; // allow by default
    private boolean allowAnonymousAccess = true; // allow by default
    private boolean accessControlEnabled = false; // turn off by default
    private boolean denormalizeOpAttrsEnabled = false;
    private int maxThreads = MAX_THREADS_DEFAULT; // set to default value
    private int maxSizeLimit = MAX_SIZE_LIMIT_DEFAULT; // set to default value
    private int maxTimeLimit = MAX_TIME_LIMIT_DEFAULT; // set to default value (milliseconds)
    private Set authenticatorConfigurations; // Set<AuthenticatorConfiguration>
    private List interceptorConfigurations; // Set<InterceptorConfiguration>
    private PartitionConfiguration systemPartitionConfiguration; 
    private Set<PartitionConfiguration> partitionConfigurations = new HashSet<PartitionConfiguration>();
    private List testEntries = new ArrayList(); // List<Attributes>


    /**
     * Creates a new instance with default settings.
     */
    public StartupConfiguration()
    {
        setDefaultAuthenticatorConfigurations();
        setDefaultInterceptorConfigurations();
    }


    /**
     * Creates a new instance with default settings that operates on the
     * {@link DirectoryService} with the specified ID.
     */
    public StartupConfiguration(String instanceId)
    {
        setDefaultAuthenticatorConfigurations();
        setDefaultInterceptorConfigurations();
        setInstanceId( instanceId );
    }


    private void setDefaultAuthenticatorConfigurations()
    {
        Set<AuthenticatorConfiguration> set = new HashSet<AuthenticatorConfiguration>();

        // Anonymous
        set.add( new MutableAuthenticatorConfiguration( "Anonymous", new AnonymousAuthenticator() ) );

        // Simple
        set.add( new MutableAuthenticatorConfiguration( "Simple", new SimpleAuthenticator() ) );

        // Strong
        set.add( new MutableAuthenticatorConfiguration( "Strong", new StrongAuthenticator() ) );

        setAuthenticatorConfigurations( set );
    }


    private void setDefaultInterceptorConfigurations()
    {
        // Set default interceptor chains
        InterceptorConfiguration interceptorCfg;
        List<InterceptorConfiguration> list = new ArrayList<InterceptorConfiguration>();

        interceptorCfg = new MutableInterceptorConfiguration();
        interceptorCfg.setName( NormalizationService.NAME );
        interceptorCfg.setInterceptor( new NormalizationService() );
        list.add( interceptorCfg );

        interceptorCfg = new MutableInterceptorConfiguration();
        interceptorCfg.setName( AuthenticationService.NAME );
        interceptorCfg.setInterceptor( new AuthenticationService() );
        list.add( interceptorCfg );

        interceptorCfg = new MutableInterceptorConfiguration();
        interceptorCfg.setName( ReferralService.NAME );
        interceptorCfg.setInterceptor( new ReferralService() );
        list.add( interceptorCfg );

        interceptorCfg = new MutableInterceptorConfiguration();
        interceptorCfg.setName( AuthorizationService.NAME );
        interceptorCfg.setInterceptor( new AuthorizationService() );
        list.add( interceptorCfg );

        interceptorCfg = new MutableInterceptorConfiguration();
        interceptorCfg.setName( DefaultAuthorizationService.NAME );
        interceptorCfg.setInterceptor( new DefaultAuthorizationService() );
        list.add( interceptorCfg );

        interceptorCfg = new MutableInterceptorConfiguration();
        interceptorCfg.setName( ExceptionService.NAME );
        interceptorCfg.setInterceptor( new ExceptionService() );
        list.add( interceptorCfg );

        interceptorCfg = new MutableInterceptorConfiguration();
        interceptorCfg.setName( OperationalAttributeService.NAME );
        interceptorCfg.setInterceptor( new OperationalAttributeService() );
        list.add( interceptorCfg );

        interceptorCfg = new MutableInterceptorConfiguration();
        interceptorCfg.setName( SchemaService.NAME );
        interceptorCfg.setInterceptor( new SchemaService() );
        list.add( interceptorCfg );

        interceptorCfg = new MutableInterceptorConfiguration();
        interceptorCfg.setName( SubentryService.NAME );
        interceptorCfg.setInterceptor( new SubentryService() );
        list.add( interceptorCfg );

        interceptorCfg = new MutableInterceptorConfiguration();
        interceptorCfg.setName( CollectiveAttributeService.NAME );
        interceptorCfg.setInterceptor( new CollectiveAttributeService() );
        list.add( interceptorCfg );

        interceptorCfg = new MutableInterceptorConfiguration();
        interceptorCfg.setName( EventService.NAME );
        interceptorCfg.setInterceptor( new EventService() );
        list.add( interceptorCfg );
        
        interceptorCfg = new MutableInterceptorConfiguration();
        interceptorCfg.setName( TriggerService.NAME );
        interceptorCfg.setInterceptor( new TriggerService() );
        list.add( interceptorCfg );

        setInterceptorConfigurations( list );
    }


    /**
     * Returns {@link AuthenticatorConfiguration}s to use for authenticating clients.
     */
    public Set<AuthenticatorConfiguration> getAuthenticatorConfigurations()
    {
        return ConfigurationUtil.getClonedSet( authenticatorConfigurations );
    }


    /**
     * Sets {@link AuthenticatorConfiguration}s to use for authenticating clients.
     */
    protected void setAuthenticatorConfigurations( Set<AuthenticatorConfiguration> authenticatorConfigurations )
    {
        Set<String> names = new HashSet<String>();

        // Loop through all the configurations to check if we do not have duplicated authenticators.
        for ( AuthenticatorConfiguration cfg:authenticatorConfigurations )
        {
            cfg.validate();

            String name = cfg.getName();

            if ( names.contains( name ) )
            {
                // TODO Not sure that it worth to throw an excpetion here. We could simply ditch the
                // duplicated authenticator, trace a warning and that's it. 
                log.error( "The authenticator nammed '{}' has already been registred.", name );
                throw new ConfigurationException( "Duplicate authenticator name: " + name );
            }
            
            names.add( name );
        }

        // The set has been checked, so we can now register it
        this.authenticatorConfigurations = authenticatorConfigurations;
    }


    /**
     * Returns {@link PartitionConfiguration}s to configure context partitions.
     */
    public Set<PartitionConfiguration> getPartitionConfigurations()
    {
        return ConfigurationUtil.getClonedSet( partitionConfigurations );
    }


    /**
     * Sets {@link PartitionConfiguration}s to configure context partitions.
     */
    protected void setPartitionConfigurations( Set<? extends PartitionConfiguration> contextParitionConfigurations )
    {
        Set newSet = ConfigurationUtil.getTypeSafeSet( contextParitionConfigurations,
            PartitionConfiguration.class );

        Set names = new HashSet();
        Iterator i = newSet.iterator();
        while ( i.hasNext() )
        {
            PartitionConfiguration cfg = ( PartitionConfiguration ) i.next();
            cfg.validate();

            String name = cfg.getName();
            if ( names.contains( name ) )
            {
                throw new ConfigurationException( "Duplicate partition name: " + name );
            }
            names.add( name );
        }

        this.partitionConfigurations = newSet;
    }


    /**
     * Returns <tt>true</tt> if access control checks are enbaled.
     */
    public boolean isAccessControlEnabled()
    {
        return accessControlEnabled;
    }


    /**
     * Sets whether to enable basic access control checks or not
     */
    protected void setAccessControlEnabled( boolean accessControlEnabled )
    {
        this.accessControlEnabled = accessControlEnabled;
    }


    /**
     * Returns <tt>true</tt> if anonymous access is allowed.
     */
    public boolean isAllowAnonymousAccess()
    {
        return allowAnonymousAccess;
    }


    /**
     * Sets whether to allow anonymous access or not
     */
    protected void setAllowAnonymousAccess( boolean enableAnonymousAccess )
    {
        this.allowAnonymousAccess = enableAnonymousAccess;
    }


    /**
     * Returns interceptor chain.
     */
    public List getInterceptorConfigurations()
    {
        return ConfigurationUtil.getClonedList( interceptorConfigurations );
    }


    /**
     * Sets interceptor chain.
     */
    protected void setInterceptorConfigurations( List interceptorConfigurations )
    {
        List newList = ConfigurationUtil.getTypeSafeList( interceptorConfigurations, InterceptorConfiguration.class );

        Set names = new HashSet();
        Iterator i = newList.iterator();
        while ( i.hasNext() )
        {
            InterceptorConfiguration cfg = ( InterceptorConfiguration ) i.next();
            cfg.validate();

            String name = cfg.getName();
            if ( names.contains( name ) )
            {
                throw new ConfigurationException( "Duplicate interceptor name: " + name );
            }
            names.add( name );
        }

        this.interceptorConfigurations = interceptorConfigurations;
    }


    /**
     * Returns test directory entries({@link Attributes}) to be loaded while
     * bootstrapping.
     */
    public List getTestEntries()
    {
    	try
    	{
    		return ConfigurationUtil.getClonedAttributesList( testEntries );
    	}
    	catch ( CloneNotSupportedException cnse )
    	{
    		return null;
    	}
    }


    /**
     * Sets test directory entries({@link Attributes}) to be loaded while
     * bootstrapping.
     */
    protected void setTestEntries( List testEntries )
    {
    	try
    	{
	        testEntries = ConfigurationUtil.getClonedAttributesList( ConfigurationUtil.getTypeSafeList( testEntries,
	            Entry.class ) );
	
	        this.testEntries = testEntries;
    	}
    	catch ( CloneNotSupportedException cnse )
    	{
    		this.testEntries = null;
    	}
    }


    /**
     * Returns working directory (counterpart of <tt>var/lib</tt>).
     */
    public File getWorkingDirectory()
    {
        return workingDirectory;
    }


    /**
     * Sets working directory (counterpart of <tt>var/lib</tt>).
     */
    protected void setWorkingDirectory( File workingDirectory )
    {
        this.workingDirectory = workingDirectory;
    }


    public void validate()
    {
        setWorkingDirectory( workingDirectory );
    }


    protected void setShutdownHookEnabled( boolean shutdownHookEnabled )
    {
        this.shutdownHookEnabled = shutdownHookEnabled;
    }


    public boolean isShutdownHookEnabled()
    {
        return shutdownHookEnabled;
    }


    protected void setExitVmOnShutdown( boolean exitVmOnShutdown )
    {
        this.exitVmOnShutdown = exitVmOnShutdown;
    }


    public boolean isExitVmOnShutdown()
    {
        return exitVmOnShutdown;
    }


    protected void setMaxThreads( int maxThreads )
    {
        this.maxThreads = maxThreads;
        if ( maxThreads < 1 )
        {
            throw new IllegalArgumentException( "Number of max threads should be greater than 0" );
        }
    }


    public int getMaxThreads()
    {
        return maxThreads;
    }


    protected void setMaxSizeLimit( int maxSizeLimit )
    {
        this.maxSizeLimit = maxSizeLimit;
    }


    public int getMaxSizeLimit()
    {
        return maxSizeLimit;
    }


    protected void setMaxTimeLimit( int maxTimeLimit )
    {
        this.maxTimeLimit = maxTimeLimit;
    }


    public int getMaxTimeLimit()
    {
        return maxTimeLimit;
    }

    protected void setSystemPartitionConfiguration( PartitionConfiguration systemPartitionConfiguration )
    {
        this.systemPartitionConfiguration = systemPartitionConfiguration;
    }


    public PartitionConfiguration getSystemPartitionConfiguration()
    {
        return systemPartitionConfiguration;
    }


    public boolean isDenormalizeOpAttrsEnabled()
    {
        return denormalizeOpAttrsEnabled;
    }
    
    
    protected void setDenormalizeOpAttrsEnabled( boolean denormalizeOpAttrsEnabled )
    {
        this.denormalizeOpAttrsEnabled = denormalizeOpAttrsEnabled;
    }
}
