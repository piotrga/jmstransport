package jmstransport;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class JmsConfig {

    public final String connectionFactoryUrl;
    public final String username;
    public final String password;

    public JmsConfig(String connectionFactoryUrl, String username, String password) {
        this.connectionFactoryUrl = connectionFactoryUrl;
        this.username = username;
        this.password = password;
    }

    @Override public boolean equals(Object object) { return EqualsBuilder.reflectionEquals(this, object); }
    @Override public int hashCode() { return HashCodeBuilder.reflectionHashCode(this); }
}
