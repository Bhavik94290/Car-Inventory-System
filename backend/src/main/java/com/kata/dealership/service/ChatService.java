package com.kata.dealership.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.*;
import com.kata.dealership.dto.ChatMessageDto;
import com.kata.dealership.dto.ChatRequest;
import com.kata.dealership.dto.ChatResponse;
import com.kata.dealership.dto.OrderResponse;
import com.kata.dealership.entity.Vehicle;
import com.kata.dealership.exception.ChatServiceException;
import com.kata.dealership.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Executes tool lookups itself and feeds results back as plain context, rather than a full tool_result round-trip.
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String SYSTEM_PROMPT = """
            You are the AI-KATA Car Dealership assistant, embedded in the dealership's website.
            You help visitors search the vehicle inventory and, for logged-in users, check their past orders.

            - Use the search_vehicles tool whenever the user asks about available cars, prices, stock, or categories.
            - Use the get_my_orders tool when the user asks about their own orders, purchases, or payment status.
            - If get_my_orders reports the user is not logged in, tell them to log in first.
            - Prices are in Indian Rupees (INR).
            - Keep replies short, friendly, and focused on this dealership. Politely decline unrelated requests.
            """;

    private final VehicleRepository vehicleRepository;
    private final PaymentService paymentService;

    @Value("${anthropic.api-key}")
    private String apiKey;

    public ChatResponse chat(ChatRequest request, String userEmail) {
        AnthropicClient client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
        List<Vehicle> foundVehicles = new ArrayList<>();

        MessageCreateParams firstPass = buildBaseParams(request)
                .addTool(searchVehiclesTool())
                .addTool(getMyOrdersTool())
                .build();

        Message response;
        try {
            response = client.messages().create(firstPass);
        } catch (Exception ex) {
            throw new ChatServiceException("Could not reach the assistant: " + ex.getMessage());
        }

        if (response.stopReason().isPresent() && response.stopReason().get() == StopReason.TOOL_USE) {
            StringBuilder toolContext = new StringBuilder();
            for (ContentBlock block : response.content()) {
                block.toolUse().ifPresent(toolUse -> {
                    String result = executeTool(toolUse, userEmail, foundVehicles);
                    toolContext.append("[Tool ").append(toolUse.name()).append(" result: ")
                            .append(result).append("]\n");
                });
            }

            MessageCreateParams.Builder followUp = buildBaseParams(request);
            followUp.addUserMessage(toolContext + "\nUsing the information above, answer my previous message.");

            try {
                response = client.messages().create(followUp.build());
            } catch (Exception ex) {
                throw new ChatServiceException("Could not reach the assistant: " + ex.getMessage());
            }
        }

        String reply = response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(TextBlock::text)
                .collect(Collectors.joining("\n"))
                .trim();

        if (reply.isEmpty()) {
            reply = "Sorry, I couldn't come up with a response. Please try rephrasing.";
        }

        return ChatResponse.builder().reply(reply).vehicles(foundVehicles).build();
    }

    private MessageCreateParams.Builder buildBaseParams(ChatRequest request) {
        MessageCreateParams.Builder builder = MessageCreateParams.builder()
                .model(Model.of("claude-opus-4-8"))
                .maxTokens(1024L)
                .system(SYSTEM_PROMPT);

        for (ChatMessageDto m : request.getHistory()) {
            if ("assistant".equalsIgnoreCase(m.getRole())) {
                builder.addAssistantMessage(m.getContent());
            } else {
                builder.addUserMessage(m.getContent());
            }
        }
        builder.addUserMessage(request.getMessage());
        return builder;
    }

    @SuppressWarnings("unchecked")
    private String executeTool(ToolUseBlock toolUse, String userEmail, List<Vehicle> foundVehicles) {
        try {
            Map<String, Object> input = toolUse._input().convert(Map.class);
            return switch (toolUse.name()) {
                case "search_vehicles" -> searchVehicles(input, foundVehicles);
                case "get_my_orders" -> getMyOrders(userEmail);
                default -> "Unknown tool";
            };
        } catch (Exception ex) {
            return "Tool execution failed: " + ex.getMessage();
        }
    }

    private String searchVehicles(Map<String, Object> input, List<Vehicle> foundVehicles) {
        String make = (String) input.get("make");
        String model = (String) input.get("model");
        String category = (String) input.get("category");
        BigDecimal minPrice = input.get("minPrice") != null ? BigDecimal.valueOf(((Number) input.get("minPrice")).doubleValue()) : null;
        BigDecimal maxPrice = input.get("maxPrice") != null ? BigDecimal.valueOf(((Number) input.get("maxPrice")).doubleValue()) : null;
        boolean inStockOnly = Boolean.TRUE.equals(input.get("inStockOnly"));

        Page<Vehicle> page = vehicleRepository.search(
                blankToNull(make), blankToNull(model), blankToNull(category),
                minPrice, maxPrice, inStockOnly,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "price")));

        foundVehicles.addAll(page.getContent());

        if (page.isEmpty()) {
            return "No matching vehicles found.";
        }
        return page.getContent().stream()
                .map(v -> v.getMake() + " " + v.getModel() + " (" + v.getCategory() + "), price "
                        + v.getPrice() + ", " + v.getQuantity() + " in stock")
                .collect(Collectors.joining("; "));
    }

    private String getMyOrders(String userEmail) {
        if (userEmail == null) {
            return "The user is not logged in, so their orders cannot be looked up.";
        }
        List<OrderResponse> orders = paymentService.getOrdersForUser(userEmail);
        if (orders.isEmpty()) {
            return "This user has no orders yet.";
        }
        return orders.stream()
                .map(o -> "Order #" + o.getId() + ": " + o.getStatus() + ", total " + o.getTotalAmount())
                .collect(Collectors.joining("; "));
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private Tool searchVehiclesTool() {
        return Tool.builder()
                .name("search_vehicles")
                .description("Search the car dealership's vehicle inventory by make, model, category, and price range.")
                .inputSchema(Tool.InputSchema.builder()
                        .properties(Tool.InputSchema.Properties.builder()
                                .putAdditionalProperty("make", JsonValue.from(Map.of("type", "string", "description", "Vehicle make, e.g. Toyota")))
                                .putAdditionalProperty("model", JsonValue.from(Map.of("type", "string", "description", "Vehicle model, e.g. Fortuner")))
                                .putAdditionalProperty("category", JsonValue.from(Map.of("type", "string", "description", "Category, e.g. SUV, Sedan, Hatchback")))
                                .putAdditionalProperty("minPrice", JsonValue.from(Map.of("type", "number", "description", "Minimum price in INR")))
                                .putAdditionalProperty("maxPrice", JsonValue.from(Map.of("type", "number", "description", "Maximum price in INR")))
                                .putAdditionalProperty("inStockOnly", JsonValue.from(Map.of("type", "boolean", "description", "Only include vehicles currently in stock")))
                                .build())
                        .build())
                .build();
    }

    private Tool getMyOrdersTool() {
        return Tool.builder()
                .name("get_my_orders")
                .description("Look up the currently logged-in user's past orders and their payment status.")
                .inputSchema(Tool.InputSchema.builder()
                        .properties(Tool.InputSchema.Properties.builder().build())
                        .build())
                .build();
    }
}
