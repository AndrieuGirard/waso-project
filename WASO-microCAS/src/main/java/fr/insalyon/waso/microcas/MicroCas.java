package fr.insalyon.waso.microcas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author WASO Team
 */
public class MicroCas {

    protected static long TICKET_COUNTER = Long.MAX_VALUE / 8 + Math.round((Long.MAX_VALUE / 16) * Math.random());
    protected static Map<String, Map<String, String>> TICKETS = new HashMap<String, Map<String, String>>();

    protected final String ldapServerUrl;

    public MicroCas(String ldapServerUrl) {
        this.ldapServerUrl = ldapServerUrl;
    }

    public Map<String, String> checkLogin(String login, String password) {

        Map<String, String> result = null;

        if (login != null && password != null && login.length() > 0) {

            if ("test".equals(this.ldapServerUrl)) {
                if (login.equals(password)) {
                    result = new HashMap<String, String>();
                    result.put("login", login);
                    result.put("description", "Name of <" + login + ">");
                }
            } else {

                try {
                    Properties props = new Properties();
                    props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                    props.put(Context.PROVIDER_URL, this.ldapServerUrl);

                    String principalName = "cn=" + login + ",dc=mars";
                    props.put(Context.SECURITY_AUTHENTICATION, "simple");
                    props.put(Context.SECURITY_PRINCIPAL, principalName);
                    props.put(Context.SECURITY_CREDENTIALS, password);

                    InitialDirContext context = new InitialDirContext(props);

                    SearchControls ctrls = new SearchControls();
                    ctrls.setReturningAttributes(new String[]{"cn", "uid", "description", "mail"});
                    ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);

                    NamingEnumeration<SearchResult> answers = context.search("dc=mars", "(cn=" + login + ")", ctrls);

                    if (answers.hasMore()) {

                        Map<String, String> user = new HashMap<String, String>();
                        user.put("login", login);

                        Attributes attributes = answers.next().getAttributes();
                        String name = (attributes.get("description") != null ? attributes.get("description").get().toString() : "<description>");
                        System.err.println("LDAP @ " + login + " => " + name);
                        NamingEnumeration<? extends Attribute> allAttributes = attributes.getAll();
                        while (allAttributes.hasMore()) {
                            Attribute attribute = allAttributes.next();

                            user.put(attribute.getID(), attribute.get().toString());

                            NamingEnumeration<?> allValues = attribute.getAll();
                            while (allValues.hasMore()) {
                                Object value = allValues.next();
                                System.err.println(" - " + attribute.getID() + ": " + value.toString());
                            }
                        }

                        result = user;
                    }

                } catch (NamingException ex) {
                    result = null;
                    ex.printStackTrace(System.err);
                }
            }
        }

        return result;
    }

    public String createTicket(String service, Map<String, String> user) {

        String ticket = String.format("%016X", ++TICKET_COUNTER);

        TICKETS.put(ticket, new HashMap<String, String>(user));

        return ticket;
    }

    public Map<String, String> checkTicket(String service, String ticket) {

        Map<String, String> user = TICKETS.remove(ticket);

        return user;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        try {
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            props.put(Context.PROVIDER_URL, "ldap://192.168.56.102"); // IP de la VM LDAP

            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("LDAP Admin pwd > ");

            String adminLogin = "admin";
            String adminPassword = console.readLine();

            String principalName = "cn=" + adminLogin + ",dc=mars";
            props.put(Context.SECURITY_AUTHENTICATION, "simple");
            props.put(Context.SECURITY_PRINCIPAL, principalName);
            props.put(Context.SECURITY_CREDENTIALS, adminPassword);

            InitialDirContext context = new InitialDirContext(props);

            for (int i = 1; i <= 12; i++) {

                String username = "user" + String.format("%02d", i);
                String password = "pwd-" + username;

                try {

                    String entryName = "cn=" + username + ",dc=mars";

                    Attributes entryAttributes = new BasicAttributes();
                    entryAttributes.put(new BasicAttribute("uid", username));
                    entryAttributes.put(new BasicAttribute("userPassword", hashMD5Password(password)));
                    entryAttributes.put(new BasicAttribute("description", "New User " + String.format("%02d", i)));
                    entryAttributes.put(new BasicAttribute("cn", username));

                    BasicAttribute classAttribute = new BasicAttribute("objectClass");
                    classAttribute.add("top");
                    classAttribute.add("uidObject");
                    classAttribute.add("simpleSecurityObject");
                    classAttribute.add("organizationalRole");
                    entryAttributes.put(classAttribute);

                    context.bind(entryName, null, entryAttributes);

                    System.out.println("New User '" + username + "' added !..");

                } catch (NamingException ex) {
                    System.out.println("ERROR: New User '" + username + "' NOT added... " + ex.getMessage());
                }
            }
        } catch (NamingException ex) {
            ex.printStackTrace(System.err);
        }
    }

    public static String hashMD5Password(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(password.getBytes(StandardCharsets.UTF_8));
        String md5Password = Base64.encodeBase64String(digest.digest());
        return "{MD5}" + md5Password;
    }
}
