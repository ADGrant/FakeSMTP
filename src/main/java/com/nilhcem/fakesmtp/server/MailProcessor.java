package com.nilhcem.fakesmtp.server;

import java.io.InputStream;

public interface MailProcessor
{
    void processEmailAndNotify(String from, String to, InputStream data);

    void deleteEmails();
}
