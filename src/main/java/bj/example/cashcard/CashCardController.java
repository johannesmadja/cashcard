package bj.example.cashcard;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
// import org.springframework.data.domain.Sort;

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

    // Méthode qui récupère le cashcard depuis le repository 
    private CashCard findCashCard(Long requestedId, Principal principal) {
        return cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
    }

    // Récupérer une ressource par son identifiant
    @GetMapping("/{requestedId}")
    public ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
       CashCard cashCard = findCashCard(requestedId, principal); 

       if (cashCard != null) {
        return ResponseEntity.ok(cashCard);
       }else {
        return ResponseEntity.notFound().build();
       }

    }

    // Récupération des ressources paginées 
    @GetMapping
    public ResponseEntity<List<CashCard>> findAll(Pageable pageable, Principal principal) {
    Page<CashCard> page = cashCardRepository
                                            .findByOwner(principal.getName(),
                                            PageRequest.of(
                                                pageable.getPageNumber(),
                                                pageable.getPageSize(),
                                                pageable.getSort()));
    return ResponseEntity.ok(page.getContent());
    }

    // Créer une ressource 
    @PostMapping
    private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb, Principal principal) {
        CashCard cashCardWithOwner = new CashCard(null, newCashCardRequest.amount(), principal.getName());
        CashCard savedCashCard = cashCardRepository.save(cashCardWithOwner);
        URI locationOfNewCashCard = ucb.path("cashcards/{id}").buildAndExpand(savedCashCard.id()).toUri();
        return ResponseEntity.created(locationOfNewCashCard).build();
    }

    // Mise à jour d'une ressource 
    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> putCashCard(@PathVariable Long requestedId, @RequestBody CashCard cashCardUpdate, Principal principal) {
        CashCard cashCard = findCashCard(requestedId, principal);
        if(cashCard != null) {
            CashCard updateCashCard = new CashCard(cashCard.id(), cashCardUpdate.amount(), principal.getName());
    
            // Sauvegarde de la resssource 
            cashCardRepository.save(updateCashCard);
            return ResponseEntity.noContent().build();

        }
        return ResponseEntity.notFound().build();

    }

    // Suppression d'une ressource 
    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteCashCard(@PathVariable Long id, Principal principal) {
        if (cashCardRepository.existsByIdAndOwner(id, principal.getName())) {
            cashCardRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
        
    }
}
