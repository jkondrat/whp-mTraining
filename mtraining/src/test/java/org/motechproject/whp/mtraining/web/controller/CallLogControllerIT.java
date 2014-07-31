package org.motechproject.whp.mtraining.web.controller;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.security.model.PermissionDto;
import org.motechproject.security.model.RoleDto;
import org.motechproject.security.service.MotechPermissionService;
import org.motechproject.security.service.MotechRoleService;
import org.motechproject.security.service.MotechUserService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.TestContext;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.whp.mtraining.domain.Location;
import org.motechproject.whp.mtraining.domain.Provider;
import org.motechproject.whp.mtraining.service.CallDurationService;
import org.motechproject.whp.mtraining.service.CallLogService;
import org.motechproject.whp.mtraining.service.ProviderService;
import org.motechproject.whp.mtraining.web.domain.ProviderStatus;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;


@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class CallLogControllerIT extends BasePaxIT {

    @Inject
    private ProviderService providerService;

    @Inject
    private CallLogService callLogService;

    @Inject
    private CallDurationService callDurationService;

    @Inject
    MotechPermissionService permissions;

    @Inject
    MotechRoleService roles;

    @Inject
    MotechUserService users;

    private Long providerId;

    private static final String CALLLOG_URL = "http://localhost:%d/motech-platform-server/module/mtraining/web-api/callLog";
    private static final String PERMISSION_NAME = "test-permission";
    private static final String ROLE_NAME = "test-role";
    private static final String SECURITY_ADMIN = "Security Admin";
    private static final String USER_NAME = "whp";
    private static final String USER_PASSWORD = "whp";
    private static final String USER_EMAIL = "whp-test@email.com";
    private static final String USER_EXTERNAL_ID = "test-externalId";
    private static final Locale USER_LOCALE = Locale.ENGLISH;
    private static final String BUNDLE_NAME = "bundle";

    @Before
    public void before() {
        PermissionDto permission = new PermissionDto(PERMISSION_NAME, BUNDLE_NAME);
        RoleDto role = new RoleDto(ROLE_NAME, Arrays.asList(PERMISSION_NAME));
        permissions.addPermission(permission);
        roles.createRole(role);
        users.register(USER_NAME, USER_PASSWORD, USER_EMAIL, USER_EXTERNAL_ID, Arrays.asList(ROLE_NAME, SECURITY_ADMIN), USER_LOCALE);
    }

    @Test
    public void shouldCreateProvider() throws Exception {
        Provider provider = providerService.createProvider(new Provider("r003", 9934793802l, ProviderStatus.WORKING_PROVIDER, new Location("Bihar")));
        assertNotNull(provider);
    }

    @Test
    public void shouldPostCallLogs() throws Exception {

        assertThat(callDurationService.getAllCallDurations().size(), Is.is(0));
        assertThat(callLogService.getAllCallLog().size(), Is.is(0));

        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("test-call-log-post.json");
        assertNotNull(resourceAsStream);
        String postContent = IOUtils.toString(resourceAsStream);

        HttpPost request = new HttpPost(String.format(CALLLOG_URL, TestContext.getJettyPort()));
        addAuthHeader(request, USER_NAME, USER_PASSWORD);

        StringEntity entity = new StringEntity(postContent, "application/json", "UTF-8");
        request.setEntity(entity);

        HttpResponse response = getHttpClient().execute(request);
        assertEquals(200, response.getStatusLine().getStatusCode());

        assertThat(callDurationService.getAllCallDurations().size(), Is.is(1));
        assertThat(callLogService.getAllCallLog().size(), Is.is(3));
    }
    
    private void addAuthHeader(HttpUriRequest request, String userName, String password) {
        request.addHeader("Authorization", "Basic " + new String(Base64.encodeBase64((userName + ":" + password).getBytes())));
    }

}
