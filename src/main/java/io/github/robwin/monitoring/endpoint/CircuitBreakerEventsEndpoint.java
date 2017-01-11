/*
 * Copyright 2017 Robert Winkler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.robwin.monitoring.endpoint;


import io.github.robwin.circuitbreaker.event.CircuitBreakerEvent;
import io.github.robwin.consumer.EventConsumer;
import io.github.robwin.consumer.EventConsumerRegistry;
import javaslang.collection.Seq;
import org.springframework.boot.actuate.endpoint.mvc.EndpointMvcAdapter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Comparator;


@Component
@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
public class CircuitBreakerEventsEndpoint extends EndpointMvcAdapter {

    private final EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry;

    public CircuitBreakerEventsEndpoint(CircuitBreakerEndpoint circuitBreakerEndpoint,
                                        EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry) {
        super(circuitBreakerEndpoint);
        this.eventConsumerRegistry = eventConsumerRegistry;
    }

    @RequestMapping(value = "events", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Seq<CircuitBreakerEventDTO> getAllCircuitBreakerEvents() {
        return eventConsumerRegistry.getAllEventConsumer()
                .flatMap(EventConsumer::getBufferedEvents)
                .sorted(Comparator.comparing(CircuitBreakerEvent::getCreationTime))
                .map(CircuitBreakerEventDTOFactory::createCircuitBreakerEventDTO);
    }

    @RequestMapping(value = "events/{circuitBreakerName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Seq<CircuitBreakerEventDTO> getEventsFilteredByCircuitBreakerName(@PathVariable("circuitBreakerName") String circuitBreakerName) {
        return eventConsumerRegistry.getEventConsumer(circuitBreakerName).getBufferedEvents()
                .filter(event -> event.getCircuitBreakerName().equals(circuitBreakerName))
                .map(CircuitBreakerEventDTOFactory::createCircuitBreakerEventDTO);
    }

    @RequestMapping(value = "events/{circuitBreakerName}/{eventType}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Seq<CircuitBreakerEventDTO> getEventsFilteredByCircuitBreakerNameAndEventType(@PathVariable("circuitBreakerName") String circuitBreakerName,
                                                @PathVariable("eventType") String eventType) {
        return eventConsumerRegistry.getEventConsumer(circuitBreakerName).getBufferedEvents()
                .filter(event -> event.getCircuitBreakerName().equals(circuitBreakerName))
                .filter(event -> event.getEventType() == CircuitBreakerEvent.Type.valueOf(eventType.toUpperCase()))
                .map(CircuitBreakerEventDTOFactory::createCircuitBreakerEventDTO);
    }
}
