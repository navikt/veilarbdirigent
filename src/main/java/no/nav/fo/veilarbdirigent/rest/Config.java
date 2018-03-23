package no.nav.fo.veilarbdirigent.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.fo.veilarbdirigent.utils.SerializerUtils;
import org.springframework.stereotype.Component;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Component
@Provider
public  class Config implements ContextResolver<ObjectMapper> {
    @Override
    public ObjectMapper getContext(Class<?> aClass) {
        return SerializerUtils.mapper;
    }
}
