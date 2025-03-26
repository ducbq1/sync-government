package org.webflux.schedule;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;

@Log4j2
//@Configuration
//@EnableScheduling
@Getter
@Setter
public class ScheduledTasks {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedRate = 60000)
    public void reportCurrentTime() {
        log.info("The time is now {}", dateFormat.format(new Date()));
        System.out.println(new Date());
    }

    @Scheduled(fixedRate = 1000)
    public void taskOther() {
        log.warn("The other task");
        System.out.println(new Date());
    }
}