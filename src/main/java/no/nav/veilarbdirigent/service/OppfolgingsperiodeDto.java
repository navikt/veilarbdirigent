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
                SYKEMELDT_MER_OPPFOLGING,
                MANUELL_REGISTRERING_VEILEDER // Lagt inn pga bug i veilarboppfølging, men de skal heller ikke ha cv-kort
            }
}

