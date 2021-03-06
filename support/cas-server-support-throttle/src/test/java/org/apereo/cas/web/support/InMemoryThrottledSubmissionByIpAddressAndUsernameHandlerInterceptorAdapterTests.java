package org.apereo.cas.web.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter}.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@TestPropertySource(locations={"classpath:/inmemoryuser.properties"})
@EnableScheduling
@Slf4j
public class InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapterTests
        extends AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapterTests {
}
