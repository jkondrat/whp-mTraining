package org.motechproject.whp.mtraining.service.impl;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.whp.mtraining.domain.Location;
import org.motechproject.whp.mtraining.domain.Provider;
import org.motechproject.whp.mtraining.web.domain.ProviderStatus;
import org.motechproject.whp.mtraining.web.domain.ResponseStatus;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.motechproject.whp.mtraining.web.domain.ResponseStatus.NOT_WORKING_PROVIDER;
import static org.motechproject.whp.mtraining.web.domain.ResponseStatus.UNKNOWN_PROVIDER;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class ProviderServiceImplIT extends BasePaxIT {

    @Inject
    private ProviderServiceImpl providerService;

    @Test
    public void shouldAddProvider() {
        Provider provider = new Provider("remediId", 654654l, ProviderStatus.WORKING_PROVIDER, new Location("block", "district", "state"));
        Provider persistedProvider = mock(Provider.class);
        when(providerService.createProvider(provider)).thenReturn(persistedProvider);
        when(persistedProvider.getId()).thenReturn(100l);

        Long id = providerService.createProvider(provider).getId();

        assertThat(id, Is.is(100l));
        verify(providerService).createProvider(provider);
    }

    @Test
    public void shouldDeleteProvider() {
        Provider provider = providerService.getProviderById(100L);
        providerService.deleteProvider(provider);
        verify(providerService).deleteProvider(provider);
    }

    @Test
    public void shouldMarkCallerAsUnidentifiedIfCallerIdNotRegistered() {
        long callerId = 76465464L;
        when(providerService.getProviderByCallerId(callerId)).thenReturn(null);

        ResponseStatus responseStatus = providerService.validateProvider(callerId);

        verify(providerService).getProviderByCallerId(callerId);
        assertEquals(responseStatus.getCode(), UNKNOWN_PROVIDER.getCode());
    }

    @Test
    public void shouldMarkErrorIfProviderIsNotValid() {
        long callerId = 76465464L;
        Provider provider = new Provider("remediId", callerId, ProviderStatus.NOT_WORKING_PROVIDER, new Location("block", "district", "state"));
        when(providerService.getProviderByCallerId(callerId)).thenReturn(provider);

        ResponseStatus response = providerService.validateProvider(callerId);

        verify(providerService).getProviderByCallerId(callerId);
        assertEquals(response, NOT_WORKING_PROVIDER);
    }

    @Test
    public void shouldAddAndRetrieveAProvider() {
        String remediId = "remedix";
        assertThat(providerService.getProviderByRemediId(remediId), IsNull.nullValue());
        Provider provider = new Provider(remediId, 717777L, ProviderStatus.WORKING_PROVIDER, new Location("block", "district", "state"));

        providerService.createProvider(provider);

        Provider savedProvider = providerService.getProviderByRemediId(remediId);
        assertThat(savedProvider, IsNull.notNullValue());
        assertThat(savedProvider.getCallerId(), Is.is(717777L));
    }

    @Test
    public void shouldUpdateAndRetrieveAProvider() {
        long callerId = 7657667L;
        long callerIdNew = 7653333L;
        String remediId = "remediId";

        Provider oldProvider = new Provider(remediId, callerId, ProviderStatus.WORKING_PROVIDER, new Location("block", "district", "state"));
        providerService.createProvider(oldProvider);

        Provider newProvider = new Provider(remediId, callerIdNew, ProviderStatus.WORKING_PROVIDER, new Location("block", "district", "state"));
        providerService.createProvider(newProvider);

        Provider savedProvider = providerService.getProviderByRemediId(remediId);
        assertThat(savedProvider, IsNull.notNullValue());
        assertThat(savedProvider.getRemediId(), Is.is(remediId));
        assertThat(savedProvider.getCallerId(), Is.is(callerIdNew));
    }

}
