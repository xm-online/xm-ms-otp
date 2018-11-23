package com.icthh.xm.ms.otp.cucumber.stepdefs;

import com.icthh.xm.ms.otp.OtpApp;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import org.springframework.boot.test.context.SpringBootTest;

@WebAppConfiguration
@SpringBootTest
@ContextConfiguration(classes = OtpApp.class)
public abstract class StepDefs {

    protected ResultActions actions;

}
