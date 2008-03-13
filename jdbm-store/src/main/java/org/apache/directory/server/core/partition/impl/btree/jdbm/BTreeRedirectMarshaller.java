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
package org.apache.directory.server.core.partition.impl.btree.jdbm;


import java.io.IOException;

import org.apache.directory.server.core.avltree.Marshaller;
import org.apache.directory.shared.asn1.codec.binary.Hex;


/**
 * Serializes and deserializes a BTreeRedirect object to and from a byte[]
 * representation.  The serialized form is a fixed size byte array of length
 * 16.  The first 8 bytes are the ascii values for the String 'redirect' and
 * the last 8 bytes encode the record identifier as a long for the BTree.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */public class BTreeRedirectMarshaller implements Marshaller<BTreeRedirect>
{
    /** fixed byte array size of 16 for serialized form */
    static final int SIZE = 16;
    /** a reusable instance of this Marshaller */
    public static final BTreeRedirectMarshaller INSTANCE = new BTreeRedirectMarshaller();

    /**
     * @see Marshaller#serialize(Object)
     */
    public final byte[] serialize( BTreeRedirect redirect ) throws IOException
    {
        byte[] bites = new byte[SIZE];

        bites[0] = 'r';
        bites[1] = 'e';
        bites[2] = 'd';
        bites[3] = 'i';
        bites[4] = 'r';
        bites[5] = 'e';
        bites[6] = 'c';
        bites[7] = 't';

        bites[8] =  ( byte ) ( redirect.recId >> 56 );
        bites[9] =  ( byte ) ( redirect.recId >> 48 );
        bites[10] = ( byte ) ( redirect.recId >> 40 );
        bites[11] = ( byte ) ( redirect.recId >> 32 );
        bites[12] = ( byte ) ( redirect.recId >> 24 );
        bites[13] = ( byte ) ( redirect.recId >> 16 );
        bites[14] = ( byte ) ( redirect.recId >> 8 );
        bites[15] = ( byte ) redirect.recId;

        return bites;
    }


    /**
     * @see Marshaller#deserialize(byte[]) 
     */
    public final BTreeRedirect deserialize( byte[] bites ) throws IOException
    {
        if ( bites.length != SIZE ||
                  bites[0] != 'r' ||
                  bites[1] != 'e' ||
                  bites[2] != 'd' ||
                  bites[3] != 'i' ||
                  bites[4] != 'r' ||
                  bites[5] != 'e' ||
                  bites[6] != 'c' ||
                  bites[7] != 't' )
        {
            throw new IOException( "Not a serialized BTreeRedirect object: "
                    + new String( Hex.encodeHex( bites ) ) );
        }

        long recId;
        recId = bites[8] + ( ( bites[8] < 0 ) ? 256 : 0 );
        recId <<= 8;
        recId += bites[9] + ( ( bites[9] < 0 ) ? 256 : 0 );
        recId <<= 8;
        recId += bites[10] + ( ( bites[10] < 0 ) ? 256 : 0 );
        recId <<= 8;
        recId += bites[11] + ( ( bites[11] < 0 ) ? 256 : 0 );
        recId <<= 8;
        recId += bites[12] + ( ( bites[12] < 0 ) ? 256 : 0 );
        recId <<= 8;
        recId += bites[13] + ( ( bites[13] < 0 ) ? 256 : 0 );
        recId <<= 8;
        recId += bites[14] + ( ( bites[14] < 0 ) ? 256 : 0 );
        recId <<= 8;
        recId += bites[15] + ( ( bites[15] < 0 ) ? 256 : 0 );

        return new BTreeRedirect( recId );
    }


    /**
     * Checks to see if a byte[] does not contain a redirect.  It's faster
     * to check invalid bytes then to check for validity.
     *
     * @param bites the bites to check for validity
     * @return true if the bites do not contain a serialized BTreeRedirect,
     * false if they do
     */
    public static boolean isNotRedirect( byte[] bites )
    {
        if ( bites == null )
        {
            return true;
        }

        // faster to check if invalid than valid
        return bites.length != SIZE ||
                bites[0] != 'r' ||
                bites[1] != 'e' ||
                bites[2] != 'd' ||
                bites[3] != 'i' ||
                bites[4] != 'r' ||
                bites[5] != 'e' ||
                bites[6] != 'c' ||
                bites[7] != 't';
    }
}
