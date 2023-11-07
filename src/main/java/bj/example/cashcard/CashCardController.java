package bj.example.cashcard;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {

    // Injection du CashCardrepository
    private CashCardRepository cashCardRepository;

    public CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    // Récupérer toutes les ressources disponible 
    // @GetMapping
    // public ResponseEntity<Iterable<CashCard>> findAll() {
    //     return ResponseEntity.ok(cashCardRepository.findAll());
    // }

    // Récupérer une ressource par son identifiant
    @GetMapping("/{requestedId}")
    public ResponseEntity<CashCard> findById(@PathVariable Long requestedId) {
        Optional<CashCard> cashCardOptional = cashCardRepository.findById(requestedId);
        if (cashCardOptional.isPresent()) {
            return ResponseEntity.ok(cashCardOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Récupération des ressources paginées 
    @GetMapping
    public ResponseEntity<List<CashCard>> findAll(Pageable pageable) {
    Page<CashCard> page = cashCardRepository.findAll(PageRequest.of(pageable.getPageNumber(),pageable.getPageSize(),pageable.getSort()));
    return ResponseEntity.ok(page.getContent());
    }

    // Créer une ressource 
    @PostMapping
    private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb) {
        CashCard savedCashCard = cashCardRepository.save(newCashCardRequest);
        URI locationOfNewCashCard = ucb.path("cashcards/{id}").buildAndExpand(savedCashCard.id()).toUri();
        return ResponseEntity.created(locationOfNewCashCard).build();
    }
}
