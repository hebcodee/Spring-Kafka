package com.alura.agencias.service.messsaging;


import com.alura.agencias.domain.Agencia;
import com.alura.agencias.domain.messaging.AgenciaMensagem;
import com.alura.agencias.repository.AgenciaRepository;
import com.alura.agencias.service.AgenciaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.io.DataInput;

@ApplicationScoped
public class RemoverAgenciaService {

    private final AgenciaService agenciaService;
    private final ObjectMapper objectMapper;

    public RemoverAgenciaService(AgenciaService agenciaService) {
        this.agenciaService = agenciaService;
        objectMapper = new ObjectMapper();
    }

    @WithTransaction
    @Incoming("remover-agencia-channel")
    public Uni<Void> consumirMensagem(String mensagem) {
        try {
            Log.info(mensagem);
            AgenciaMensagem agenciaMensagem = objectMapper.readValue(mensagem, AgenciaMensagem.class);
            return agenciaService.buscarPorCnpj(agenciaMensagem.getCnpj())
                    .onItem().ifNotNull().transformToUni(agencia ->
                        agenciaService.deletar(agencia.getId())
                    ).replaceWithVoid();
//
        } catch (Exception e) {
            Log.error(e.getMessage());
            return Uni.createFrom().failure(e);
        }
    }

}
