/*
 * Copyright 2005-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package cf.monteux.silvertunnel.netlib.adapter.url.impl.net.http;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to cleanup any remaining data that may be on a
 * KeepAliveStream so that the connection can be cached in the KeepAliveCache.
 * Instances of this class can be used as a FIFO queue for KeepAliveCleanerEntry
 * objects. Executing this Runnable removes each KeepAliveCleanerEntry from the
 * Queue, reads the reamining bytes on its KeepAliveStream, and if successful
 * puts the connection in the KeepAliveCache.
 * 
 * @author Chris Hegarty
 */

@SuppressWarnings("serial")
// never serialized
public class KeepAliveStreamCleaner extends
		LinkedBlockingQueue<MyKeepAliveCleanerEntry> implements Runnable
{
	/** */
	private static final Logger logger = LogManager.getLogger(KeepAliveStreamCleaner.class);
	// maximum amount of remaining data that we will try to cleanup
	protected static int MAX_DATA_REMAINING = 512;

	// maximum amount of KeepAliveStreams to be queued
	protected static int MAX_CAPACITY = 10;

	// timeout for both socket and poll on the queue
	protected static final int TIMEOUT = 5000;

	// max retries for skipping data
	private static final int MAX_RETRIES = 5;

	static
	{
		final String maxDataKey = "http.KeepAlive.remainingData";
		final int maxData = AccessController.doPrivileged(
				new PrivilegedAction<Integer>()
				{
					@Override
					public Integer run()
					{
						return NetProperties.getInteger(maxDataKey,
								MAX_DATA_REMAINING);
					}
				}).intValue() * 1024;
		MAX_DATA_REMAINING = maxData;

		final String maxCapacityKey = "http.KeepAlive.queuedConnections";
		final int maxCapacity = AccessController.doPrivileged(
				new PrivilegedAction<Integer>()
				{
					@Override
					public Integer run()
					{
						return NetProperties.getInteger(maxCapacityKey,
								MAX_CAPACITY);
					}
				}).intValue();
		MAX_CAPACITY = maxCapacity;

	}

	public KeepAliveStreamCleaner()
	{
		super(MAX_CAPACITY);
	}

	public KeepAliveStreamCleaner(int capacity)
	{
		super(capacity);
	}

	@Override
	public void run()
	{
		MyKeepAliveCleanerEntry kace = null;

		do
		{
			try
			{
				kace = poll(TIMEOUT, TimeUnit.MILLISECONDS);
				if (kace == null)
				{
					break;
				}

				final KeepAliveStream kas = kace.getKeepAliveStream();

				if (kas != null)
				{
					synchronized (kas)
					{
						final HttpClient hc = kace.getHttpClient();
						try
						{
							if (hc != null && !hc.isInKeepAliveCache())
							{
								final int oldTimeout = hc.setTimeout(TIMEOUT);
								long remainingToRead = kas.remainingToRead();
								if (remainingToRead > 0)
								{
									long n = 0;
									int retries = 0;
									while (n < remainingToRead
											&& retries < MAX_RETRIES)
									{
										remainingToRead = remainingToRead - n;
										n = kas.skip(remainingToRead);
										if (n == 0)
										{
											retries++;
										}
									}
									remainingToRead = remainingToRead - n;
								}
								if (remainingToRead == 0)
								{
									hc.setTimeout(oldTimeout);
									hc.finished();
								}
								else
								{
									hc.closeServer();
								}
							}
						}
						catch (final IOException ioe)
						{
							hc.closeServer();
							logger.debug("got IOException : {}", ioe.getMessage(), ioe);
						}
						finally
						{
							kas.setClosed();
						}
					}
				}
			}
			catch (final InterruptedException ie)
			{
				logger.debug("got IterruptedException : {}", ie.getMessage(), ie);
			}
		}
		while (kace != null);
	}
}
