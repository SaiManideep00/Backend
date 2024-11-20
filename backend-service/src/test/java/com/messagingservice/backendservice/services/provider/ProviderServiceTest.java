package com.messagingservice.backendservice.services.provider;

import com.messagingservice.backendservice.model.provider.Producer;
import com.messagingservice.backendservice.model.provider.ProviderBusinessPOC;
import com.messagingservice.backendservice.model.provider.ProviderTechnicalPOC;
import com.messagingservice.backendservice.util.Util;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.util.AssertionErrors.assertEquals;


@RunWith(SpringRunner.class)

@SpringBootTest
class ProviderServiceTest {
    @Mock
    private ProviderService providerService;
    @Before
    public void setUp() throws Exception {
        //this.mockMvc = MockMvcBuilders.standaloneSetup(providerController).build();

    }
    @Test
    void saveProducer() {
    }

    @Test
    void getProducersTest() throws Exception {
        List<Producer> producers= new ArrayList<Producer>();
        Producer producer1 = new Producer(1, "provider1", true, new ProviderTechnicalPOC("+1 6659086753", "provider1@gmail.com"), new ProviderBusinessPOC("+1 99873737377", "providerBussiness@gmail.com"), "alert1@gmail.com");
        Producer producer2 = new Producer(1, "provider1", true, new ProviderTechnicalPOC("+1 6659086753", "provider1@gmail.com"), new ProviderBusinessPOC("+1 99873737377", "providerBussiness@gmail.com"), "alert1@gmail.com");
        producers.add(producer1);
        producers.add(producer2);
        ResponseEntity<Object> responseEntity = Util.prepareResponse(producers, HttpStatus.OK);
        Mockito.when(providerService.getProducers())
                .thenReturn(responseEntity);

        ResponseEntity<Object> providers = providerService.getProducers();
        List<Producer> producerlist = (List<Producer>) providers.getBody();

        assertEquals("providers", 2, producerlist.size());
        assertEquals(null, HttpStatus.OK, providers.getStatusCode());
        Mockito.verify(providerService).getProducers();
    }

    @Test
    void getProducerById() {
    }
}