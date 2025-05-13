package ru.diszexuf.streamlive.user;

import org.mapstruct.Mapper;
import ru.diszexuf.streamlive.common.CommonMapper;
import ru.diszexuf.streamlive.model.UserGetRequestDto;

import java.util.List;

@Mapper(config = CommonMapper.class)
public interface UserMapper {

  UserGetRequestDto mapToDto(User entity);

  List<UserGetRequestDto> mapToDtos(List<User> entities);
}
