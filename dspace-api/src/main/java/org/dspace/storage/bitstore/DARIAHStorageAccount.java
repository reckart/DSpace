package org.dspace.storage.bitstore;


/**
 * This class contain all relevant account information for DARIAH Storage Service
 * @author Dieter
 */
public class DARIAHStorageAccount
{

    private final String baseUrl;
    private final String idpUrl;
    private final String username;
    private final String passsword;

    /**
     *<code>DARIAHStorageAccount</code> constructor, there must be at least <code>aBaseUrl</code> argument.
     * @param aBaseUrl
     * @param aIdpUrl
     * @param aUsername
     * @param aPassword
     */
    public DARIAHStorageAccount(String aBaseUrl, String aIdpUrl, String aUsername,
            String aPassword)
    {
        baseUrl = aBaseUrl;
        idpUrl = aIdpUrl;
        username = aUsername;
        passsword = aPassword;

    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public String getIdpUrl()
    {
        return idpUrl;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPasssword()
    {
        return passsword;
    }



}
