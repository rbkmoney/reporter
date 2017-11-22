package com.rbkmoney.reporter.validator;

import com.rbkmoney.reporter.model.ShopAccountingModel;
import org.junit.Test;

import javax.validation.*;
import java.util.Set;

public class ShopAccountingModelValidatorTest {

    @Test
    public void testValidate() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        ShopAccountingModel shopAccountingModel = new ShopAccountingModel();
        shopAccountingModel.setClosingBalance(-1);
        shopAccountingModel.setFeeCharged(-1);
        shopAccountingModel.setFundsAcquired(-1);
        shopAccountingModel.setFundsPaidOut(-1);
        shopAccountingModel.setFundsRefunded(-1);
        shopAccountingModel.setOpeningBalance(-1);

        Set<ConstraintViolation<ShopAccountingModel>> constraintViolations = validator.validate(shopAccountingModel);
        constraintViolations.stream().forEach(violation -> System.out.println(violation));
    }

}
