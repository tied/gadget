package ut.com.avrethem.rest;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import com.avrethem.rest.dataGenerator;
import com.avrethem.rest.dataGeneratorModel;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericEntity;

public class dataGeneratorTest {

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void messageIsValid() {
        dataGenerator resource = new dataGenerator();

        Response response = resource.getMessage();
        final dataGeneratorModel message = (dataGeneratorModel) response.getEntity();

        assertEquals("wrong message","Hello World",message.getMessage());
    }
}
