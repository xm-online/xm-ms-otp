package com.icthh.xm.ms.otp.web.rest.view;

import com.icthh.xm.ms.otp.service.LoginPageRefreshableConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@RequiredArgsConstructor
public class LoginPageView implements View {

    private final LoginPageRefreshableConfiguration loginPageRefreshableConfiguration;

    @Override
    public String getContentType() {
        return MediaType.TEXT_HTML_VALUE;
    }

    @Override
    public void render(Map<String, ?> map, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        if (loginPageRefreshableConfiguration.getLoginContent() == null){
            httpServletResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        httpServletResponse.setContentType(getContentType());
        PrintWriter writer = httpServletResponse.getWriter();
        writer.write(loginPageRefreshableConfiguration.getLoginContent());
    }


}
