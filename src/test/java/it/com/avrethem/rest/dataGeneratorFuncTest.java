package it.com.avrethem.rest;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import com.avrethem.rest.dataGenerator;
import com.avrethem.rest.dataGeneratorModel;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;

public class dataGeneratorFuncTest {

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void messageIsValid() {

        String baseUrl = System.getProperty("baseurl");
        String resourceUrl = baseUrl + "/rest/datagenerator/1.0/message/testmessage";

        RestClient client = new RestClient();
        Resource resource = client.resource(resourceUrl);

        dataGeneratorModel message = resource.get(dataGeneratorModel.class);

        assertEquals("wrong message","Hello World",message.getMessage());
    }
}
