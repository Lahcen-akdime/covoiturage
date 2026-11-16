package com.covoiturage.covoiturage.response;

import lombok.Builder;

import java.util.Date;

@Builder
public record ErrorResponse(

     Date timestamp ,

     int statusCode ,

     String message ,

     String path
) {}
