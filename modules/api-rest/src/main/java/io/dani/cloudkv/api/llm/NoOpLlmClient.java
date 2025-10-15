package io.dani.cloudkv.api.llm;

public final class NoOpLlmClient implements LlmClient {
    @Override
    public String complete(String prompt) { return ""; }
}
