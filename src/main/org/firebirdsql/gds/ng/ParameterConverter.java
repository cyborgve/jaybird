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
package org.firebirdsql.gds.ng;

import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ServiceParameterBuffer;

/**
 * Provides conversion of parameters (eg from {@link org.firebirdsql.gds.ng.IConnectionProperties} to a
 * {@link org.firebirdsql.gds.DatabaseParameterBuffer}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface ParameterConverter {

    /**
     * Builds a {@code DatabaseParameterBuffer} from the supplied {code IConnectionProperties}.
     *
     * @param connectionProperties
     *         Connection properties
     * @param encodingFactory
     *         Encoding factory
     * @return Database parameter buffer populated based on the connection properties
     */
    DatabaseParameterBuffer toDatabaseParameterBuffer(IConnectionProperties connectionProperties,
            IEncodingFactory encodingFactory);

    /**
     * Builds a {@code ServiceParameterBuffer} from the supplied {code IServiceProperties}.
     *
     * @param serviceProperties
     *         Service properties
     * @param encodingFactory
     *         Encoding factory
     * @return Service parameter buffer populated based on the service properties
     */
    ServiceParameterBuffer toServiceParameterBuffer(IServiceProperties serviceProperties,
            IEncodingFactory encodingFactory);
}
