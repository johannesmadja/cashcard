package bj.example.cashcard;

// import com.jayway.jsonpath.DocumentContext;
// import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CashcardApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;

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

}
