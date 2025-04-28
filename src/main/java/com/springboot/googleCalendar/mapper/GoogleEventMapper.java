//package com.springboot.googleCalendar.mapper;
//
//import com.google.api.services.calendar.model.Event;
//import com.springboot.googleCalendar.dto.GoogleEventDto;
//import org.mapstruct.Mapper;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Mapper(componentModel = "spring")
//public interface GoogleEventMapper {
//
//    default GoogleEventDto eventToGoogleEventDto (Event event) {
//        return new GoogleEventDto(
//                event.getSummary(),
//                event.getDescription(),
//                event.getLocation(),
//                event.getStart().toString(),
//                event.getEnd().toString(),
//                event.getId()
//        );
//    }
//
//    default List<GoogleEventDto> eventListToGoogleEventDtoList (List<Event> eventList) {
//        List<GoogleEventDto> googleEventDtoList = eventList.stream()
//                .map(event -> eventToGoogleEventDto(event))
//                .collect(Collectors.toList());
//
//        return googleEventDtoList;
//    }
//}
//
