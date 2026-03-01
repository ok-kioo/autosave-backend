package com.signature.autosave.modules.contract.domain.enums;

public enum ContractType {
    SIGNATURE ("PENDING"),
    PAID ("PAID");

    private final String type;

    ContractType(String type) {
        this.type = type;
    }

}
