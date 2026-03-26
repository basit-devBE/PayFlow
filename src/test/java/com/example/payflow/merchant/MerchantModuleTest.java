package com.example.payflow.merchant;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.test.ApplicationModuleTest;

@ApplicationModuleTest
class MerchantModuleTest {

    @Test
    void verifyModuleStructure() {
        ApplicationModules.of(com.example.payflow.PayFlowApplication.class).verify();
    }
}
