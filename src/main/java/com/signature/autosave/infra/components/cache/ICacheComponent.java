package com.signature.autosave.infra.components.cache;

public interface ICacheComponent {
    String processIdempotentRequest(String key);
}
