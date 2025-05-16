package ru.diszexuf.streamlive.stream;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.diszexuf.streamlive.common.CommonMapper;
import ru.diszexuf.streamlive.model.StreamResponseDto;

import java.util.List;

@Mapper(config = CommonMapper.class)
public interface StreamMapper {

    StreamResponseDto toStreamDto(Stream stream);
    
    List<StreamResponseDto> toStreamDtos(List<Stream> streams);
}
