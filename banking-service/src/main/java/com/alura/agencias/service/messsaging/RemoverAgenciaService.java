package com.alura.agencias.service.messsaging;


import br.com.alura.Agencia;
import com.alura.agencias.domain.messaging.AgenciaMensagem;
import com.alura.agencias.service.AgenciaService;
import com.alura.agencias.service.http.saga.SagaHttpService;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.io.DataInput;

@ApplicationScoped
public class RemoverAgenciaService {

    @RestClient
    SagaHttpService sagaHttpService;

    private final AgenciaService agenciaService;

    public RemoverAgenciaService(AgenciaService agenciaService) {
        this.agenciaService = agenciaService;
    }

    @WithTransaction
    @Incoming("remover-agencia-channel")
    public Uni<Void> consumirMensagem(Agencia mensagem) {
        try {
            Log.info(mensagem);
            AgenciaMensagem agenciaMensagem = new AgenciaMensagem(
                    mensagem.getNome(),
                    mensagem.getRazaoSocial(),
                    mensagem.getCnpj(),
                    mensagem.getSituacaoCadastral()
            );
            return agenciaService.buscarPorCnpj(agenciaMensagem.getCnpj())
                    .onItem().ifNotNull().transformToUni(agencia ->
                        agenciaService.deletar(agencia.getId()).call(() -> sagaHttpService.fechaSaga(agencia.getCnpj())
                    ).replaceWithVoid());
//
        } catch (Exception e) {
            Log.error(e.getMessage());
            return Uni.createFrom().failure(e);
        }
    }

}
