package com.signature.autosave.modules.pay.payment.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Subscription {
    private String id;
    private Integer version;
    private Long applicationId;
    private Long collectorId;
    private String preapprovalPlanId;
    private String reason;
    private String externalReference;
    private String backUrl;
    private String initPoint;
    private AutoRecurring autoRecurring;
    private Long payerId;
    private String cardId;
    private String paymentMethodId;
    private Date nextPaymentDate;
    private Date dateCreated;
    private Date lastModified;
    private String status;

    @Getter
    @Setter
    public static class AutoRecurring {
        private Integer frequency;
        private String frequencyType;
        private Date startDate;
        private Date endDate;
        private String currencyId;
        private Double transaction_amount;
        /*private FreeTrial freeTrial;*/
    }

    /*
    @Getter
    @Setter
    public static class FreeTrial {
        private Integer frequency;
        private String frequencyType;

    }*/
}
