package dk.statsbiblioteket.newspaper.repocleaner;

public class ConfigConstants {

    /**
     * A comma-separated list of email addresses to which alert messages will be sent.
     */
    public static final String ALERT_EMAIL_ADDRESSES = "email.addresses";
    /**
     * The smtp host.
     */
    public static final String SMTP_HOST = "smtp.host";

    /**
     * The smtp port.
     */
    public static final String SMTP_PORT = "smtp.port";

    /**
     * The "from" email address.
     */
    public static final String EMAIL_FROM_ADDRESS = "email.from.address";

    public static final java.lang.String SUBJECT_PATTERN = "email.subject.pattern";
    public static final java.lang.String BODY_PATTERN = "email.body.pattern";
    public static final java.lang.String EVENT_ID = "autonomous.eventID";
    public static final java.lang.String DOMS_COMMIT_COMMENT = "doms.commit.comment";
    public static final java.lang.String RELATION = "doms.batch.to.roundtrip.relation";
}
