package org.example.bot.utils;

import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import lombok.Getter;
import org.example.bot.utils.activation.InputStreamDataSource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class MimeMessageParser {

    /**
     * The MimeMessage to convert.
     */
    @Getter
    private final MimeMessage mimeMessage;

    /**
     * Plain mail content from MimeMessage.
     */
    @Getter
    private String plainContent;

    /**
     * HTML mail content from MimeMessage.
     */
    @Getter
    private String htmlContent;

    @Getter
    private Multipart multipartContent;

    /**
     * List of attachments of MimeMessage.
     */
    @Getter
    private final List<DataSource> attachmentList;

    /**
     * Attachments stored by their content-id.
     */
    private final Map<String, DataSource> cidMap;

    /**
     * Is this a Multipart email.
     */
    private boolean isMultiPart;

    /**
     * Constructs an instance with the MimeMessage to be extracted.
     *
     * @param mimeMessage the message to parse
     */
    public MimeMessageParser(final MimeMessage mimeMessage) {
        this.attachmentList = new ArrayList<>();
        this.cidMap = new HashMap<>();
        this.mimeMessage = mimeMessage;
        this.isMultiPart = false;
    }

    private List<Address> asList(final Address[] recipients) {
        return recipients != null ? Arrays.asList(recipients) : new ArrayList<>();
    }

    /**
     * Parses the MimePart to create a DataSource.
     *
     * @param part the current part to be processed
     * @return the DataSource
     * @throws MessagingException creating the DataSource failed
     * @throws IOException        error getting InputStream or unsupported encoding
     */
    @SuppressWarnings("resource") // Caller closes InputStream
    protected DataSource createDataSource(final MimePart part) throws MessagingException, IOException {
        final DataSource dataSource = part.getDataHandler().getDataSource();
        final String contentType = getBaseMimeType(dataSource.getContentType());
        final String dataSourceName = getDataSourceName(part, dataSource);
        return new InputStreamDataSource(dataSource.getInputStream(), contentType, dataSourceName);
    }

    /**
     * Find an attachment using its content-id.
     * <p>
     * The content-id must be stripped of any angle brackets, i.e. "part1" instead of "&lt;part1&gt;".
     * </p>
     *
     * @param cid the content-id of the attachment
     * @return the corresponding datasource or null if nothing was found
     * @since 1.3.4
     */
    public DataSource findAttachmentByCid(final String cid) {
        return cidMap.get(cid);
    }

    /**
     * Find an attachment using its name.
     *
     * @param name the name of the attachment
     * @return the corresponding datasource or null if nothing was found
     */
    public DataSource findAttachmentByName(final String name) {
        for (final DataSource dataSource : getAttachmentList()) {
            if (name.equalsIgnoreCase(dataSource.getName())) {
                return dataSource;
            }
        }
        return null;
    }

    /**
     * Gets the MIME type.
     *
     * @param fullMimeType the mime type from the mail API
     * @return the real mime type
     */
    private String getBaseMimeType(final String fullMimeType) {
        final int pos = fullMimeType.indexOf(';');
        return pos < 0 ? fullMimeType : fullMimeType.substring(0, pos);
    }

    /**
     * Gets the BCC Address list.
     *
     * @return the 'BCC' recipients of the message
     * @throws MessagingException determining the recipients failed
     */
    public List<Address> getBcc() throws MessagingException {
        return asList(mimeMessage.getRecipients(Message.RecipientType.BCC));
    }

    /**
     * Gets the CC Address list.
     *
     * @return the 'CC' recipients of the message
     * @throws MessagingException determining the recipients failed
     */
    public List<Address> getCc() throws MessagingException {
        return asList(mimeMessage.getRecipients(Message.RecipientType.CC));
    }

    /**
     * Returns a collection of all content-ids in the parsed message.
     * <p>
     * The content-ids are stripped of any angle brackets, i.e. "part1" instead of "&lt;part1&gt;".
     * </p>
     *
     * @return the collection of content ids.
     * @since 1.3.4
     */
    public Collection<String> getContentIds() {
        return Collections.unmodifiableSet(cidMap.keySet());
    }

    /**
     * Determines the name of the data source if it is not already set.
     *
     * @param part       the mail part
     * @param dataSource the data source
     * @return the name of the data source or {@code null} if no name can be determined
     * @throws MessagingException           accessing the part failed
     * @throws UnsupportedEncodingException decoding the text failed
     */
    protected String getDataSourceName(final Part part, final DataSource dataSource) throws MessagingException, UnsupportedEncodingException {
        String result = dataSource.getName();
        if (isEmpty(result)) {
            result = part.getFileName();
        }
        if (!isEmpty(result)) {
            result = MimeUtility.decodeText(result);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Gets the FROM field.
     *
     * @return the FROM field of the message
     * @throws MessagingException parsing the mime message failed
     */
    public String getFrom() throws MessagingException {
        final Address[] addresses = mimeMessage.getFrom();
        if (isEmpty(addresses)) {
            return null;
        }
        return ((InternetAddress) addresses[0]).getAddress();
    }

    /**
     * Gets the 'replyTo' address of the email.
     *
     * @return the 'replyTo' address of the email
     * @throws MessagingException parsing the mime message failed
     */
    public String getReplyTo() throws MessagingException {
        final Address[] addresses = mimeMessage.getReplyTo();
        if (isEmpty(addresses)) {
            return null;
        }
        return ((InternetAddress) addresses[0]).getAddress();
    }

    /**
     * Gets the MIME message subject.
     *
     * @return the MIME message subject.
     * @throws MessagingException parsing the mime message failed.
     */
    public String getSubject() throws MessagingException {
        return mimeMessage.getSubject();
    }

    /**
     * Gets the MIME message 'to' list.
     *
     * @return the 'to' recipients of the message.
     * @throws MessagingException determining the recipients failed
     */
    public List<Address> getTo() throws MessagingException {
        return asList(mimeMessage.getRecipients(Message.RecipientType.TO));
    }

    /**
     * Tests if attachments are present.
     *
     * @return true if attachments are present.
     */
    public boolean hasAttachments() {
        return !attachmentList.isEmpty();
    }

    /**
     * Tests is HTML content is present.
     *
     * @return true if HTML content is present.
     */
    public boolean hasHtmlContent() {
        return htmlContent != null;
    }

    /**
     * Tests is plain content is present.
     *
     * @return true if a plain content is present.
     */
    public boolean hasPlainContent() {
        return plainContent != null;
    }

    private boolean isEmpty(final Object[] array) {
        return array == null || array.length == 0;
    }

    private boolean isEmpty(final String result) {
        return result == null || result.isEmpty();
    }

    /**
     * Tests whether the MimePart contains an object of the given mime type.
     *
     * @param part     the current MimePart
     * @param mimeType the mime type to check
     * @return {@code true} if the MimePart matches the given mime type, {@code false} otherwise
     * @throws MessagingException parsing the MimeMessage failed
     */
    public boolean isMimeType(final MimePart part, final String mimeType) throws MessagingException {
        // Do not use part.isMimeType(String) as it is broken for MimeBodyPart
        // and does not really check the actual content type.
        try {
            return new ContentType(part.getDataHandler().getContentType()).match(mimeType);
        } catch (final ParseException ex) {
            return part.getContentType().equalsIgnoreCase(mimeType);
        }
    }

    public String getMimeTypeSubtype(final MimePart part) throws MessagingException {
        try {
            return new ContentType(part.getDataHandler().getContentType()).getSubType();
        } catch (final ParseException ex) {
            String[] parts = part.getContentType().toLowerCase().split("/");
            if (parts.length == 2) {
                return parts[1];
            }
            return null;
        }
    }

    /**
     * Tests whether this is multipart.
     *
     * @return Returns the isMultiPart.
     */
    public boolean isMultipart() {
        return isMultiPart;
    }

    /**
     * Does the actual extraction.
     *
     * @return this instance
     * @throws MessagingException parsing the mime message failed
     * @throws IOException        parsing the mime message failed
     */
    public MimeMessageParser parse() throws MessagingException, IOException {
        parse(null, mimeMessage);
        return this;
    }

    /**
     * Extracts the content of a MimeMessage recursively.
     *
     * @param parent the parent multi-part
     * @param part   the current MimePart
     * @throws MessagingException parsing the MimeMessage failed
     * @throws IOException        parsing the MimeMessage failed
     */
    protected void parse(final Multipart parent, final MimePart part) throws MessagingException, IOException {
        if (isMimeType(part, "text/plain") && plainContent == null && !Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
            plainContent = (String) part.getContent();
            if (parent != null) {
                BodyPart bodyPart = new MimeBodyPart();
                bodyPart.setContent(part.getContent(), part.getContentType());
                parent.addBodyPart(bodyPart);
            }
        } else if (isMimeType(part, "text/html") && htmlContent == null && !Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
            htmlContent = (String) part.getContent();
            if (parent != null) {
                BodyPart bodyPart = new MimeBodyPart();
                bodyPart.setContent(part.getContent(), part.getContentType());
                parent.addBodyPart(bodyPart);
            }
        } else if (isMimeType(part, "multipart/*")) {
            isMultiPart = true;
            final Multipart multipart = (Multipart) part.getContent();
            Multipart copy;
            if (parent == null) {
                copy = multipartContent = new MimeMultipart(getMimeTypeSubtype(part));
            } else {
                copy = new MimeMultipart(getMimeTypeSubtype(part));
                BodyPart bodyPart = new MimeBodyPart();
                bodyPart.setContent(copy);
                multipartContent.addBodyPart(bodyPart);
            }
            final int count = multipart.getCount();
            // iterate over all MimeBodyPart
            for (int i = 0; i < count; i++) {
                parse(copy, (MimeBodyPart) multipart.getBodyPart(i));
            }
        } else {
            final String cid = stripContentId(part.getContentID());
            final DataSource dataSource = createDataSource(part);
            if (cid != null) {
                cidMap.put(cid, dataSource);
            }
            attachmentList.add(dataSource);
        }
    }

    /**
     * Strips the content id of any whitespace and angle brackets.
     *
     * @param contentId the string to strip
     * @return a stripped version of the content id
     */
    private String stripContentId(final String contentId) {
        return contentId == null ? null : contentId.trim().replaceAll("[\\<\\>]", "");
    }
}
