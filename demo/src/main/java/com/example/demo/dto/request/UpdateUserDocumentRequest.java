package com.example.demo.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserDocumentRequest {
    String cccd;           // số CCCD
    String gplx;           // số GPLX
    MultipartFile cccdFile; // ảnh CCCD
    MultipartFile gplxFile; // ảnh GPLX
}
