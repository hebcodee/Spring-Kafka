package br.com.alura.service.saga;

import br.com.alura.Agencia;
import br.com.alura.repository.SagaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.subscription.MultiEmitter;
import io.smallrye.reactive.messaging.MutinyEmitter;
import io.vertx.mutiny.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;

import java.time.LocalDateTime;

@ApplicationScoped
public class SagaResyncService {
    private final MultiEmitter<Agencia> kafkaEmitter;

    private final SagaRepository sagaRepository;

    private final ObjectMapper objectMapper;

    private final Vertx vertx;

    public SagaResyncService(MultiEmitter<Agencia> kafkaEmitter, SagaRepository sagaRepository, Vertx vertx) {
        this.kafkaEmitter = kafkaEmitter;
        this.sagaRepository = sagaRepository;
        this.objectMapper = new ObjectMapper();
        this.vertx = vertx;
    }

    @Scheduled(every = "5s")
    public void resync() {
        vertx.runOnContext(() -> {
            LocalDateTime limite = LocalDateTime.now().minusMinutes(2);

            sagaRepository.listByStatusAndCreatedAt(limite)
                    .subscribe().with(
                            sagas -> sagas.forEach(saga -> {
                                try {
                                    br.com.alura.domain.Agencia agenciaConvertida = objectMapper.readValue(saga.entidade, br.com.alura.domain.Agencia.class);
                                    kafkaEmitter.sendAndForget(new Agencia(agenciaConvertida.getNome(), agenciaConvertida.getRazaoSocial(), agenciaConvertida.getCnpj(), agenciaConvertida.getSituacaoCadastral()));
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }));
        });

    }
}
