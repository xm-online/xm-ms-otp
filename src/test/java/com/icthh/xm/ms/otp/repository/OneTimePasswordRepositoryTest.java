package com.icthh.xm.ms.otp.repository;

import static org.junit.Assert.assertEquals;

import com.icthh.xm.ms.otp.OtpApp;
import com.icthh.xm.ms.otp.domain.OneTimePassword;
import com.icthh.xm.ms.otp.domain.enumeration.ReceiverTypeKey;
import com.icthh.xm.ms.otp.domain.enumeration.StateKey;
import groovy.util.logging.Slf4j;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {OtpApp.class})
public class OneTimePasswordRepositoryTest {

    @Autowired
    OneTimePasswordRepository oneTimePasswordRepository;

    @Test
    public void testFindTopByReceiverOrderByStartDateDesc_shouldReturnLatestOTP() throws InterruptedException {
        Instant now = Instant.now();
        Thread.sleep(10L); // just in case the test executes the code too fast
        Instant nowTwo = Instant.now();
        Thread.sleep(10L); // just in case the test executes the code too fast
        Instant nowThree = Instant.now();

        String receiver = "123";
        OneTimePassword first = oneTimePassword(receiver, now);
        OneTimePassword second = oneTimePassword(receiver, nowTwo);
        OneTimePassword third = oneTimePassword(receiver, nowThree);
        oneTimePasswordRepository.saveAll(List.of(first, second, third));

        OneTimePassword newestOtp = oneTimePasswordRepository.findTopByReceiverOrderByStartDateDesc(receiver);
        assertEquals(nowThree, newestOtp.getStartDate());
    }

    private OneTimePassword oneTimePassword(String receiver, Instant startDate) {
        Instant endDate = startDate.plusSeconds(120L);
        return new OneTimePassword(
            new Random().nextLong(),
            receiver,
            ReceiverTypeKey.NAME,
            "typeKey",
            StateKey.ACTIVE,
            3,
            startDate,
            endDate,
            "passwordHash"
        );
    }
}
