package org.example.bot.utils;


import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

/**
 * This is a very simple authentication object that can be used for any transport needing basic userName and password type authentication.
 *
 * @since 1.0
 */
public class DefaultAuthenticator extends Authenticator {

    /**
     * Stores the login information for authentication.
     */
    private final PasswordAuthentication authentication;

    /**
     * Default constructor.
     *
     * @param userName user name to use when authentication is requested
     * @param password password to use when authentication is requested
     * @since 1.0
     */
    public DefaultAuthenticator(final String userName, final String password) {
        this.authentication = new PasswordAuthentication(userName, password);
    }

    /**
     * Gets the authentication object that will be used to login to the mail server.
     *
     * @return A {@code PasswordAuthentication} object containing the login information.
     * @since 1.0
     */
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return authentication;
    }
}
