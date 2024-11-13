package com.grs.utils;

import com.grs.api.model.request.FileDTO;
import com.grs.api.model.response.file.FileBaseDTO;
import com.grs.api.model.response.file.FileContainerDTO;
import com.grs.api.model.response.file.FileDerivedDTO;
import com.grs.core.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FileUploadUtil {

    private final StorageService storageService;

    public List<FileDTO> getFileDTOFromMultipart(List<MultipartFile> files, String fileNameByUser, Principal principal){
        FileContainerDTO fileContainerDTO = storageService.storeFileNew(principal, files.toArray(new MultipartFile[0]));
        List<FileBaseDTO> fileBaseDTOList = fileContainerDTO.getFiles();
        List<FileDTO> fileDTOS = new ArrayList<>();
        String[] fileNames = fileNameByUser.split(",");
        int i  = 0;
        for (FileBaseDTO f : fileBaseDTOList) {
            FileDerivedDTO fileDerivedDTO = (FileDerivedDTO) f;
            fileDTOS.add(
                    FileDTO.builder()
                            .name(fileNames[i++])
                            .url(fileDerivedDTO.getUrl())
                            .build()
            );
        }
        return fileDTOS;
    }
}
