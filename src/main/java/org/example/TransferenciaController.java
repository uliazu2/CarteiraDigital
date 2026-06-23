package org.example;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transferencias")
@CrossOrigin(origins = "*")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    public TransferenciaController(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @PostMapping
    public ResponseEntity<String> realizarTransferencia(@RequestBody TransferenciaRequest request) {
        System.out.println("DEBUG: Recebendo transferencia - Origem: " + request.origemId());
        transferenciaService.transferir(
                request.origemId(),
                request.destinoId(),
                request.valorEmCentavos()
        );
        return ResponseEntity.ok("Transferência realizada com sucesso!");
    }
}
