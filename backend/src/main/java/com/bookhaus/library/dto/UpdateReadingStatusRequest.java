package com.bookhaus.library.dto;

import com.bookhaus.library.ReadingStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateReadingStatusRequest(@NotNull ReadingStatus status) {
}
