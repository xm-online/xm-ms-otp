package com.icthh.xm.ms.otp.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.icthh.xm.ms.otp.OtpApp;
import com.icthh.xm.ms.otp.domain.OneTimePassword;
import com.icthh.xm.ms.otp.domain.enumeration.ReceiverTypeKey;
import com.icthh.xm.ms.otp.domain.enumeration.StateKey;
import groovy.util.logging.Slf4j;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = {OtpApp.class})
public class OneTimePasswordRepositoryTest {

    @Autowired
    OneTimePasswordRepository oneTimePasswordRepository;

    @Test
    public void testFindTopByReceiverOrderByStartDateDesc_shouldReturnLatestOTP() throws InterruptedException {
        // the database keeps microseconds (rounded), Instant.now() on Linux with Java 15+ has nanoseconds
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        Thread.sleep(10L); // just in case the test executes the code too fast
        Instant nowTwo = Instant.now().truncatedTo(ChronoUnit.MICROS);
        Thread.sleep(10L); // just in case the test executes the code too fast
        Instant nowThree = Instant.now().truncatedTo(ChronoUnit.MICROS);

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
        // no preset id: Hibernate 6.6+ rejects merging a new entity whose generated id is already set
        return new OneTimePassword(
            null,
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
