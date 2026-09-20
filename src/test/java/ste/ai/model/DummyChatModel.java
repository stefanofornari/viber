/**
 * Copyright 2025 the original author or authors from the Jeddict project (https://jeddict.github.io/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ste.ai.model;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ModelProvider;

import java.util.List;
import java.util.Set;

public class DummyChatModel extends DummyChatModelBase implements ChatModel {

    public DummyChatModel() {
        super();
    }

    @Override
    public ChatResponse doChat(final ChatRequest chatRequest) {
        return computeResponse(chatRequest);
    }

    @Override
    public ChatResponse chat(final ChatRequest chatRequest) {
        final ChatResponse chatResponse = ChatModel.super.chat(chatRequest);

        return chatResponse;
    }

    @Override
    public String chat(final String userMessage) {
        final String chatResponse = ChatModel.super.chat(userMessage);

        return chatResponse;
    }

    @Override
    public ChatResponse chat(final ChatMessage[] messages) {
        final ChatResponse chatResponse = ChatModel.super.chat(messages);

        return chatResponse;
    }

    @Override
    public Set<Capability> supportedCapabilities() {
        final Set capabilities = ChatModel.super.supportedCapabilities();

        return capabilities;
    }

    @Override
    public ChatRequestParameters defaultRequestParameters() {
        final ChatRequestParameters params = ChatModel.super.defaultRequestParameters();

        return params;
    }

    @Override
    public ModelProvider provider() {
        final ModelProvider provider = ChatModel.super.provider();

        return provider;
    }
}
