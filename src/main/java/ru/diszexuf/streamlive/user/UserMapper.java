package ru.diszexuf.streamlive.user;

import org.mapstruct.Mapper;
import ru.diszexuf.streamlive.common.CommonMapper;
import ru.diszexuf.streamlive.model.UserResponseDto;

import java.util.List;

@Mapper(config = CommonMapper.class)
public interface UserMapper {

  UserResponseDto mapToDto(User entity);


  List<UserResponseDto> mapToDtos(List<User> entities);
}
