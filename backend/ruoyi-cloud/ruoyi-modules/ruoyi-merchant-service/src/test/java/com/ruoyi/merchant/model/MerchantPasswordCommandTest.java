package com.ruoyi.merchant.model;

import static org.junit.Assert.assertEquals;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.Test;

public class MerchantPasswordCommandTest
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void bindsStrictNewPasswordField() throws Exception
    {
        MerchantPasswordCommand command = objectMapper.readValue(
                "{\"newPassword\":\"13067766577\"}", MerchantPasswordCommand.class);

        assertEquals("13067766577", command.getNewPassword());
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void rejectsIncorrectPasswordField() throws Exception
    {
        objectMapper.readValue("{\"password\":\"13067766577\"}", MerchantPasswordCommand.class);
    }
}
