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

package cf.monteux.silvertunnel.netlib.api.impl;

import java.io.IOException;
import java.net.ServerSocket;

import cf.monteux.silvertunnel.netlib.api.NetServerSocket;
import cf.monteux.silvertunnel.netlib.api.NetSocket;

/**
 * Adapter class
 * 
 * @author hapke
 */
public class ServerSocket2NetServerSocket implements NetServerSocket
{
	/** the wrapped ServerSocket object */
	private final ServerSocket serverSocket;

	public ServerSocket2NetServerSocket(final ServerSocket serverSocket)
			throws IOException
	{
		this.serverSocket = serverSocket;
	}

	@Override
	public NetSocket accept() throws IOException
	{
		return new Socket2NetSocket(serverSocket.accept());
	}

	@Override
	public void close() throws IOException
	{
		serverSocket.close();
	}
}
