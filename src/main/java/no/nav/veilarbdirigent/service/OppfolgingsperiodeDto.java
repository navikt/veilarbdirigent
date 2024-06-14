package no.nav.veilarbdirigent.service;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Builder
@Getter
public class OppfolgingsperiodeDto {
            private UUID uuid;
            private ZonedDateTime startDato;
            private ZonedDateTime sluttDato;
            private String aktorId;
            private StartetBegrunnelseDTO startetBegrunnelse;

            enum StartetBegrunnelseDTO {
                ARBEIDSSOKER,
                SYKEMELDT_MER_OPPFOLGING
            }
}

