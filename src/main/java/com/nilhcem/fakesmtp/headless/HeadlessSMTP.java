package com.nilhcem.fakesmtp.headless;

import com.nilhcem.fakesmtp.core.ArgsHandler;
import com.nilhcem.fakesmtp.core.Configuration;
import com.nilhcem.fakesmtp.core.exception.UncaughtExceptionHandler;
import com.nilhcem.fakesmtp.server.SMTPServerHandler;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HeadlessSMTP
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HeadlessSMTP.class);

    public static void main(final String[] args)
    {
        try
        {
            ArgsHandler.INSTANCE.handleArgs(args);
        }
        catch (ParseException e)
        {
            ArgsHandler.INSTANCE.displayUsage();
            return;
        }
        try
        {
            SMTPServerHandler.INSTANCE.startServer(getPort(), getBindAddress());
        }
        catch (NumberFormatException e)
        {
            LOGGER.error("Error: Invalid port number", e);
        }
        catch (UnknownHostException e)
        {
            LOGGER.error("Error: Invalid bind address", e);
        }
        catch (Exception e)
        {
            LOGGER.error("Failed to auto-start server in background", e);
        }
    }

    /**
     * @return either the default port, or the custom port, if specified.
     * @throws NumberFormatException if the specified port cannot be parsed to an integer.
     */
    private static int getPort() throws NumberFormatException
    {
        String portStr = ArgsHandler.INSTANCE.getPort();
        if (portStr == null)
        {
            portStr = Configuration.INSTANCE.get("smtp.default.port");
        }
        return Integer.parseInt(portStr);
    }

    /**
     * @return an InetAddress representing the specified bind address, or null, if not specified
     * @throws UnknownHostException if the bind address is invalid
     */
    private static InetAddress getBindAddress() throws UnknownHostException
    {
        String bindAddressStr = ArgsHandler.INSTANCE.getBindAddress();
        if (bindAddressStr == null || bindAddressStr.isEmpty())
        {
            return null;
        }
        return InetAddress.getByName(bindAddressStr);
    }
}
