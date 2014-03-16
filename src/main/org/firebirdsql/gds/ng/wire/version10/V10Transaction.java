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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.impl.wire.Xdrable;
import org.firebirdsql.gds.ng.AbstractFbTransaction;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireTransaction;
import org.firebirdsql.gds.ng.wire.GenericResponse;

import java.io.IOException;
import java.sql.SQLException;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * {@link FbTransaction} implementation for the version 10 wire protocol.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V10Transaction extends AbstractFbTransaction implements FbWireTransaction {

    private final FbWireDatabase database;
    private volatile int handle;

    /**
     * Creates a new instance of V10Transaction for the specified database.
     *
     * @param database FbWireDatabase implementation
     */
    public V10Transaction(FbWireDatabase database) {
        this.database = database;
    }

    protected final XdrOutputStream getXdrOut() throws SQLException {
        return database.getXdrStreamAccess().getXdrOut();
    }

    protected final FbWireDatabase getDatabase() {
        return database;
    }

    @Override
    public int getHandle() {
        return handle;
    }

    @Override
    public void beginTransaction(TransactionParameterBuffer tpb) throws SQLException {
        synchronized (getSynchronizationObject()) {
            switchState(TransactionState.STARTING);
            GenericResponse response;
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    final XdrOutputStream xdrOut = getXdrOut();
                    xdrOut.writeInt(op_transaction);
                    xdrOut.writeInt(getDatabase().getHandle());
                    xdrOut.writeTyped(ISCConstants.isc_tpb_version3, (Xdrable) tpb);
                    xdrOut.flush();
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ioex).toSQLException();
                }
                try {
                    response = (GenericResponse) getDatabase().readResponse(null);
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioex).toSQLException();
                }
            }
            handle = response.getObjectHandle();
            switchState(TransactionState.ACTIVE);
        }
    }

    @Override
    public void commit() throws SQLException {
        synchronized (getSynchronizationObject()) {
            switchState(TransactionState.COMMITTING);
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    final XdrOutputStream xdrOut = getXdrOut();
                    xdrOut.writeInt(op_commit);
                    xdrOut.writeInt(handle);
                    xdrOut.flush();
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ioex).toSQLException();
                }
                try {
                    getDatabase().readResponse(null);
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioex).toSQLException();
                }
            }
            switchState(TransactionState.COMMITTED);
        }
    }

    @Override
    public void rollback() throws SQLException {
        synchronized (getSynchronizationObject()) {
            switchState(TransactionState.ROLLING_BACK);
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    final XdrOutputStream xdrOut = getXdrOut();
                    xdrOut.writeInt(op_rollback);
                    xdrOut.writeInt(handle);
                    xdrOut.flush();
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ioex).toSQLException();
                }
                try {
                    getDatabase().readResponse(null);
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioex).toSQLException();
                }
            }
            switchState(TransactionState.ROLLED_BACK);
        }
    }

    @Override
    public void prepare(byte[] recoveryInformation) throws SQLException {
        synchronized (getSynchronizationObject()) {
            switchState(TransactionState.PREPARING);
            synchronized (getDatabase().getSynchronizationObject()) {
                try {
                    final XdrOutputStream xdrOut = getXdrOut();
                    xdrOut.writeInt(op_prepare2);
                    xdrOut.writeInt(handle);
                    xdrOut.writeBuffer(recoveryInformation);
                    xdrOut.flush();
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ioex).toSQLException();
                }
                try {
                    getDatabase().readResponse(null);
                } catch (IOException ioex) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ioex).toSQLException();
                }
            }
            switchState(TransactionState.PREPARED);
        }
    }
}
