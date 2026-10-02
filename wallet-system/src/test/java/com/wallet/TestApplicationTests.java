package com.wallet;

import com.wallet.dto.CreateUserAccountRequest;
import com.wallet.dto.DoTransDto;
import com.wallet.dto.LoginRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TestApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private final Map<String, String> tokensByAccount = new HashMap<>();

	@Test
	void contextLoads() {
	}

	@Test
	void shouldCreateUserAndTransferFundsSuccessfully() throws Exception {
		String sourceAccount = createUser("source@example.com", new BigDecimal("100.00"));
		String destinationAccount = createUser("destination@example.com", new BigDecimal("25.00"));

		DoTransDto transfer = new DoTransDto("TXN-1001", sourceAccount, destinationAccount, new BigDecimal("40.00"));

		mockMvc.perform(authorizedTransfer(transfer)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(transfer)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fromAccount").value(sourceAccount))
				.andExpect(jsonPath("$.toAccount").value(destinationAccount))
				.andExpect(jsonPath("$.fromBalance").value(60.00))
				.andExpect(jsonPath("$.toBalance").value(65.00));

		mockMvc.perform(balance(sourceAccount))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.balance").value(60.00));
	}

	@Test
	void shouldRejectTransferWhenInsufficientFunds() throws Exception {
		String sourceAccount = createUser("poor@example.com", new BigDecimal("10.00"));
		String destinationAccount = createUser("rich@example.com", new BigDecimal("5.00"));

		DoTransDto transfer = new DoTransDto("TXN-1002", sourceAccount, destinationAccount, new BigDecimal("20.00"));

		mockMvc.perform(authorizedTransfer(transfer)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(transfer)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Insufficient funds in account: " + sourceAccount));
	}

	@Test
	void shouldRejectInvalidTransferAmount() throws Exception {
		String sourceAccount = createUser("amount-a@example.com", new BigDecimal("10.00"));
		String destinationAccount = createUser("amount-b@example.com", new BigDecimal("5.00"));

		DoTransDto transfer = new DoTransDto("TXN-1003", sourceAccount, destinationAccount, BigDecimal.ZERO);

		mockMvc.perform(authorizedTransfer(transfer)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(transfer)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldRejectAmountWithMoreThanTwoDecimalPlaces() throws Exception {
		String sourceAccount = createUser("scale-a@example.com", new BigDecimal("10.00"));
		String destinationAccount = createUser("scale-b@example.com", new BigDecimal("5.00"));
		DoTransDto transfer = new DoTransDto("TXN-SCALE-1", sourceAccount, destinationAccount, new BigDecimal("1.234"));

		mockMvc.perform(authorizedTransfer(transfer)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(transfer)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Amount must have at most 2 decimal places"));
	}

	@Test
	void shouldReturnNotFoundForMissingAccount() throws Exception {
		String account = createUser("missing-test@example.com", BigDecimal.ZERO);
		mockMvc.perform(get("/api/wallet/accounts/{accountNumber}/balance", "ACC-MISSING").header("Authorization", "Bearer " + tokensByAccount.get(account))
					)
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldRejectDuplicateEmailCreation() throws Exception {
		CreateUserAccountRequest request = new CreateUserAccountRequest("dupe@example.com", "Test-Password-123", new BigDecimal("10.00"));

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("User already exists for email: dupe@example.com"));
	}

	@Test
	void shouldRejectTransferToSameAccount() throws Exception {
		String accountNumber = createUser("self@example.com", new BigDecimal("50.00"));
		DoTransDto transfer = new DoTransDto("TXN-1004", accountNumber, accountNumber, new BigDecimal("10.00"));

		mockMvc.perform(authorizedTransfer(transfer)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(transfer)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Source and destination accounts cannot be the same"));
	}

	@Test
	void shouldRejectBlankEmailOnCreateUser() throws Exception {
		CreateUserAccountRequest request = new CreateUserAccountRequest(" ", "Test-Password-123", new BigDecimal("10.00"));

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldRejectBlankSourceAccountOnTransfer() throws Exception {
		String destinationAccount = createUser("valid-destination@example.com", new BigDecimal("15.00"));
		DoTransDto transfer = new DoTransDto("TXN-1005", " ", destinationAccount, new BigDecimal("5.00"));

		mockMvc.perform(authorizedTransfer(transfer)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(transfer)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldBeIdempotentForDuplicateTransactionReference() throws Exception {
		String sourceAccount = createUser("idem-source@example.com", new BigDecimal("100.00"));
		String destinationAccount = createUser("idem-destination@example.com", new BigDecimal("10.00"));

		DoTransDto transfer = new DoTransDto("TXN-IDEMPOTENT-1", sourceAccount, destinationAccount, new BigDecimal("30.00"));

		mockMvc.perform(authorizedTransfer(transfer)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(transfer)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fromBalance").value(70.00))
				.andExpect(jsonPath("$.toBalance").value(40.00));

		// Simulates partial failure case: first transfer committed but client retries
		// because it did not receive response (timeout/network). No second debit should happen.
		mockMvc.perform(authorizedTransfer(transfer)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(transfer)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fromBalance").value(70.00))
				.andExpect(jsonPath("$.toBalance").value(40.00));
	}

	@Test
	void shouldRejectReusedTransactionReferenceWithDifferentPayload() throws Exception {
		String sourceAccount = createUser("guard-source@example.com", new BigDecimal("100.00"));
		String destinationAccount = createUser("guard-destination@example.com", new BigDecimal("10.00"));
		String otherDestination = createUser("guard-other@example.com", new BigDecimal("5.00"));

		DoTransDto original = new DoTransDto("TXN-GUARD-1", sourceAccount, destinationAccount, new BigDecimal("20.00"));
		mockMvc.perform(authorizedTransfer(original)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(original)))
				.andExpect(status().isOk());

		// Same transaction reference but different destination/amount must be rejected.
		DoTransDto mutated = new DoTransDto("TXN-GUARD-1", sourceAccount, otherDestination, new BigDecimal("25.00"));
		mockMvc.perform(authorizedTransfer(mutated)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(mutated)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Transaction reference already used with different payload"));
	}

	@Test
	void shouldHandleAtLeastOnceDeliveryWithoutDuplicateDebit() throws Exception {
		String sourceAccount = createUser("delivery-source@example.com", new BigDecimal("90.00"));
		String destinationAccount = createUser("delivery-destination@example.com", new BigDecimal("10.00"));
		DoTransDto transfer = new DoTransDto("TXN-DELIVERY-1", sourceAccount, destinationAccount, new BigDecimal("20.00"));

		// Simulate repeated delivery (client retry / gateway retry / message redelivery).
		for (int attempt = 0; attempt < 5; attempt++) {
			mockMvc.perform(authorizedTransfer(transfer)
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(transfer)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.fromBalance").value(70.00))
					.andExpect(jsonPath("$.toBalance").value(30.00));
		}

		mockMvc.perform(balance(sourceAccount))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.balance").value(70.00));

		mockMvc.perform(balance(destinationAccount))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.balance").value(30.00));
	}

	private String createUser(String email, BigDecimal initialBalance) throws Exception {
		CreateUserAccountRequest request = new CreateUserAccountRequest(email, "Test-Password-123", initialBalance);
		String response = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		JsonNode body = objectMapper.readTree(response);
		String accountNumber = body.get("accountNumber").asText();
		LoginRequest login = new LoginRequest();
		login.setEmail(email);
		login.setPassword("Test-Password-123");
		String authJson = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(login)))
			.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		tokensByAccount.put(accountNumber, objectMapper.readTree(authJson).get("accessToken").asText());
		return accountNumber;
	}

	private MockHttpServletRequestBuilder authorizedTransfer(DoTransDto transfer) {
		String ownerAccount = transfer.getFromAccount().isBlank() ? transfer.getToAccount() : transfer.getFromAccount();
		return post("/api/wallet/transfer").header("Authorization", "Bearer " + tokensByAccount.get(ownerAccount));
	}

	private MockHttpServletRequestBuilder balance(String accountNumber) {
		return get("/api/wallet/accounts/{accountNumber}/balance", accountNumber)
				.header("Authorization", "Bearer " + tokensByAccount.get(accountNumber));
	}
}
