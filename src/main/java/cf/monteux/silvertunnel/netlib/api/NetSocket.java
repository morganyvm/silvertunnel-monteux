/*
 * SilverTunnel-Monteux Netlib - Java library to easily access anonymity networks
 * Copyright (c) 2009-2012 silvertunnel.org
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses/>.
 */

package cf.monteux.silvertunnel.netlib.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This is a protocol independent network socket.
 * 
 * Usage is similar to class Socket.
 * 
 * @author hapke
 */
public interface NetSocket
{
	InputStream getInputStream() throws IOException;

	OutputStream getOutputStream() throws IOException;

	void close() throws IOException;
}
