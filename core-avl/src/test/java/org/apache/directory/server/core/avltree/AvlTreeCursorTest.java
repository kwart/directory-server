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
package org.apache.directory.server.core.avltree;


import static org.junit.Assert.*;

import java.util.Comparator;

import org.apache.directory.server.core.cursor.InvalidCursorPositionException;
import org.junit.Test;


/**
 * Tests the AvlTreeCursor class.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class AvlTreeCursorTest
{
    @Test
    public void testEmptyCursor() throws Exception
    {
        AvlTree<Integer> tree = new AvlTreeImpl<Integer>( new IntegerComparator() );
        AvlTreeCursor<Integer> cursor = new AvlTreeCursor<Integer>( tree );
        
        assertFalse( cursor.isClosed() );
        assertFalse( cursor.available() );
        assertTrue( cursor.isElementReused() );
        
        try
        {
            cursor.get();
            fail( "Should not get here" );
        }
        catch ( InvalidCursorPositionException e )
        {
            assertNotNull( e );
        }
        
        cursor.beforeFirst();
        assertFalse( cursor.available() );
        
        cursor.afterLast();
        assertFalse( cursor.available() );
        
        assertFalse( cursor.first() );
        assertFalse( cursor.available() );
        
        assertFalse( cursor.last() );
        assertFalse( cursor.available() );
        
        assertFalse( cursor.next() );
        assertFalse( cursor.available() );
        
        assertFalse( cursor.previous() );
        assertFalse( cursor.available() );

        cursor.before( 3 );
        assertFalse( cursor.available() );
        
        cursor.after( 3 );
        assertFalse( cursor.available() );
        
        cursor.close();
        assertTrue( cursor.isClosed() );
    }
    
    
    @Test
    public void testOneEntryCursor() throws Exception
    {
        AvlTree<Integer> tree = new AvlTreeImpl<Integer>( new IntegerComparator() );
        tree.insert( 7 );
        AvlTreeCursor<Integer> cursor = new AvlTreeCursor<Integer>( tree );
        
        assertFalse( cursor.isClosed() );
        assertFalse( cursor.available() );
        assertTrue( cursor.isElementReused() );
        
        try
        {
            cursor.get();
            fail( "Should not get here" );
        }
        catch ( InvalidCursorPositionException e )
        {
            assertNotNull( e );
        }
        
        cursor.beforeFirst();
        assertFalse( cursor.available() );
        assertFalse( cursor.previous() );
        assertTrue( cursor.next() );
        assertTrue( cursor.available() );
        assertEquals( 7, ( int ) cursor.get() );
        
        cursor.afterLast();
        assertFalse( cursor.next() );
        assertFalse( cursor.available() );
        
        assertTrue( cursor.first() );
        assertTrue( cursor.available() );
        
        assertTrue( cursor.last() );
        assertTrue( cursor.available() );
        
        assertFalse( cursor.next() );
        assertFalse( cursor.available() );
        
        assertTrue( cursor.previous() );
        assertTrue( cursor.available() );

        cursor.before( 3 );
        assertFalse( cursor.available() );
        assertTrue( cursor.next() );
        assertTrue( cursor.available() );
        assertEquals( 7, ( int ) cursor.get() );
        
        cursor.after( 3 );
        assertFalse( cursor.available() );
        assertTrue( cursor.next() );
        assertTrue( cursor.available() );
        assertEquals( 7, ( int ) cursor.get() );

        cursor.before( 7 );
        assertFalse( cursor.available() );
        assertTrue( cursor.next() );
        assertTrue( cursor.available() );
        assertEquals( 7, ( int ) cursor.get() );
    }
    
    
    @Test
    public void testManyEntriesCursor() throws Exception
    {
        AvlTree<Integer> tree = new AvlTreeImpl<Integer>( new IntegerComparator() );
        tree.insert( 3 );
        tree.insert( 7 );
        tree.insert( 10 );
        tree.insert( 11 );
        AvlTreeCursor<Integer> cursor = new AvlTreeCursor<Integer>( tree );
        
        assertFalse( cursor.isClosed() );
        assertFalse( cursor.available() );
        assertTrue( cursor.isElementReused() );
        assertEquals( 4, tree.getSize() );
        
        try
        {
            cursor.get();
            fail( "Should not get here" );
        }
        catch ( InvalidCursorPositionException e )
        {
            assertNotNull( e );
        }
        
        cursor.beforeFirst();
        assertFalse( cursor.available() );
        assertTrue( cursor.next() );
        assertTrue( cursor.available() );
        assertEquals( 3, ( int ) cursor.get() );
        assertTrue( cursor.next() );
        assertTrue( cursor.available() );
        assertEquals( 7, ( int ) cursor.get() );
        assertTrue( cursor.next() );
        assertTrue( cursor.available() );
        assertEquals( 10, ( int ) cursor.get() );
        assertTrue( cursor.next() );
        assertTrue( cursor.available() );
        assertEquals( 11, ( int ) cursor.get() );
        assertFalse( cursor.next() );
        assertFalse( cursor.available() );
        
        
        cursor.afterLast();
        assertFalse( cursor.available() );
        assertTrue( cursor.previous() );
        assertTrue( cursor.available() );
        assertEquals( 11, ( int ) cursor.get() );
        assertTrue( cursor.previous() );
        assertTrue( cursor.available() );
        assertEquals( 10, ( int ) cursor.get() );
        assertTrue( cursor.previous() );
        assertTrue( cursor.available() );
        assertEquals( 7, ( int ) cursor.get() );
        assertTrue( cursor.previous() );
        assertTrue( cursor.available() );
        assertEquals( 3, ( int ) cursor.get() );
        assertFalse( cursor.previous() );
        assertFalse( cursor.available() );
        
        assertTrue( cursor.first() );
        assertTrue( cursor.available() );
        
        assertTrue( cursor.last() );
        assertTrue( cursor.available() );
        
        assertFalse( cursor.next() );
        assertFalse( cursor.available() );
        
        assertTrue( cursor.previous() );
        assertTrue( cursor.available() );
        
        cursor.after( 5 );
        assertTrue( cursor.next() );
        assertTrue( cursor.available() );
        assertEquals( 7, ( int ) cursor.get() );
        
        
        cursor.before( 11 );
        assertTrue( cursor.next() );
        assertTrue( cursor.available() );
        assertEquals( 11, ( int ) cursor.get() );
        
    }
   
    
    class IntegerComparator implements Comparator<Integer>
    {
        public int compare( Integer o1, Integer o2 )
        {
            return o1.compareTo( o2 );
        }
    }
}
