/*
 * $Id$
 *
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
package org.firebirdsql.common;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.FBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

/**
 * Helper class for executing DDL while ignoring certain errors.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public final class DdlHelper {

    private DdlHelper() {
    }

    /**
     * Helper method for executing CREATE TABLE, ignoring <code>isc_no_meta_update</code> errors.
     *
     * @param connection
     *         Connection to execute statement
     * @param sql
     *         Create table statement
     * @throws SQLException
     *         SQLException for executing statement, except if there error is <code>isc_no_meta_update</code>
     * @see #executeDDL(java.sql.Connection, String, int...)
     */
    public static void executeCreateTable(Connection connection, String sql) throws SQLException {
        DdlHelper.executeDDL(connection, sql, ISCConstants.isc_no_meta_update);
    }

    /**
     * Helper method for executing DDL (or technically: any statement), ignoring the specified list of error codes.
     *
     * @param connection
     *         Connection to execute statement
     * @param sql
     *         DDL statement
     * @param ignoreErrors
     *         Firebird error codes to ignore
     * @throws SQLException
     *         SQLException for executing statement, except for errors with the error code listed in
     *         <code>ignoreErrors</code>
     * @see org.firebirdsql.gds.ISCConstants
     */
    public static void executeDDL(Connection connection, String sql, int... ignoreErrors) throws SQLException {
        if (ignoreErrors != null) {
            Arrays.sort(ignoreErrors);
        }

        try {
            Statement stmt = connection.createStatement();
            try {
                stmt.execute(sql);
            } finally {
                stmt.close();
            }
        } catch (SQLException ex) {
            if (ignoreErrors == null || ignoreErrors.length == 0)
                throw ex;

            for (Throwable current : ex) {
                if (current instanceof SQLException
                        && Arrays.binarySearch(ignoreErrors, ((SQLException) current).getErrorCode()) >= 0) {
                    return;
                }
            }

            throw ex;
        }
    }

    /**
     * Helper method for executing DROP TABLE, ignoring errors if the table or view doesn't exist.
     * <p>
     * Ignored errors are:
     * <ul>
     *     <li><code>isc_no_meta_update</code></li>
     *     <li><code>isc_dsql_table_not_found</code></li>
     *     <li><code>isc_dsql_view_not_found</code></li>
     *     <li><code>isc_dsql_error</code> (Firebird 1.5 or earlier only)</li>
     * </ul>
     * </p>
     *
     * @param connection
     *         Connection to execute statement
     * @param sql
     *         Drop table statement
     * @throws SQLException
     *         SQLException for executing statement, except for the listed errors.
     * @see #executeDDL(java.sql.Connection, String, int...)
     */
    public static void executeDropTable(Connection connection, String sql) throws SQLException {
        executeDDL(connection, sql, DdlHelper.getDropIgnoreErrors(connection));
    }

    private static int[] getDropIgnoreErrors(Connection connection) throws SQLException {
        GDSHelper gdsHelper = ((FBConnection) connection).getGDSHelper();
        if (gdsHelper.compareToVersion(2, 0) < 0) {
            // Firebird 1.5 and earlier do not always return specific error codes
            return new int[] { ISCConstants.isc_dsql_error, ISCConstants.isc_no_meta_update,
                    ISCConstants.isc_dsql_table_not_found, ISCConstants.isc_dsql_view_not_found };
        } else {
            return new int[] { ISCConstants.isc_no_meta_update,
                    ISCConstants.isc_dsql_table_not_found, ISCConstants.isc_dsql_view_not_found };
        }
    }

}
