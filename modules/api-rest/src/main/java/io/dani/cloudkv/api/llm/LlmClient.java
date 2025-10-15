package io.dani.cloudkv.api.llm;

public interface LlmClient {
    String complete(String prompt);
}
