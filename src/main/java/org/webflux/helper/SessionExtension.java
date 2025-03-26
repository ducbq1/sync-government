package org.webflux.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.server.WebSession;
import org.webflux.enumerate.Status;
import org.webflux.model.Toast;

@Log4j2
public class SessionExtension {
    public static void toastMessage(WebSession session, Status status, String title, String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            session.getAttributes().put("toast", objectMapper.writeValueAsString(new Toast(status, title, message)));
        } catch (JsonProcessingException ex) {
            log.error(ex);
        }
    }
}


