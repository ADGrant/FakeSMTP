package com.nilhcem.fakesmtp.headless;

import com.nilhcem.fakesmtp.core.I18n;
import com.nilhcem.fakesmtp.model.EmailModel;
import com.nilhcem.fakesmtp.server.MailProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.mail.*;

public class MailPublisher extends Observable implements MailProcessor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MailPublisher.class);
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    // This can be a static variable since it is Thread Safe
    private static final Pattern SUBJECT_PATTERN = Pattern.compile("^Subject: (.*)$");

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyhhmmssSSS");

    /**
     * Saves incoming email in file system and notifies observers.
     *
     * @param from the user who send the email.
     * @param to the recipient of the email.
     * @param data an InputStream object containing the email.
     */
    public void processEmailAndNotify(String from, String to, InputStream data) {
        try
        {
            String mailContent = convertStreamToString(data);
            Email email = new SimpleEmail();
            email.setHostName("localhost");
            email.setSmtpPort(2550);
            email.setStartTLSEnabled(true);
            email.setFrom(from);
            email.addTo(to);
            email.setSubject(getSubjectFromStr(mailContent));
            email.setMsg(getBodyFromStr(mailContent));
            email.send();
        }
        catch (EmailException e)
        {
            e.printStackTrace();
        }


    }

    /**
     * Deletes all received emails from file system.
     */
    public void deleteEmails() {

    }

    /**
     * Converts an {@code InputStream} into a {@code String} object.
     * <p>
     * The method will not copy the first 4 lines of the input stream.<br>
     * These 4 lines are SubEtha SMTP additional information.
     * </p>
     *
     * @param is the InputStream to be converted.
     * @return the converted string object, containing data from the InputStream passed in parameters.
     */
    private String convertStreamToString(InputStream is) {
        final long lineNbToStartCopy = 4; // Do not copy the first 4 lines (received part)
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName(I18n.UTF8)));
        StringBuilder sb = new StringBuilder();

        String line;
        long lineNb = 0;
        try {
            while ((line = reader.readLine()) != null) {
                if (++lineNb > lineNbToStartCopy) {
                    sb.append(line).append(LINE_SEPARATOR);
                }
            }
        } catch (IOException e) {
            LOGGER.error("", e);
        }
        return sb.toString();
    }



    /**
     * Gets the subject from the email data passed in parameters.
     *
     * @param data a string representing the email content.
     * @return the subject of the email, or an empty subject if not found.
     */
    private String getSubjectFromStr(String data) {
        try {
            BufferedReader reader = new BufferedReader(new StringReader(data));

            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = SUBJECT_PATTERN.matcher(line);
                if (matcher.matches()) {
                    return matcher.group(1);
                }
            }
        } catch (IOException e) {
            LOGGER.error("", e);
        }
        return "";
    }

    private String getBodyFromStr(String mailContent)
    {
        var lines = mailContent.split("\n");
        int lineIdx = 0;
        for (; lineIdx < lines.length; lineIdx++) {
            if (lines[lineIdx].startsWith("Content-Transfer-Encoding:"))
            {
                break;
            }
        }
        lineIdx++;
        var buffer = new StringBuffer();
        for (; lineIdx < lines.length; lineIdx++)
        {
            buffer.append(lines[lineIdx]).append("\n");
        }
        return buffer.toString();
    }
}
