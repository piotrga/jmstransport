package jmstransport;

import java.io.Serializable;

public class JmsConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public final String connectionFactoryUrl;
    public final String username;
    public final String password;


    public JmsConfig(String connectionFactoryUrl, String username, String password) {
        this.connectionFactoryUrl = connectionFactoryUrl;
        this.username = username;
        this.password = password;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        JmsConfig jmsConfig = (JmsConfig) object;

        return connectionFactoryUrl.equals(jmsConfig.connectionFactoryUrl) &&
                password.equals(jmsConfig.password) &&
                username.equals(jmsConfig.username);
    }


    @Override
    public int hashCode() {
        int result = connectionFactoryUrl.hashCode();
        result = 31 * result + username.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }

}
