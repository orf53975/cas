package org.apereo.cas.adaptors.ldap.services;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.ldap.services.config.LdapServiceRegistryConfiguration;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.DefaultRegisteredServiceUsernameProvider;
import org.apereo.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * This is {@link BaseLdapServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnableScheduling
@DirtiesContext
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {LdapServiceRegistryConfiguration.class, RefreshAutoConfiguration.class})
public class BaseLdapServiceRegistryTests {
    @Autowired
    @Qualifier("ldapServiceRegistry")
    private ServiceRegistry dao;
    
    @Before
    public void setUp() {
        this.dao.load().forEach(service -> this.dao.delete(service));
    }

    @Test
    public void verifyEmptyRegistry() {
        assertEquals(0, this.dao.load().size());
    }

    @Test
    public void verifyNonExistingService() {
        assertNull(this.dao.findServiceById(9999991));
    }

    @Test
    public void verifySavingServices() {
        this.dao.save(getRegexRegisteredService());
        this.dao.save(getRegexRegisteredService());
        final List<RegisteredService> services = this.dao.load();
        assertEquals(2, services.size());
    }

    @Test
    public void verifyUpdatingServices() {
        this.dao.save(getRegexRegisteredService());
        final List<RegisteredService> services = this.dao.load();

        final AbstractRegisteredService rs = (AbstractRegisteredService) this.dao.findServiceById(services.get(0).getId());
        assertNotNull(rs);
        rs.setEvaluationOrder(9999);
        rs.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        rs.setName("Another Test Service");
        rs.setDescription("The new description");
        rs.setServiceId("https://hello.world");
        rs.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("https"));
        rs.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy());
        assertNotNull(this.dao.save(rs));

        final RegisteredService rs3 = this.dao.findServiceById(rs.getId());
        assertEquals(rs3.getName(), rs.getName());
        assertEquals(rs3.getDescription(), rs.getDescription());
        assertEquals(rs3.getEvaluationOrder(), rs.getEvaluationOrder());
        assertEquals(rs3.getUsernameAttributeProvider(), rs.getUsernameAttributeProvider());
        assertEquals(rs3.getProxyPolicy(), rs.getProxyPolicy());
        assertEquals(rs3.getUsernameAttributeProvider(), rs.getUsernameAttributeProvider());
        assertEquals(rs3.getServiceId(), rs.getServiceId());
    }

    @Test
    public void verifySamlService() {
        final SamlRegisteredService r = new SamlRegisteredService();
        r.setName("verifySamlService");
        r.setServiceId("Testing");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        final Map fmt = new HashMap();
        fmt.put("key", "value");
        r.setAttributeNameFormats(fmt);
        r.setMetadataCriteriaDirection("INCLUDE");
        r.setMetadataCriteriaRemoveEmptyEntitiesDescriptors(true);
        r.setMetadataSignatureLocation("location");
        r.setRequiredAuthenticationContextClass("Testing");
        final SamlRegisteredService r2 = (SamlRegisteredService) this.dao.save(r);
        assertEquals(r, r2);
    }

    @Test
    public void verifyOAuthServices() {
        final OAuthRegisteredService r = new OAuthRegisteredService();
        r.setName("test1456");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        r.setClientId("testoauthservice");
        r.setClientSecret("anothertest");
        r.setBypassApprovalPrompt(true);
        final RegisteredService r2 = this.dao.save(r);
        assertEquals(r, r2);
    }

    @Test
    public void verifySavingServiceChangesDn() {
        this.dao.save(getRegexRegisteredService());
        final List<RegisteredService> services = this.dao.load();

        final AbstractRegisteredService rs = (AbstractRegisteredService) this.dao.findServiceById(services.get(0).getId());
        final long originalId = rs.getId();
        assertNotNull(rs);
        rs.setId(666);
        assertNotNull(this.dao.save(rs));
        assertNotEquals(rs.getId(), originalId);
    }

    @Test
    public void verifyDeletingSingleService() {
        final RegisteredService rs = getRegexRegisteredService();
        final RegisteredService rs2 = getRegexRegisteredService();
        this.dao.save(rs2);
        this.dao.save(rs);
        this.dao.load();
        this.dao.delete(rs2);

        final List<RegisteredService> services = this.dao.load();
        assertEquals(1, services.size());
        assertEquals(services.get(0).getId(), rs.getId());
        assertEquals(services.get(0).getName(), rs.getName());
    }

    @Test
    public void verifyDeletingServices() {
        this.dao.save(getRegexRegisteredService());
        this.dao.save(getRegexRegisteredService());
        final List<RegisteredService> services = this.dao.load();
        services.forEach(registeredService -> this.dao.delete(registeredService));
        assertEquals(0, this.dao.load().size());
    }

    private static RegisteredService getRegexRegisteredService() {
        final AbstractRegisteredService rs = new RegexRegisteredService();
        rs.setName("Service Name Regex");
        rs.setProxyPolicy(new RefuseRegisteredServiceProxyPolicy());
        rs.setUsernameAttributeProvider(new AnonymousRegisteredServiceUsernameAttributeProvider(
            new ShibbolethCompatiblePersistentIdGenerator("hello")
        ));
        rs.setDescription("Service description");
        rs.setServiceId("^http?://.+");
        rs.setTheme("the theme name");
        rs.setEvaluationOrder(123);
        rs.setDescription("Here is another description");
        rs.setRequiredHandlers(CollectionUtils.wrapHashSet("handler1", "handler2"));

        final Map<String, RegisteredServiceProperty> propertyMap = new HashMap<>();
        final DefaultRegisteredServiceProperty property = new DefaultRegisteredServiceProperty();

        final Set<String> values = new HashSet<>();
        values.add("value1");
        values.add("value2");
        property.setValues(values);
        propertyMap.put("field1", property);
        rs.setProperties(propertyMap);

        return rs;
    }
}
