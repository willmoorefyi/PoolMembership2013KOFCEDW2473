package com.columbusclubevents.pool.membershipApplication.json;

import java.text.SimpleDateFormat;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

	@Override
   public ObjectMapper getContext(Class<?> arg0) {
      ObjectMapper objectMapper = new ObjectMapper();
      SerializationConfig serializationConfig = objectMapper.getSerializationConfig();
      serializationConfig.withDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ"));
      objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
      return objectMapper;
   }

}
