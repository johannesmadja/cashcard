package bj.example.cashcard;

import net.minidev.json.JSONArray;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.test.annotation.DirtiesContext.*;

import java.net.URI;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class CashcardApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;

	// Test méthode GET [Récupéraiton d'une ressource]
	@Test
	void shouldReturnACardWhenDataIsSaved() {
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/99", String.class);
		// Vérification du status de la requête 
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		// Récupération du corps de la requête et converion en Json
		DocumentContext documentContext = JsonPath.parse(response.getBody());

		// Récupération de l'id et vérification de la correspondance
		Number id = documentContext.read("$.id");
		assertThat(id).isEqualTo(99);

		// Récupération du solde et vérification de la correspondance 
		Double amount = documentContext.read("$.amount");
		assertThat(amount).isEqualTo(123.45);
	}

	@Test 
	void shouldNotReturnACashCardWithAnUnknownId() {
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards/1000", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}


	// Test méthode POST [Création d'une nouvelle ressource]
	@Test 
	@DirtiesContext
	void shouldCreatedANewCashCard() {
		CashCard newCashCard = new CashCard(null, 250.00);
		ResponseEntity<Void> createResponse = restTemplate.postForEntity("/cashcards", newCashCard, Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		// Récupération de l'URI	
		URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
		// Envoi d'une requête GET vers l'addrese contenue dans l'URI	
		ResponseEntity<String> getResponse = restTemplate.getForEntity(locationOfNewCashCard, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		// Récupération du corps de la réponse 
		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		// Récupération du ID et du amount
		Number id = documentContext.read("@.id");
		Double amount = documentContext.read("@.amount");

		assertThat(id).isNotNull(); 
		assertThat(amount).isEqualTo(250.00);

	}

	// Test de méthode GET [Récupération de toutes les cards]
	@Test 
	void shouldReturnAllCashCardWhenLisdIsRequested() {
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		// Extraction des données de la réponse renvoyée 
		DocumentContext documentContext = JsonPath.parse(response.getBody());

		// Comparaison des données 
		int cashCardCount = documentContext.read("$.length()");
		assertThat(cashCardCount).isEqualTo(3);
   
		JSONArray ids = documentContext.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);
   
		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.00, 150.00);
	}

	// Test pour la pagination 
	@Test 
	void shouldReturnAPageOfCashCards() {
		ResponseEntity<String> response = restTemplate.getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(1);

		double amount = documentContext.read("$[0].amount");
		assertThat(amount).isEqualTo(150.00);
	}


}
