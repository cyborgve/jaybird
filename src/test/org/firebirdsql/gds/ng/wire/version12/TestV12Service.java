/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.wire.version12;

import org.firebirdsql.gds.ng.wire.FbWireService;
import org.firebirdsql.gds.ng.wire.ProtocolCollection;
import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.version10.V10Service;
import org.firebirdsql.gds.ng.wire.version11.TestV11Service;
import org.junit.BeforeClass;

import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.junit.Assume.assumeTrue;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version10.V10Service} in the V12 protocol.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV12Service extends TestV11Service {

    private static final ProtocolDescriptor DUMMY_DESCRIPTOR = new Version12Descriptor();

    @BeforeClass
    public static void checkDbVersion() {
        assumeTrue(getDefaultSupportInfo().supportsProtocol(12));
    }

    protected V10Service createDummyService() {
        return new V10Service(DUMMY_CONNECTION, DUMMY_DESCRIPTOR);
    }

    @Override
    protected ProtocolCollection getProtocolCollection() {
        return ProtocolCollection.create(new Version12Descriptor());
    }

    @Override
    protected Class<? extends FbWireService> getExpectedServiceType() {
        return V10Service.class;
    }
}
