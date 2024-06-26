package io.github.patternknife.pxbsample.util.auth;

import io.github.patternknife.pxbsample.domain.customer.entity.Customer;
import io.github.patternknife.pxbsample.config.security.principal.AccessTokenUserInfo;

public class UnitMockAuth extends AbstractMockAuth {



    public UnitMockAuth(){

    }

    @Override
    public AccessTokenUserInfo mockAuthenticationPrincipal(Customer customer) {
        return super.mockAuthenticationPrincipal(customer);
    }

    @Override
    public Customer mockCustomerObject() {
        return super.mockCustomerObject();
    }

}
