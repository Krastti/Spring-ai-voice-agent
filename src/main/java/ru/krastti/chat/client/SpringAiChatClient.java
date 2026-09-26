package ru.krastti.chat.client;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;
import ru.krastti.chat.config.AgentLoopConfig;
import ru.krastti.chat.exception.ChatProviderException;
import ru.krastti.weather.tool.WeatherTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Реализация клиента чата приложения на Spring AI.
 * <p>
 * Цикл взаимодействия модели и инструментов выполняется кодом приложения. Каждый вызов
 * начинает новую историю сообщений, поэтому сообщения не переносятся между запросами.</p>
 */
@Component
public class SpringAiChatClient implements AiChatClient {

    /** Максимальное количество обращений к модели для одного сообщения пользователя. */
    private static final int MAX_MODEL_CALLS = 5;

    /** Модель, которая формирует итоговый ответ или запрашивает вызов инструмента. */
    private final ChatModel chatModel;

    /** Выполняет запрошенные моделью инструменты и добавляет их результаты в историю. */
    private final ToolCallingManager toolCallingManager;

    /** Описания инструментов, доступные модели в каждом запросе. */
    private final ToolCallback[] toolCallbacks;

    /**
     * Создаёт клиент с настроенной моделью чата, исполнителем вызовов и инструментом погоды.
     *
     * @param chatModel модель для каждой итерации цикла агента
     * @param toolCallingManager компонент, который находит и запускает запрошенные моделью инструменты
     * @param weatherTools компонент с доступным инструментом погоды
     */
    public SpringAiChatClient(ChatModel chatModel, ToolCallingManager toolCallingManager, WeatherTools weatherTools) {
        this.chatModel = chatModel;
        this.toolCallingManager = toolCallingManager;
        this.toolCallbacks = ToolCallbacks.from(weatherTools);
    }

    /**
     * Передаёт сообщение пользователя в цикл модели и инструментов и возвращает итоговый текст модели.
     *
     * <p>Первый запрос содержит системные инструкции и сообщение пользователя. Если модель
     * запрашивает инструмент, приложение выполняет его и добавляет результат в следующий запрос.
     * Допускается не более {@value #MAX_MODEL_CALLS} обращений к модели. Если последний разрешённый
     * ответ содержит запрос инструмента, инструмент не запускается: оставшихся обращений к модели
     * недостаточно для формирования итогового ответа.</p>
     *
     * @param message непустое сообщение пользователя для обработки
     * @return непустой итоговый ответ модели
     * @throws ChatProviderException если ответ модели пуст или лимит обращений исчерпан до получения
     *                               итогового ответа
     */
    @Override
    public String call(String message) {
        if (!(chatModel.getOptions() instanceof ToolCallingChatOptions modelOptions)) {
            throw new IllegalStateException("Configured chat model does not support tool calling options");
        }
        ToolCallingChatOptions options = modelOptions.mutate()
                .toolCallbacks(toolCallbacks)
                .build();
        if (!(options instanceof OpenAiChatOptions)) {
            throw new IllegalStateException("Configured OpenAI chat model requires OpenAiChatOptions");
        }
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(AgentLoopConfig.SYSTEM_PROMPT));
        messages.add(new UserMessage(message));
        Prompt prompt = new Prompt(messages, options);

        for (int modelCall = 1; modelCall <= MAX_MODEL_CALLS; modelCall++) {
            ChatResponse response = chatModel.call(prompt);
            //noinspection ConstantValue
            if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
                throw new ChatProviderException("AI provider returned an empty response");
            }

            if (!response.hasToolCalls()) {
                String content = response.getResult().getOutput().getText();
                if (content == null || content.isBlank()) {
                    throw new ChatProviderException("AI provider returned an empty response");
                }
                return content;
            }

            if (modelCall == MAX_MODEL_CALLS) {
                throw new ChatProviderException("Agent reached the maximum number of model calls");
            }

            ToolExecutionResult result = toolCallingManager.executeToolCalls(prompt, response);
            prompt = new Prompt(result.conversationHistory(), options);
        }

        throw new IllegalStateException("Agent loop exited without a final answer");
    }
}
